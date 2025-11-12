package kuke.board.article.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * 각 게시판(Board)별 게시글(Article)의 총 개수를 저장하는 엔티티입니다.
 * 이는 게시글 수를 조회할 때마다 전체 테이블을 카운트하는 비효율을 피하기 위한
 * 일종의 카운터 캐시(counter cache) 역할을 합니다. (비정규화)
 */
@Table(name = "board_article_count") // 'board_article_count' 테이블과 매핑
@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA를 위한 기본 생성자
public class BoardArticleCount {
    @Id
    private Long boardId; // 게시판 ID. 이 테이블의 기본 키(Primary Key)이자 샤드 키(shard key)입니다.

    private Long articleCount; // 해당 게시판의 총 게시글 수

    /**
     * 새로운 BoardArticleCount 객체를 생성(초기화)하는 정적 팩토리 메서드입니다.
     * @param boardId 게시판 ID
     * @param articleCount 게시글 수
     * @return 생성된 BoardArticleCount 객체
     */
    public static BoardArticleCount init(Long boardId, Long articleCount) {
        BoardArticleCount boardArticleCount = new BoardArticleCount();
        boardArticleCount.boardId = boardId;
        boardArticleCount.articleCount = articleCount;
        return boardArticleCount;
    }
}