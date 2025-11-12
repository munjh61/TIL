package kuke.board.articleread.service;

import kuke.board.articleread.client.ArticleClient;
import kuke.board.articleread.client.CommentClient;
import kuke.board.articleread.client.LikeClient;
import kuke.board.articleread.client.ViewClient;
import kuke.board.articleread.repository.ArticleIdListRepository;
import kuke.board.articleread.repository.ArticleQueryModel;
import kuke.board.articleread.repository.ArticleQueryModelRepository;
import kuke.board.articleread.repository.BoardArticleCountRepository;
import kuke.board.articleread.service.event.handler.EventHandler;
import kuke.board.articleread.service.response.ArticleReadPageResponse;
import kuke.board.articleread.service.response.ArticleReadResponse;
import kuke.board.common.event.Event;
import kuke.board.common.event.EventPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * 게시글 조회 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * CQRS 패턴의 조회(Query) 부분을 담당하며, 데이터 동기화 및 조회를 위한 핵심 로직을 포함합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ArticleReadService {
    // 다른 서비스와 통신하기 위한 클라이언트들
    private final ArticleClient articleClient;
    private final CommentClient commentClient;
    private final LikeClient likeClient;
    private final ViewClient viewClient;

    // Redis 기반의 읽기 모델 리포지토리들
    private final ArticleIdListRepository articleIdListRepository;
    private final ArticleQueryModelRepository articleQueryModelRepository;
    private final BoardArticleCountRepository boardArticleCountRepository;

    // 이벤트 처리를 위한 핸들러 목록 (Spring이 자동으로 주입)
    private final List<EventHandler> eventHandlers;


    /**
     * Kafka 컨슈머로부터 받은 이벤트를 처리합니다.
     * Strategy Pattern을 사용하여 이벤트 타입에 맞는 핸들러를 찾아 실행합니다.
     * @param event 처리할 이벤트
     */
    public void handleEvent(Event<EventPayload> event) {
        for (EventHandler eventHandler : eventHandlers) {
            if (eventHandler.supports(event)) {
                eventHandler.handle(event);
                return; // 적절한 핸들러를 찾으면 실행하고 종료
            }
        }
    }

    /**
     * 단일 게시글을 조회합니다.
     * 'Cache-Aside' 패턴을 사용하여 Redis 캐시를 먼저 조회하고, 없으면 원본 서비스에서 가져와 캐시를 채웁니다.
     * @param articleId 조회할 게시글 ID
     * @return 조회된 게시글 정보
     */
    public ArticleReadResponse read(Long articleId) {
        // 1. Redis에서 ArticleQueryModel을 조회합니다.
        ArticleQueryModel articleQueryModel = articleQueryModelRepository.read(articleId)
                // 2. 캐시에 없으면 or()를 통해 fetch 메서드를 호출하여 원본 데이터로부터 가져옵니다.
                .or(() -> fetch(articleId))
                .orElseThrow();

        // 3. 최종적으로 ArticleQueryModel과 조회수(viewClient)를 조합하여 응답을 생성합니다.
        return ArticleReadResponse.from(
                articleQueryModel,
                viewClient.count(articleId) // 조회수는 별도로 캐시/조회
        );
    }

    /**
     * 캐시 미스(cache miss) 시 원본 서비스들로부터 데이터를 가져와 ArticleQueryModel을 생성하고 캐시에 저장합니다.
     * @param articleId 조회할 게시글 ID
     * @return 생성된 ArticleQueryModel을 담은 Optional
     */
    private Optional<ArticleQueryModel> fetch(Long articleId) {
        // 1. 각 클라이언트를 호출하여 게시글 정보, 댓글 수, 좋아요 수를 가져옵니다.
        Optional<ArticleQueryModel> articleQueryModelOptional = articleClient.read(articleId)
                .map(article -> ArticleQueryModel.create(
                        article,
                        commentClient.count(articleId),
                        likeClient.count(articleId)
                ));
        // 2. 성공적으로 모델을 생성했다면, 다음 조회를 위해 Redis에 저장합니다.
        articleQueryModelOptional
                .ifPresent(articleQueryModel -> articleQueryModelRepository.create(articleQueryModel, Duration.ofDays(1)));
        log.info("[ArticleReadService.fetch] fetch data. articleId={}, isPresent={}", articleId, articleQueryModelOptional.isPresent());
        return articleQueryModelOptional;
    }

    /**
     * 오프셋 기반 페이지네이션으로 게시글 목록을 조회합니다.
     * @param boardId 게시판 ID
     * @param page 페이지 번호
     * @param pageSize 페이지 크기
     * @return 페이징된 게시글 목록 정보
     */
    public ArticleReadPageResponse readAll(Long boardId, Long page, Long pageSize) {
        return ArticleReadPageResponse.of(
                readAll(
                        readAllArticleIds(boardId, page, pageSize)
                ),
                count(boardId)
        );
    }

    /**
     * 주어진 ID 목록에 해당하는 게시글 목록을 조회합니다. (Bulk Read)
     * @param articleIds 조회할 게시글 ID 리스트
     * @return ArticleReadResponse 리스트
     */
    private List<ArticleReadResponse> readAll(List<Long> articleIds) {
        // 1. Redis에서 ID 목록에 해당하는 ArticleQueryModel들을 한 번에 조회합니다.
        Map<Long, ArticleQueryModel> articleQueryModelMap = articleQueryModelRepository.readAll(articleIds);
        return articleIds.stream()
                .map(articleId -> articleQueryModelMap.containsKey(articleId) ?
                        // 2. 캐시에 있으면 바로 사용합니다.
                        articleQueryModelMap.get(articleId) :
                        // 3. 캐시에 없으면 fetch를 통해 개별적으로 다시 채웁니다.
                        fetch(articleId).orElse(null))
                .filter(Objects::nonNull)
                .map(articleQueryModel ->
                        // 4. 각 모델에 조회수를 조합하여 최종 응답을 만듭니다.
                        ArticleReadResponse.from(
                                articleQueryModel,
                                viewClient.count(articleQueryModel.getArticleId())
                        ))
                .toList();
    }

    /**
     * 오프셋 기반 페이지네이션을 위한 게시글 ID 목록을 조회합니다.
     * Redis에 캐시된 ID 목록을 우선 사용하고, 데이터가 부족하면 원본 서비스에서 조회합니다.
     */
    private List<Long> readAllArticleIds(Long boardId, Long page, Long pageSize) {
        List<Long> articleIds = articleIdListRepository.readAll(boardId, (page - 1) * pageSize, pageSize);
        // Redis에서 가져온 ID 개수가 요청한 페이지 크기와 같으면 캐시가 최신 상태라고 가정합니다.
        if (pageSize == articleIds.size()) {
            log.info("[ArticleReadService.readAllArticleIds] return redis data.");
            return articleIds;
        }
        // ID 개수가 부족하면 캐시가 오래되었거나 불완전하다고 판단하고, 원본 서비스에서 직접 조회합니다.
        log.info("[ArticleReadService.readAllArticleIds] return origin data.");
        return articleClient.readAll(boardId, page, pageSize).getArticles().stream()
                .map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }

    /**
     * 특정 게시판의 총 게시글 수를 조회합니다.
     * Redis 카운터 캐시를 우선 사용하고, 없으면 원본 서비스에서 조회 후 캐시를 업데이트합니다.
     */
    private long count(Long boardId) {
        Long result = boardArticleCountRepository.read(boardId);
        if (result != null) {
            return result;
        }
        long count = articleClient.count(boardId);
        boardArticleCountRepository.createOrUpdate(boardId, count);
        return count;
    }

    /**
     * 무한 스크롤 방식으로 게시글 목록을 조회합니다.
     */
    public List<ArticleReadResponse> readAllInfiniteScroll(Long boardId, Long lastArticleId, Long pageSize) {
        return readAll(
                readAllInfiniteScrollArticleIds(boardId, lastArticleId, pageSize)
        );
    }

    /**
     * 무한 스크롤을 위한 게시글 ID 목록을 조회합니다.
     * Redis에 캐시된 ID 목록을 우선 사용하고, 데이터가 부족하면 원본 서비스에서 조회합니다.
     */
    private List<Long> readAllInfiniteScrollArticleIds(Long boardId, Long lastArticleId, Long pageSize) {
        List<Long> articleIds = articleIdListRepository.readAllInfiniteScroll(boardId, lastArticleId, pageSize);
        if (pageSize == articleIds.size()) {
            log.info("[ArticleReadService.readAllInfiniteScrollArticleIds] return redis data.");
            return articleIds;
        }
        log.info("[ArticleReadService.readAllInfiniteScrollArticleIds] return origin data.");
        return articleClient.readAllInfiniteScroll(boardId, lastArticleId, pageSize).stream()
                .map(ArticleClient.ArticleResponse::getArticleId)
                .toList();
    }

}