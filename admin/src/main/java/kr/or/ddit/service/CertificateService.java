package kr.or.ddit.service;

import java.util.Map;

import kr.or.ddit.finalProject.dto.certificate.CertificateIssueDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 증명서 발급 서비스
 * · 직원 셀프 신청·발급·조회·출력(1회) + 행정직원 모니터링
 */
public interface CertificateService {

    /** 신청 즉시 자동발급 후 발급 일련번호 반환 */
    Long issue(CertificateIssueDto dto);

    /** 본인 발급 이력 (서버사이드 페이징) */
    PageResponse<CertificateIssueDto> searchMyList(PaginationInfo<Map<String, Object>> paging);

    /**
     * 출력 처리 (1회 제한) 후 출력용 증명서 데이터 반환.
     * 이미 출력된 건이거나 본인 소유가 아니면 IllegalStateException.
     */
    CertificateIssueDto print(Long certSn, String userId);

    /** 전체 발급 이력 (행정직원 모니터링 · 서버사이드 페이징) */
    PageResponse<CertificateIssueDto> searchAllList(PaginationInfo<Map<String, Object>> paging);
}
