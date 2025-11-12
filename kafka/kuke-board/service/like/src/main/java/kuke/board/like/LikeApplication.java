package kuke.board.like;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * 좋아요(Like) 서비스의 메인 애플리케이션 클래스입니다.
 * Spring Boot 애플리케이션의 시작점(entry point) 역할을 합니다.
 */
// @EntityScan: JPA 엔티티를 스캔할 패키지를 지정합니다.
// "kuke.board" 패키지 및 하위 패키지 전체에서 @Entity 어노테이션이 붙은 클래스를 찾습니다.
@EntityScan(basePackages = "kuke.board")
@SpringBootApplication
// @EnableJpaRepositories: Spring Data JPA 리포지토리를 스캔할 패키지를 지정합니다.
// "kuke.board" 패키지 및 하위 패키지 전체에서 JPA 리포지토리 인터페이스를 찾습니다.
@EnableJpaRepositories(basePackages = "kuke.board")
public class LikeApplication {
    public static void main(String[] args) {
        SpringApplication.run(LikeApplication.class, args);
    }
}