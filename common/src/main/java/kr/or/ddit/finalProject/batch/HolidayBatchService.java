package kr.or.ddit.finalProject.batch;

import java.net.URI;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import kr.or.ddit.finalProject.dto.calendar.CalendarEventDto;
import kr.or.ddit.finalProject.mapper.calendar.CalendarEventMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class HolidayBatchService {

    @Value("${public.api.holiday-key}")
    private String apiKey;

    private final CalendarEventMapper calendarEventMapper;
    private final ObjectMapper objectMapper = new ObjectMapper();

    private static final String BASE_URL =
            "http://apis.data.go.kr/B090041/openapi/service/SpcdeInfoService/getHoliDeInfo";

    public void fetchAndSaveHolidays(int year) {
        List<CalendarEventDto> holidays = new ArrayList<>();

        for (int month = 1; month <= 12; month++) {
            try {
                URI uri = UriComponentsBuilder.fromHttpUrl(BASE_URL)
                        .queryParam("serviceKey", apiKey).queryParam("solYear", year)
                        .queryParam("solMonth", String.format("%02d", month))
                        .queryParam("numOfRows", 30).queryParam("pageNo", 1)
                        .queryParam("_type", "json") // JSON 응답 요청
                        .build(true).toUri();

                RestTemplate restTemplate = new RestTemplate();
                String response = restTemplate.getForObject(uri, String.class);

                JsonNode root = objectMapper.readTree(response);
                JsonNode body = root.path("response").path("body");
                int totalCount = body.path("totalCount").asInt();

                if (totalCount == 0)
                    continue;

                JsonNode items = body.path("items").path("item");

                // 1개일 때는 객체, 여러 개일 때는 배열로 오는 경우 처리
                if (items.isObject()) {
                    parseItem(items, holidays);
                } else if (items.isArray()) {
                    for (JsonNode item : items) {
                        parseItem(item, holidays);
                    }
                }

            } catch (Exception e) {
                log.error("[HolidayBatch] {}년 {}월 공휴일 조회 실패: {}", year, month, e.getMessage());
            }
        }

        // 기존 holiday 데이터 삭제 후 새로 저장
        calendarEventMapper.deleteCalendarEventByTypeAndYear("holiday", year);
        for (CalendarEventDto dto : holidays) {
            calendarEventMapper.insertCalendarEvent(dto);
        }

        log.info("[HolidayBatch] {}년 공휴일 {}건 저장 완료", year, holidays.size());
    }

    private void parseItem(JsonNode item, List<CalendarEventDto> holidays) {
        String dateName = item.path("dateName").asText();
        String isHoliday = item.path("isHoliday").asText();
        String locdate = item.path("locdate").asText();

        if (!"Y".equals(isHoliday))
            return;
        if (locdate.length() != 8)
            return;

        String dateStr = locdate.substring(0, 4) + "-" + locdate.substring(4, 6) + "-"
                + locdate.substring(6, 8);

        CalendarEventDto dto = CalendarEventDto.builder().eventType("holiday").eventTitle(dateName)
                .startDt(LocalDate.parse(dateStr)).endDt(LocalDate.parse(dateStr))
                .regUserId("SYSTEM").build();

        holidays.add(dto);
    }
}
