package kuke.board.common.outboxmessagerelay;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Outbox 엔티티에 대한 데이터 접근을 처리하는 Spring Data JPA 리포지토리입니다.
 */
@Repository
public interface OutboxRepository extends JpaRepository<Outbox, Long> {

    /**
     * 특정 샤드 키(shardKey)를 가지며, 주어진 시간(from) 이전에 생성된 아웃박스 메시지들을
     * 생성 시각(createdAt) 오름차순으로 정렬하여 페이지 단위로 조회합니다.
     * 이 메서드는 메시지 릴레이가 처리할 메시지를 순서대로 가져오는 데 사용됩니다.
     *
     * @param shardKey 조회할 샤드 키
     * @param from 조회할 시간 범위의 상한선
     * @param pageable 페이징 처리 정보 (가져올 개수, 페이지 번호 등)
     * @return 조회된 Outbox 메시지 리스트
     */
    List<Outbox> findAllByShardKeyAndCreatedAtLessThanEqualOrderByCreatedAtAsc(
            Long shardKey,
            LocalDateTime from,
            Pageable pageable
    );
}