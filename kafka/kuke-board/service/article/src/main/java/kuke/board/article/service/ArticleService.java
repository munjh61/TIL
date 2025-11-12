package kuke.board.article.service;

import kuke.board.article.entity.Article;
import kuke.board.article.entity.BoardArticleCount;
import kuke.board.article.repository.ArticleRepository;
import kuke.board.article.repository.BoardArticleCountRepository;
import kuke.board.article.service.request.ArticleCreateRequest;
import kuke.board.article.service.request.ArticleUpdateRequest;
import kuke.board.article.service.response.ArticlePageResponse;
import kuke.board.article.service.response.ArticleResponse;
import kuke.board.common.event.EventType;
import kuke.board.common.event.payload.ArticleCreatedEventPayload;
import kuke.board.common.event.payload.ArticleDeletedEventPayload;
import kuke.board.common.event.payload.ArticleUpdatedEventPayload;
import kuke.board.common.outboxmessagerelay.OutboxEventPublisher;
import kuke.board.common.snowflake.Snowflake;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 게시글(Article) 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 */
@Service
@RequiredArgsConstructor
public class ArticleService {
    private final Snowflake snowflake = new Snowflake(); // 고유 ID 생성을 위한 Snowflake 인스턴스
    private final ArticleRepository articleRepository;
    private final OutboxEventPublisher outboxEventPublisher; // Outbox 이벤트를 발행하기 위한 퍼블리셔
    private final BoardArticleCountRepository boardArticleCountRepository; // 게시판별 게시글 수 관리를 위한 리포지토리

    /**
     * 새로운 게시글을 생성합니다.
     * @param request 게시글 생성 요청 정보
     * @return 생성된 게시글 정보
     */
    @Transactional
    public ArticleResponse create(ArticleCreateRequest request) {
        // 1. Snowflake를 사용하여 새 게시글 ID를 생성하고 Article 엔티티를 저장합니다.
        Article article = articleRepository.save(
                Article.create(snowflake.nextId(), request.getTitle(), request.getContent(), request.getBoardId(), request.getWriterId())
        );
        // 2. 게시판의 게시글 수를 1 증가시킵니다.
        int result = boardArticleCountRepository.increase(request.getBoardId());
        // 만약 업데이트된 행이 0이라면, 해당 게시판의 첫 글이므로 카운트를 1로 초기화합니다.
        if (result == 0) {
            boardArticleCountRepository.save(
                    BoardArticleCount.init(request.getBoardId(), 1L)
            );
        }

        // 3. '게시글 생성' 이벤트를 Outbox를 통해 발행합니다.
        // 이 이벤트는 다른 서비스(예: article-read)에서 소비하여 데이터를 동기화하는 데 사용됩니다.
        outboxEventPublisher.publish(
                EventType.ARTICLE_CREATED,
                ArticleCreatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getBoardId() // boardId를 샤드 키로 사용하여 메시지 순서를 보장합니다.
        );

        return ArticleResponse.from(article);
    }

    /**
     * 기존 게시글을 수정합니다.
     * @param articleId 수정할 게시글 ID
     * @param request 게시글 수정 요청 정보
     * @return 수정된 게시글 정보
     */
    @Transactional
    public ArticleResponse update(Long articleId, ArticleUpdateRequest request) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        article.update(request.getTitle(), request.getContent());

        // '게시글 수정' 이벤트를 Outbox를 통해 발행합니다.
        outboxEventPublisher.publish(
                EventType.ARTICLE_UPDATED,
                ArticleUpdatedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .build(),
                article.getBoardId()
        );
        return ArticleResponse.from(article);
    }

    /**
     * 특정 게시글을 조회합니다.
     * @param articleId 조회할 게시글 ID
     * @return 조회된 게시글 정보
     */
    public ArticleResponse read(Long articleId) {
        return ArticleResponse.from(articleRepository.findById(articleId).orElseThrow());
    }

    /**
     * 특정 게시글을 삭제합니다.
     * @param articleId 삭제할 게시글 ID
     */
    @Transactional
    public void delete(Long articleId) {
        Article article = articleRepository.findById(articleId).orElseThrow();
        articleRepository.delete(article);
        boardArticleCountRepository.decrease(article.getBoardId()); // 게시판 게시글 수 감소

        // '게시글 삭제' 이벤트를 Outbox를 통해 발행합니다.
        outboxEventPublisher.publish(
                EventType.ARTICLE_DELETED,
                ArticleDeletedEventPayload.builder()
                        .articleId(article.getArticleId())
                        .title(article.getTitle())
                        .content(article.getContent())
                        .boardId(article.getBoardId())
                        .writerId(article.getWriterId())
                        .createdAt(article.getCreatedAt())
                        .modifiedAt(article.getModifiedAt())
                        .boardArticleCount(count(article.getBoardId()))
                        .build(),
                article.getBoardId()
        );
    }

    /**
     * 특정 게시판의 게시글 목록을 오프셋 기반 페이지네이션으로 조회합니다.
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 당 게시글 수
     * @return 페이징된 게시글 목록 정보
     */
    public ArticlePageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticlePageResponse.of(
                articleRepository.findAll(boardId, (page - 1) * pageSize, pageSize).stream()
                        .map(ArticleResponse::from)
                        .toList(),
                articleRepository.count(
                        boardId,
                        PageLimitCalculator.calculatePageLimit(page, pageSize, 10L)
                )
        );
    }

    /**
     * 특정 게시판의 게시글 목록을 무한 스크롤(커서 기반 페이지네이션) 방식으로 조회합니다.
     * @param boardId 게시판 ID
     * @param pageSize 조회할 개수
     * @param lastArticleId 마지막으로 조회된 게시글의 ID (첫 페이지의 경우 null)
     * @return 조회된 게시글 목록
     */
    public List<ArticleResponse> readAllInfiniteScroll(Long boardId, Long pageSize, Long lastArticleId) {
        List<Article> articles = lastArticleId == null ?
                articleRepository.findAllInfiniteScroll(boardId, pageSize) :
                articleRepository.findAllInfiniteScroll(boardId, pageSize, lastArticleId);
        return articles.stream().map(ArticleResponse::from).toList();
    }

    /**
     * 특정 게시판의 총 게시글 수를 조회합니다.
     * @param boardId 게시판 ID
     * @return 총 게시글 수
     */
    public Long count(Long boardId) {
        return boardArticleCountRepository.findById(boardId)
                .map(BoardArticleCount::getArticleCount)
                .orElse(0L);
    }
}