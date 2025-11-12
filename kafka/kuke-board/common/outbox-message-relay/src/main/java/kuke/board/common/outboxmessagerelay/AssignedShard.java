package kuke.board.common.outboxmessagerelay;

import lombok.Getter;

import java.util.List;
import java.util.stream.LongStream;

/**
 * 특정 애플리케이션 인스턴스에 할당된 샤드(shard)의 집합을 나타내는 클래스입니다.
 * 샤드 할당은 결정론적으로 이루어지므로, 동일한 입력에 대해서는 항상 동일한 결과를 반환합니다.
 */
@Getter
public class AssignedShard {
    private List<Long> shards; // 현재 인스턴스에 할당된 샤드 ID 목록

    /**
     * 주어진 정보를 바탕으로 AssignedShard 객체를 생성하는 정적 팩토리 메서드입니다.
     * @param appId 현재 애플리케이션 인스턴스의 ID
     * @param appIds 활성화된 모든 인스턴스 ID 목록 (정렬된 상태)
     * @param shardCount 전체 샤드의 개수
     * @return 생성된 AssignedShard 객체
     */
    public static AssignedShard of(String appId, List<String> appIds, long shardCount) {
        AssignedShard assignedShard = new AssignedShard();
        assignedShard.shards = assign(appId, appIds, shardCount);
        return assignedShard;
    }

    /**
     * 샤드를 각 인스턴스에 할당하는 핵심 로직입니다.
     * 전체 샤드를 활성화된 인스턴스 수로 나누어 각 인스턴스에 범위를 할당합니다.
     * @param appId 현재 인스턴스 ID
     * @param appIds 모든 인스턴스 ID 목록
     * @param shardCount 전체 샤드 개수
     * @return 현재 인스턴스에 할당된 샤드 ID 리스트
     */
    private static List<Long> assign(String appId, List<String> appIds, long shardCount) {
        // 1. 전체 앱 목록에서 현재 앱의 인덱스를 찾습니다.
        int appIndex = findAppIndex(appId, appIds);
        if (appIndex == -1) {
            // 목록에 없으면 할당할 샤드가 없습니다.
            return List.of();
        }

        // 2. 현재 앱 인덱스를 사용하여 담당할 샤드의 시작과 끝 범위를 계산합니다.
        // 예: shardCount=10, appIds.size()=2
        // appIndex 0: start=0, end=4  (0, 1, 2, 3, 4)
        // appIndex 1: start=5, end=9  (5, 6, 7, 8, 9)
        long start = (long) appIndex * shardCount / appIds.size();
        long end = (long) (appIndex + 1) * shardCount / appIds.size() - 1;

        // 3. 범위 내의 모든 샤드 ID를 리스트로 만들어 반환합니다.
        return LongStream.rangeClosed(start, end).boxed().toList();
    }

    /**
     * 정렬된 인스턴스 ID 목록에서 특정 ID의 인덱스를 찾습니다.
     * @param appId 찾을 인스턴스 ID
     * @param appIds 전체 인스턴스 ID 목록
     * @return 찾은 인덱스. 찾지 못하면 -1을 반환합니다.
     */
    private static int findAppIndex(String appId, List<String> appIds) {
        for (int i=0; i < appIds.size(); i++) {
            if (appIds.get(i).equals(appId)) {
                return i;
            }
        }
        return -1;
    }
}