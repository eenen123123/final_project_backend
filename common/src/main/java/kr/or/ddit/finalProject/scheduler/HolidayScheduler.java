package kr.or.ddit.finalProject.scheduler;

import java.time.LocalDate;

import kr.or.ddit.finalProject.batch.HolidayBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 공휴일 배치 수동 실행 엔드포인트
 *
 * 공공데이터 API(한국천문연구원_특일 정보)를 호출하여 해당 연도의 공휴일 데이터를 CALENDAR_EVENT 테이블에 저장합니다.
 *
 * ※ 스케줄러(HolidayScheduler)가 매년 1월 1일 00:10 자동 실행하므로 데이터 누락 또는 최초 적재 시에만 사용할 것
 *
 * 사용법: GET /admin/calendar/batch/holiday/{year} 예시 : GET /admin/calendar/batch/holiday/2026
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class HolidayScheduler {

    private final HolidayBatchService holidayBatchService;

    // 매년 1월 1일 00시 10분에 실행 (서버 시작 후 자동 실행)
    @Scheduled(cron = "0 10 0 1 1 *")
    public void fetchHolidaysScheduled() {
        int year = LocalDate.now().getYear();
        log.info("[HolidayScheduler] {}년 공휴일 배치 시작", year);
        holidayBatchService.fetchAndSaveHolidays(year);
        log.info("[HolidayScheduler] {}년 공휴일 배치 완료", year);
    }

    // 다음 해 공휴일도 미리 저장 (12월 31일 23시 00분)
    @Scheduled(cron = "0 0 23 31 12 *")
    public void fetchNextYearHolidays() {
        int nextYear = LocalDate.now().getYear() + 1;
        log.info("[HolidayScheduler] {}년 공휴일 사전 배치 시작", nextYear);
        holidayBatchService.fetchAndSaveHolidays(nextYear);
        log.info("[HolidayScheduler] {}년 공휴일 사전 배치 완료", nextYear);
    }
}
