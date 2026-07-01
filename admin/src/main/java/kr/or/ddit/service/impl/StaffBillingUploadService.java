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
import kr.or.ddit.finalProject.dto.tuition.TuitionPaymentDto;
import kr.or.ddit.mapper.StaffBillingMapper;
import kr.or.ddit.service.CommonCodeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 외부 결제 프로그램 수납 Excel 업로드 적재 서비스
 * - 헤더명 기반 파싱(컬럼 순서 무관): 결제ID, 학생ID, 항목, 금액, 결제수단, 할부, 상태, 수납일시
 * - 결제ID(EXT_PAY_ID) 중복 행은 스킵(멱등)
 * - TUITION_PAYMENT 적재 후 같은 학생·금액의 미납 청구를 완료로 자동 정산
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StaffBillingUploadService {

    private final StaffBillingMapper mapper;
    private final CommonCodeService commonCodeService;

    private static final String CL_ITEM   = "222";
    private static final String CL_STATUS = "225";

    private static final DateTimeFormatter[] DT_FORMATS = {
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss")
    };

    @Transactional
    public Map<String, Object> upload(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드된 파일이 없습니다.");
        }

        // 공통코드 이름→코드 매핑 (항목 222 / 상태 225)
        Map<String, String> itemMap   = nameToCode(CL_ITEM);
        Map<String, String> statusMap = nameToCode(CL_STATUS);

        int inserted = 0, skipped = 0, failed = 0;
        List<String> errors = new ArrayList<>();

        try (Workbook wb = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = wb.getSheetAt(0);
            Row header = sheet.getRow(sheet.getFirstRowNum());
            if (header == null) throw new IllegalArgumentException("빈 시트입니다.");

            DataFormatter fmt = new DataFormatter();
            Map<String, Integer> col = headerIndex(header, fmt);
            requireHeaders(col, "결제ID", "학생ID", "항목", "금액", "결제수단", "상태", "수납일시");

            for (int r = header.getRowNum() + 1; r <= sheet.getLastRowNum(); r++) {
                Row row = sheet.getRow(r);
                if (row == null) continue;

                String extPayId = str(row, col, "결제ID", fmt);
                if (extPayId.isBlank()) continue;          // 빈 행 스킵

                try {
                    if (mapper.existsByExtPayId(extPayId) > 0) { skipped++; continue; }

                    String userId  = str(row, col, "학생ID", fmt);
                    String itemNm  = str(row, col, "항목", fmt);
                    String itemCd  = itemMap.get(itemNm);
                    if (itemCd == null) throw new IllegalArgumentException("알 수 없는 항목 '" + itemNm + "'");

                    long amt = (long) num(row, col, "금액", fmt);

                    // 결제수단: kakaopay→02, tosspay→03, 그 외(카드사명)→01(카드)+카드사명
                    String mr = str(row, col, "결제수단", fmt);
                    String mthdCd; String cardNm = null;
                    if (mr.equalsIgnoreCase("kakaopay") || mr.equals("카카오페이")) {
                        mthdCd = "02";
                    } else if (mr.equalsIgnoreCase("tosspay") || mr.equals("토스페이")) {
                        mthdCd = "03";
                    } else {
                        mthdCd = "01";
                        cardNm = mr.isBlank() ? null : mr;
                    }

                    int install = col.containsKey("할부") ? (int) num(row, col, "할부", fmt) : 0;

                    String statusNm = str(row, col, "상태", fmt);
                    String statCd   = statusMap.getOrDefault(statusNm, "01");

                    LocalDateTime payDt = parseDateTime(row, col, "수납일시", fmt);

                    TuitionPaymentDto p = new TuitionPaymentDto();
                    p.setUserId(userId);
                    p.setExtPayId(extPayId);
                    p.setPayItemCd(itemCd);
                    p.setPayAmt(amt);
                    p.setPayMthdCd(mthdCd);
                    p.setCardNm(cardNm);
                    p.setInstallmentMm(install);
                    p.setPayStatCd(statCd);
                    p.setPayDt(payDt);

                    mapper.insertPayment(p);
                    // 결제 완료(01)인 경우에만 같은 학생·금액의 미납 청구를 정산 (미완료는 정산 안 함)
                    if ("01".equals(statCd)) {
                        Long paySn = mapper.selectPaySnByExtPayId(extPayId);
                        mapper.settleMatchingBill(userId, paySn, amt);
                    }
                    inserted++;
                } catch (Exception e) {
                    failed++;
                    errors.add((r + 1) + "행: " + e.getMessage());
                }
            }
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (Exception e) {
            log.error("Excel 업로드 처리 실패", e);
            throw new IllegalStateException("파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
        }

        Map<String, Object> result = new HashMap<>();
        result.put("inserted", inserted);
        result.put("skipped", skipped);
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

    private double num(Row row, Map<String, Integer> col, String name, DataFormatter fmt) {
        Cell cell = cellOf(row, col, name);
        if (cell == null) return 0;
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC) {
            return cell.getNumericCellValue();
        }
        String s = fmt.formatCellValue(cell).replaceAll("[,\\s원]", "");
        if (s.isEmpty()) return 0;
        return Double.parseDouble(s);
    }

    private LocalDateTime parseDateTime(Row row, Map<String, Integer> col, String name, DataFormatter fmt) {
        Cell cell = cellOf(row, col, name);
        if (cell == null) throw new IllegalArgumentException("수납일시가 비어 있습니다.");
        if (cell.getCellType() == org.apache.poi.ss.usermodel.CellType.NUMERIC
                && DateUtil.isCellDateFormatted(cell)) {
            return cell.getLocalDateTimeCellValue();
        }
        String s = fmt.formatCellValue(cell).trim();
        for (DateTimeFormatter f : DT_FORMATS) {
            try { return LocalDateTime.parse(s, f); } catch (Exception ignore) { }
        }
        try { return LocalDate.parse(s).atStartOfDay(); } catch (Exception ignore) { }
        throw new IllegalArgumentException("수납일시 형식 오류 '" + s + "' (yyyy-MM-dd HH:mm:ss 필요)");
    }
}
