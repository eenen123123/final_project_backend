package kr.or.ddit.finalProject.service.staff;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.dto.student.StudentBlackListDto;
import kr.or.ddit.finalProject.dto.student.StudentBlackListHistoryDto;
import kr.or.ddit.finalProject.mapper.BlacklistMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlacklistServiceImpl implements BlacklistService {

    private final BlacklistMapper blacklistMapper;

    /* 이벤트 유형 서버 상수 (BLKLST_EVT_CD) */
    private static final String EVT_WARN    = "WARN"; // 1회: 경고
    private static final String EVT_SUSPEND = "SUSP"; // 2회: 기간정지
    private static final String EVT_PERM    = "PERM"; // 3회+: 영구정지
    private static final String EVT_MODIFY  = "MOD";  // 수정
    private static final String EVT_RESOLVE = "REL";  // 해제

    /* 위험 등급 코드 (cl 700) */
    private static final String LVL_OBSERVE = "02";   // 관찰(경고)
    private static final String LVL_HIGH    = "01";   // 고위험(정지)

    /* 2회차 기간정지 일수 */
    private static final int SUSPEND_DAYS_2ND = 7;

    @Override
    public PageResponse<StudentBlackListDto> searchBlacklist(PaginationInfo<Map<String, Object>> paging) {
        List<StudentBlackListDto> items = blacklistMapper.searchBlacklist(paging);
        int totalCount = blacklistMapper.countBlacklist(paging);
        return new PageResponse<>(items, totalCount);
    }

    @Override
    public Map<String, Object> getSummary() {
        return blacklistMapper.selectBlacklistSummary();
    }

    @Override
    public StudentBlackListDto getDetail(String stdUserId) {
        return blacklistMapper.selectBlacklistDetail(stdUserId);
    }

    @Override
    public List<StudentBlackListHistoryDto> getHistory(String stdUserId) {
        return blacklistMapper.selectBlacklistHistory(stdUserId);
    }

    @Override
    @Transactional
    public Map<String, Object> registerBlacklist(StudentBlackListDto dto, String loginUserId) {
        // 누적 위반 횟수로 페널티 자동 결정 (1회 경고 / 2회 기간정지 / 3회+ 영구정지)
        int count = blacklistMapper.countOffenses(dto.getStdUserId()) + 1;
        String lvl, evt;
        int days;
        if (Boolean.TRUE.equals(dto.getForcePermanent())) {
            // 관리자 직접 지정: 누적 무시하고 즉시 영구정지
            lvl = LVL_HIGH; days = 0; evt = EVT_PERM;
        }
        else if (count <= 1)   { lvl = LVL_OBSERVE; days = 0;                 evt = EVT_WARN; }
        else if (count == 2)   { lvl = LVL_HIGH;    days = SUSPEND_DAYS_2ND;  evt = EVT_SUSPEND; }
        else                   { lvl = LVL_HIGH;    days = 0;                 evt = EVT_PERM; }

        dto.setBlklstLvlCd(lvl);
        dto.setBlklstImpsDaysCnt(days);

        boolean exists = blacklistMapper.existsBlacklist(dto.getStdUserId()) > 0;
        if (exists) blacklistMapper.reactivateBlacklist(dto);
        else        blacklistMapper.insertBlacklist(dto);

        insertHistory(dto.getStdUserId(), dto.getBlklstRsnCn(), days, loginUserId, evt);

        boolean blocked   = LVL_HIGH.equals(lvl);
        boolean permanent = blocked && days == 0;
        log.info("[registerBlacklist] stdUserId={}, count={}, evt={}, blocked={}, by={}",
                dto.getStdUserId(), count, evt, blocked, loginUserId);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("offenseCount", count);
        result.put("levelCd", lvl);
        result.put("impsDays", days);
        result.put("blocked", blocked);
        result.put("permanent", permanent);
        return result;
    }

    @Override
    @Transactional
    public void updateBlacklist(StudentBlackListDto dto, String loginUserId) {
        blacklistMapper.updateBlacklist(dto); // 속성만 수정 (날짜·로그인 상태 불변)
        insertHistory(dto.getStdUserId(), dto.getBlklstRsnCn(), null, loginUserId, EVT_MODIFY);
        log.info("[updateBlacklist] stdUserId={}, by={}", dto.getStdUserId(), loginUserId);
    }

    @Override
    @Transactional
    public void resolveBlacklist(String stdUserId, String loginUserId) {
        blacklistMapper.resolveBlacklist(stdUserId);
        insertHistory(stdUserId, "주의 목록에서 해제 처리", null, loginUserId, EVT_RESOLVE);
        log.info("[resolveBlacklist] stdUserId={}, by={}", stdUserId, loginUserId);
    }

    @Override
    public boolean isLoginBlocked(String userId) {
        return blacklistMapper.countActiveBlock(userId) > 0;
    }

    /** 이력 1행 적재 공통 */
    private void insertHistory(String stdUserId, String reason, Integer impsDays, String loginUserId, String evtCd) {
        StudentBlackListHistoryDto hist = StudentBlackListHistoryDto.builder()
                .blklstStdUserId(stdUserId)
                .blklstRsnCn(reason)
                .blklstImpsDaysCnt(impsDays)
                .blklstRgtrUserId(loginUserId)
                .blklstEvtCd(evtCd)
                .build();
        blacklistMapper.insertBlacklistHistory(hist);
    }
}
