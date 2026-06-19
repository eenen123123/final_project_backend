package kr.or.ddit.mapper;

import java.util.List;
import java.util.Map;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.certificate.CertificateIssueDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

/**
 * 증명서 발급 Mapper
 * · 직원 셀프 신청·발급·조회·출력(1회) + 행정직원 모니터링
 */
@Mapper
public interface CertificateMapper {

    /** 신청 즉시 자동발급 (PRN_YN='N') */
    int insertCertificate(CertificateIssueDto dto);

    /** 단건 조회 (출력용 · 직원 정보 자동 조인) */
    CertificateIssueDto selectByCertSn(@Param("certSn") Long certSn);

    /** 본인 발급 이력 (서버사이드 페이징) */
    List<CertificateIssueDto> selectMyList(PaginationInfo<Map<String, Object>> paging);
    int countMyList(PaginationInfo<Map<String, Object>> paging);

    /** 출력 처리 (1회 제한) · 미출력(N) 건만 Y 로 변경, 변경 행수 반환(0=이미 출력) */
    int markPrinted(@Param("certSn") Long certSn, @Param("userId") String userId);

    /** 전체 발급 이력 (행정직원 모니터링 · 서버사이드 페이징) */
    List<CertificateIssueDto> selectAllList(PaginationInfo<Map<String, Object>> paging);
    int countAllList(PaginationInfo<Map<String, Object>> paging);
}
