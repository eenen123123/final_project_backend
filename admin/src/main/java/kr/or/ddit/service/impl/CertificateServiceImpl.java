package kr.or.ddit.service.impl;

import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.certificate.CertificateIssueDto;
import kr.or.ddit.finalProject.dto.common.PageResponse;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import kr.or.ddit.mapper.CertificateMapper;
import kr.or.ddit.service.CertificateService;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CertificateServiceImpl implements CertificateService {

    private final CertificateMapper mapper;

    @Override
    @Transactional
    public Long issue(CertificateIssueDto dto) {
        mapper.insertCertificate(dto);
        return dto.getCertSn();
    }

    @Override
    public PageResponse<CertificateIssueDto> searchMyList(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.selectMyList(paging), mapper.countMyList(paging));
    }

    @Override
    @Transactional
    public CertificateIssueDto print(Long certSn, String userId) {
        int updated = mapper.markPrinted(certSn, userId);
        if (updated == 0) {
            throw new IllegalStateException("이미 출력된 증명서이거나 출력 권한이 없습니다.");
        }
        return mapper.selectByCertSn(certSn);
    }

    @Override
    public PageResponse<CertificateIssueDto> searchAllList(PaginationInfo<Map<String, Object>> paging) {
        return new PageResponse<>(mapper.selectAllList(paging), mapper.countAllList(paging));
    }
}
