package kuke.board.comment.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * 페이지네이션 UI를 위한 limit 값을 계산하는 유틸리티 클래스입니다.
 * article 서비스의 PageLimitCalculator와 동일한 클래스입니다.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageLimitCalculator {

    /**
     * 페이지네이션에서 '다음 페이지 그룹'의 존재 여부를 확인하기 위한 limit 값을 계산합니다.
     * 예를 들어, 페이지네이션 UI가 1-10, 11-20과 같이 10개 단위로 표시될 때,
     * 현재 페이지가 1-10 사이에 있다면, 11페이지가 존재하는지 확인하기 위해 필요한 최소한의
     * 아이템 수를 계산합니다.
     *
     * 로직 분석:
     * 1. `(page - 1) / movablePageCount`: 현재 페이지가 몇 번째 페이지 그룹에 속하는지 계산합니다. (0-indexed)
     * 2. `(...) + 1`: 1-indexed 그룹 번호로 변환합니다.
     * 3. `(...) * pageSize * movablePageCount`: 현재 페이지 그룹의 마지막 페이지까지 필요한 총 아이템 수를 계산합니다.
     * 4. `(...) + 1`: 다음 페이지(그룹)에 아이템이 하나라도 더 있는지 확인하기 위해 1을 더합니다.
     *
     * 이 값은 Repository의 count 쿼리에서 limit으로 사용되어, 전체 테이블을 count하는 대신
     * 효율적으로 다음 페이지 그룹의 존재 여부를 파악할 수 있게 합니다.
     *
     * @param page 현재 페이지 번호 (1-based)
     * @param pageSize 페이지 당 아이템 수
     * @param movablePageCount 페이지네이션 UI에 표시되는 페이지 번호의 개수
     * @return 계산된 limit 값
     */
    public static Long calculatePageLimit(Long page, Long pageSize, Long movablePageCount) {
        return (((page - 1) / movablePageCount) + 1) * pageSize * movablePageCount + 1;
    }
}