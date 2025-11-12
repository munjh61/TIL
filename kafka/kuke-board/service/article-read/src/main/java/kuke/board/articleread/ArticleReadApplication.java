package kuke.board.articleread;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 게시글 조회(Article-Read) 서비스의 메인 애플리케이션 클래스입니다.
 * CQRS 패턴에서 조회(Query) 역할을 담당하는 마이크로서비스의 시작점입니다.
 */
@SpringBootApplication
public class ArticleReadApplication {
    public static void main(String[] args) {
        SpringApplication.run(ArticleReadApplication.class, args);
    }
}