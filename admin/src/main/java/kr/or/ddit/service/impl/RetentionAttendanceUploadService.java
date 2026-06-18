package kr.or.ddit.service.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.dto.student.StudentAttendanceDto;
import kr.or.ddit.mapper.RetentionMapper;
import kr.or.ddit.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 외부 근태 프로그램 출결 Excel 업로드 적재 서비스
 * - 헤더명 기반 파싱(컬럼 순서 무관): 학생ID, 날짜, 출결유형, 사유
 * - 출결유형명(출석/결석/지각/조퇴)을 공통코드 226 코드로 변환
 * - 오프라인 학생(ROLE_STUDENT)만 적재, 그 외 행은 실패로 기록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RetentionAttendanceUploadService {

    private final RetentionMapper mapper;
    private final CommonCodeService commonCodeService;

    private static final String CL_ATND_TYPE = "226";

    private static final DateTimeFormatter[] DT_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    };

    @Transactional
    public Map<String, Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }

        Map<String, String> typeMap = nameToCode(CL_ATND_TYPE); // 출석/결석/지각/조퇴 → 코드

        int inserted = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) throw new IllegalArgumentException("빈 시트입니다.");

            DataFormatter fmt = new DataFormatter();
            Map<String, Integer> col = headerIndex(header, fmt);
            requireHeaders(col, "학생ID", "날짜", "출결유형");

            for (int r = header.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String userId = str(row, col, "학생ID", fmt);
                if (userId.isBlank()) continue; // 빈 행 스킵

                try {
                    if (mapper.existsStudent(userId) == 0) {
                        throw new IllegalArgumentException("오프라인 학생이 아니거나 존재하지 않는 학생 '" + userId + "'");
                    }

                    String typeNm = str(row, col, "출결유형", fmt);
                    String typeCd = typeMap.get(typeNm);
                    if (typeCd == null) throw new IllegalArgumentException("알 수 없는 출결유형 '" + typeNm + "'");

                    LocalDateTime atndDt = parseDateTime(row, col, "날짜", fmt);
                    String note = col.containsKey("사유") ? str(row, col, "사유", fmt) : null;

                    StudentAttendanceDto dto = StudentAttendanceDto.builder()
                            .stdUserId(userId)
                            .atndTypeCd(typeCd)
                            .atndRegDt(atndDt)
                            .atndNoteCn(note == null || note.isBlank() ? null : note)
                            .build();

                    mapper.insertAttendance(dto);
                    inserted++;
                } catch (Exception e) {
                    failed++;
                    errors.add((r + 1) + "행: " + e.getMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("근태 Excel 업로드 처리 실패", e);
            throw new IllegalStateException("파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("inserted", inserted);
        result.put("failed", failed);
        result.put("errors", errors);
        return result;
    }

    // ───────────────────────────── helpers ─────────────────────────────

    private Map<String, String> nameToCode(String clCode) {
        Map<String, String> map = new HashMap<>();
        for (CommonCodeDto c : commonCodeService.getAllCodes(clCode)) {
            if (c.getComCdNm() != null) map.put(c.getComCdNm().trim(), c.getComCd().trim());
        }
        return map;
    }

    private Map<String, Integer> headerIndex(Row header, DataFormatter fmt) {
        Map<String, Integer> idx = new LinkedHashMap<>();
        for (int c = header.getFirstCellNum(); c < header.getLastCellNum(); c++) {
            Cell cell = header.getCell(c);
            if (cell == null) continue;
            String name = fmt.formatCellValue(cell).trim();
            if (!name.isEmpty()) idx.put(name, c);
        }
        return idx;
    }

    private void requireHeaders(Map<String, Integer> col, String... required) {
        List<String> missing = new ArrayList<>();
        for (String h : required) if (!col.containsKey(h)) missing.add(h);
        if (!missing.isEmpty()) {
            throw new IllegalArgumentException("필수 헤더 누락: " + String.join(", ", missing));
        }
    }

    private Cell cellOf(Row row, Map<String, Integer> col, String name) {
        Integer c = col.get(name);
        return c == null ? null : row.getCell(c);
    }

    private String str(Row row, Map<String, Integer> col, String name, DataFormatter fmt) {
        Cell cell = cellOf(row, col, name);
        return cell == null ? "" : fmt.formatCellValue(cell).trim();
    }

    private LocalDateTime parseDateTime(Row row, Map<String, Integer> col, String name, DataFormatter fmt) {
        Cell cell = cellOf(row, col, name);
        if (cell == null) throw new IllegalArgumentException("날짜가 비어 있습니다.");
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }
        String s = fmt.formatCellValue(cell).trim();
        for (DateTimeFormatter f : DT_FORMATS) {
            try { return LocalDateTime.parse(s, f); } catch (Exception ignore) { }
        }
        try { return LocalDate.parse(s).atStartOfDay(); } catch (Exception ignore) { }
        throw new IllegalArgumentException("날짜 형식 오류 '" + s + "' (yyyy-MM-dd 필요)");
    }
}
