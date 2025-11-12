package kuke.board.article.repository;

import kuke.board.article.entity.Article;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Article 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 * 페이지네이션을 위해 다양한 방식의 네이티브 쿼리를 사용합니다.
 */
@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {

    /**
     * 전통적인 오프셋(offset) 기반 페이지네이션으로 게시글 목록을 조회합니다.
     * 서브쿼리를 사용하여 먼저 ID 목록을 페이징하고, 그 결과와 조인하여 전체 데이터를 가져옵니다.
     * 이는 'covering index'를 활용하여 딥 페이징(deep paging) 시의 성능 저하를 완화하는 최적화 기법입니다.
     * @param boardId 게시판 ID
     * @param offset 조회 시작 위치
     * @param limit 조회할 개수
     * @return 조회된 Article 리스트
     */
    @Query(
            value = "select article.article_id, article.title, article.content, article.board_id, article.writer_id, " +
                    "article.created_at, article.modified_at " +
                    "from (" +
                    "   select article_id from article " +
                    "   where board_id = :boardId " +
                    "   order by article_id desc " +
                    "   limit :limit offset :offset " +
                    ") t left join article on t.article_id = article.article_id ",
            nativeQuery = true
    )
    List<Article> findAll(
            @Param("boardId") Long boardId,
            @Param("offset") Long offset,
            @Param("limit") Long limit
    );

    /**
     * 특정 게시판의 게시글 수를 제한적으로 카운트합니다.
     * 일반적인 총 개수 조회가 아니라, 다음 페이지의 존재 여부 등을 확인하기 위한 목적으로 보입니다.
     * @param boardId 게시판 ID
     * @param limit 조회할 최대 개수
     * @return 카운트된 게시글 수
     */
    @Query(
            value = "select count(*) from (" +
                    "   select article_id from article where board_id = :boardId limit :limit" +
                    ") t",
            nativeQuery = true
    )
    Long count(@Param("boardId") Long boardId, @Param("limit") Long limit);

    /**
     * 무한 스크롤(커서 기반 페이지네이션)의 첫 페이지를 조회합니다.
     * @param boardId 게시판 ID
     * @param limit 조회할 개수
     * @return 조회된 Article 리스트
     */
    @Query(
            value = "select article.article_id, article.title, article.content, article.board_id, article.writer_id, " +
                    "article.created_at, article.modified_at " +
                    "from article " +
                    "where board_id = :boardId " +
                    "order by article_id desc limit :limit",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(@Param("boardId") Long boardId, @Param("limit") Long limit);

    /**
     * 무한 스크롤의 다음 페이지를 조회합니다.
     * 마지막으로 조회된 게시글의 ID(lastArticleId)보다 작은 ID를 가진 게시글들을 조회합니다.
     * @param boardId 게시판 ID
     * @param limit 조회할 개수
     * @param lastArticleId 마지막으로 조회된 게시글의 ID
     * @return 조회된 Article 리스트
     */
    @Query(
            value = "select article.article_id, article.title, article.content, article.board_id, article.writer_id, " +
                    "article.created_at, article.modified_at " +
                    "from article " +
                    "where board_id = :boardId and article_id < :lastArticleId " +
                    "order by article_id desc limit :limit",
            nativeQuery = true
    )
    List<Article> findAllInfiniteScroll(
            @Param("boardId") Long boardId,
            @Param("limit") Long limit,
            @Param("lastArticleId") Long lastArticleId
    );
}