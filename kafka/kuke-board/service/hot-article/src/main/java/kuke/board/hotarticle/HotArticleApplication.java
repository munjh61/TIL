package kuke.board.hotarticle;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 인기 게시글(Hot Article) 서비스의 메인 애플리케이션 클래스입니다.
 * Spring Boot 애플리케이션의 시작점(entry point) 역할을 합니다.
 */
@SpringBootApplication
public class HotArticleApplication {
    public static void main(String[] args) {
        SpringApplication.run(HotArticleApplication.class, args);
    }
}