package kr.or.ddit.finalProject.scheduler;

import java.time.LocalDate;

import kr.or.ddit.finalProject.batch.HolidayBatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

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
