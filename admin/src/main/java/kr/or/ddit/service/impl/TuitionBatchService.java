package kr.or.ddit.service.impl;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.mapper.TuitionBatchMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 원비 청구 배치 서비스
 * 1) 결제일 3일 전 월 청구 선생성(미납)
 * 2) 결제일 경과 미납 → 연체 전환
 * 3) 누적 미납 3회 이상 → 블랙리스트 자동 등록
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TuitionBatchService {

    private final TuitionBatchMapper mapper;

    /** 블랙리스트 자동 등록 임계값(누적 미납 회차) */
    private static final int BLACKLIST_THRESHOLD = 3;

    @Transactional
    public Map<String, Object> runDailyBatch() {
        int generated   = mapper.generateMonthlyBills();
        int overdue     = mapper.markOverdueBills();
        mapper.registerBlacklistHistory(BLACKLIST_THRESHOLD);
        int blacklisted = mapper.registerBlacklist(BLACKLIST_THRESHOLD);

        log.info("[원비배치] 청구생성 {}건 · 연체전환 {}건 · 블랙리스트 등록 {}건",
                generated, overdue, blacklisted);

        Map<String, Object> result = new HashMap<>();
        result.put("generated", generated);
        result.put("overdue", overdue);
        result.put("blacklisted", blacklisted);
        return result;
    }
}
