package kuke.board.common.snowflake;

import java.util.random.RandomGenerator;

/**
 * 트위터의 Snowflake 알고리즘을 구현하여 분산 환경에서 고유한 64비트 ID를 생성하는 클래스입니다.
 * ID는 다음과 같은 구조로 구성됩니다:
 *
 * | 1 bit (unused) | 41 bits (timestamp) | 10 bits (node id) | 12 bits (sequence) |
 */
public class Snowflake {
	// 비트 할당
	private static final int UNUSED_BITS = 1; // 첫 비트는 부호 비트로 사용하지 않음
	private static final int EPOCH_BITS = 41; // 타임스탬프(밀리초)를 위한 비트 수
	private static final int NODE_ID_BITS = 10; // 노드(서버) ID를 위한 비트 수 (0-1023)
	private static final int SEQUENCE_BITS = 12; // 동일 시간 내 순서를 위한 시퀀스 비트 수 (0-4095)

	// 각 부분의 최대값 계산
	private static final long maxNodeId = (1L << NODE_ID_BITS) - 1;
	private static final long maxSequence = (1L << SEQUENCE_BITS) - 1;

	// 노드 ID (0 ~ 1023 사이의 임의의 값으로 설정)
	// 실제 운영 환경에서는 각 서버마다 고유한 값을 할당해야 합니다.
	private final long nodeId = RandomGenerator.getDefault().nextLong(maxNodeId + 1);

	// 기준 시간 (Epoch). 2024년 1월 1일 0시 0분 0초 (UTC)
	// 이 값을 기준으로 타임스탬프를 계산하여 ID의 유효 기간을 늘립니다.
	private final long startTimeMillis = 1704067200000L;

	private long lastTimeMillis = startTimeMillis; // 마지막으로 ID를 생성한 시간
	private long sequence = 0L; // 시퀀스 번호

	/**
	 * 다음 고유 ID를 생성합니다.
	 * 이 메서드는 여러 스레드에서 동시에 접근할 수 없도록 synchronized로 선언되었습니다.
	 * @return 생성된 64비트 고유 ID
	 */
	public synchronized long nextId() {
		long currentTimeMillis = System.currentTimeMillis();

		// 시스템 시간이 거꾸로 흐르는 경우 예외 발생
		if (currentTimeMillis < lastTimeMillis) {
			throw new IllegalStateException("Invalid Time");
		}

		// 마지막으로 ID를 생성했을 때와 같은 밀리초에 요청이 온 경우
		if (currentTimeMillis == lastTimeMillis) {
			// 시퀀스 번호를 1 증가시키고, maxSequence로 나눈 나머지 값을 사용 (오버플로우 방지)
			sequence = (sequence + 1) & maxSequence;
			// 시퀀스가 오버플로우된 경우 (같은 밀리초에 4096개 이상 생성)
			if (sequence == 0) {
				// 다음 밀리초가 될 때까지 기다립니다.
				currentTimeMillis = waitNextMillis(currentTimeMillis);
			}
		} else {
			// 다른 밀리초에 요청이 온 경우, 시퀀스를 0으로 리셋
			sequence = 0;
		}

		// 마지막 생성 시간을 현재 시간으로 업데이트
		lastTimeMillis = currentTimeMillis;

		// 각 부분을 비트 연산을 통해 조합하여 최종 ID를 생성합니다.
		return ((currentTimeMillis - startTimeMillis) << (NODE_ID_BITS + SEQUENCE_BITS)) // 1. 타임스탬프 부분
			| (nodeId << SEQUENCE_BITS) // 2. 노드 ID 부분
			| sequence; // 3. 시퀀스 부분
	}

	/**
	 * 현재 시간(밀리초)이 마지막으로 ID를 생성한 시간보다 커질 때까지 기다립니다.
	 * @param currentTimestamp 현재 타임스탬프
	 * @return 다음 밀리초의 타임스탬프
	 */
	private long waitNextMillis(long currentTimestamp) {
		while (currentTimestamp <= lastTimeMillis) {
			currentTimestamp = System.currentTimeMillis();
		}
		return currentTimestamp;
	}
}