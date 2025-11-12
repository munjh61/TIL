package kuke.board.view.service;

import kuke.board.view.repository.ArticleViewCountRepository;
import kuke.board.view.repository.ArticleViewDistributedLockRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 게시글 조회수 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * Redis를 이용한 실시간 조회수 증가, 분산 락을 통한 중복 조회 방지,
 * 그리고 주기적인 데이터베이스 백업을 조율합니다.
 */
@Service
@RequiredArgsConstructor
public class ArticleViewService {
    private final ArticleViewCountRepository articleViewCountRepository; // Redis 기반 실시간 조회수 리포지토리
    private final ArticleViewCountBackUpProcessor articleViewCountBackUpProcessor; // DB 백업 및 이벤트 발행 프로세서
    private final ArticleViewDistributedLockRepository articleViewDistributedLockRepository; // 분산 락 리포지토리

    private static final int BACK_UP_BACH_SIZE = 100; // 조회수가 이 값의 배수가 될 때마다 DB에 백업
    private static final Duration TTL = Duration.ofMinutes(10); // 사용자별 조회수 락의 만료 시간

    /**
     * 특정 게시글의 조회수를 증가시킵니다.
     * 사용자별 분산 락을 통해 일정 시간 내 중복 조회를 방지하고,
     * 조회수가 특정 임계값에 도달하면 데이터베이스에 백업합니다.
     *
     * @param articleId 조회수를 증가시킬 게시글 ID
     * @param userId 조회수를 증가시키는 사용자 ID
     * @return 증가된 후의 현재 조회수
     */
    public Long increase(Long articleId, Long userId) {
        // 1. 사용자별 분산 락을 시도합니다. (예: 10분 내에 같은 사용자가 같은 게시글을 다시 조회하는 것을 방지)
        if (!articleViewDistributedLockRepository.lock(articleId, userId, TTL)) {
            // 락 획득에 실패하면 (이미 락이 걸려있으면), 조회수를 증가시키지 않고 현재 조회수를 반환합니다.
            return articleViewCountRepository.read(articleId);
        }

        // 2. Redis에서 게시글 조회수를 원자적으로 1 증가시킵니다.
        Long count = articleViewCountRepository.increase(articleId);

        // 3. 조회수가 BACK_UP_BACH_SIZE의 배수가 될 때마다 데이터베이스에 백업하고 이벤트를 발행합니다.
        //    이는 모든 조회수 증가마다 DB에 접근하는 것을 방지하여 부하를 줄입니다.
        if (count % BACK_UP_BACH_SIZE == 0) {
            articleViewCountBackUpProcessor.backUp(articleId, count);
        }
        return count;
    }

    /**
     * 특정 게시글의 현재 조회수를 조회합니다.
     * @param articleId 조회할 게시글 ID
     * @return 현재 조회수 (Redis에서 가져옴)
     */
    public Long count(Long articleId) {
        return articleViewCountRepository.read(articleId);
    }
}