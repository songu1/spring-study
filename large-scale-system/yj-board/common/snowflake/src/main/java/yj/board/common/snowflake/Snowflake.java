package yj.board.common.snowflake;

import java.util.random.RandomGenerator;

public class Snowflake {
	private static final int UNUSED_BITS = 1;
	private static final int EPOCH_BITS = 41;		// 41bit 타임스탬프로 순차성 보장
	private static final int NODE_ID_BITS = 10;		// 동일한 시간 내에서도 unique를 보장
	private static final int SEQUENCE_BITS = 12;	// 동일한 시간 내에서도 unique를 보장

	private static final long maxNodeId = (1L << NODE_ID_BITS) - 1;
	private static final long maxSequence = (1L << SEQUENCE_BITS) - 1;

	private final long nodeId = RandomGenerator.getDefault().nextLong(maxNodeId + 1);
	// UTC = 2024-01-01T00:00:00Z
	private final long startTimeMillis = 1704067200000L;

	private long lastTimeMillis = startTimeMillis;
	private long sequence = 0L;

	/* 키를 생성 (동시성 문제를 막음) */
	public synchronized long nextId() {
		long currentTimeMillis = System.currentTimeMillis();

		if (currentTimeMillis < lastTimeMillis) {
			throw new IllegalStateException("Invalid Time");
		}

		if (currentTimeMillis == lastTimeMillis) {
			sequence = (sequence + 1) & maxSequence;
			if (sequence == 0) {
				currentTimeMillis = waitNextMillis(currentTimeMillis);
			}
		} else {
			sequence = 0;
		}

		lastTimeMillis = currentTimeMillis;

		// timestamp bit + NODE_ID_BITS + SEQUENCE_BITS 로 하나의 id를 생성
		return ((currentTimeMillis - startTimeMillis) << (NODE_ID_BITS + SEQUENCE_BITS))
			| (nodeId << SEQUENCE_BITS)
			| sequence;
	}

	private long waitNextMillis(long currentTimestamp) {
		while (currentTimestamp <= lastTimeMillis) {
			currentTimestamp = System.currentTimeMillis();
		}
		return currentTimestamp;
	}
}
