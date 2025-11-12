package kuke.board.hotarticle.utils;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 시간 계산 관련 유틸리티 메서드를 제공하는 클래스입니다.
 */
public class TimeCalculatorUtils {

    /**
     * 현재 시간으로부터 다음 날 자정까지 남은 시간을 계산합니다.
     * 이 메서드는 Redis 키의 TTL(Time-To-Live)을 설정하여, 데이터가
     * 매일 자정에 자동으로 만료되도록 하는 데 사용됩니다.
     *
     * @return 현재 시간부터 자정까지의 Duration 객체
     */
    public static Duration calculateDurationToMidnight() {
        LocalDateTime now = LocalDateTime.now();
        // 내일(now.plusDays(1))의 시간 부분을 자정(LocalTime.MIDNIGHT)으로 설정합니다.
        LocalDateTime midnight = now.plusDays(1).with(LocalTime.MIDNIGHT);
        return Duration.between(now, midnight);
    }
}