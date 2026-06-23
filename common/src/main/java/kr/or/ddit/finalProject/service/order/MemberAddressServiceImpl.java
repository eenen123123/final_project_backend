package kr.or.ddit.finalProject.service.order;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.order.MemberAddressDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.order.MemberAddressMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MemberAddressServiceImpl implements MemberAddressService {

    private final MemberAddressMapper memberAddressMapper;

    @Override
    @Transactional
    public void registerAddress(MemberAddressDto dto) {
        validateRequiredFields(dto);

        // 첫 번째 주소 등록 시 자동으로 기본 배송지 설정
        List<MemberAddressDto> existing = memberAddressMapper.selectAddressListByUserId(dto.getUserId());
        if (existing.isEmpty()) {
            dto.setDefaultYn("Y");
        }

        if ("Y".equals(dto.getDefaultYn())) {
            memberAddressMapper.updateResetDefaultAddress(dto.getUserId());
        }
        memberAddressMapper.insertMemberAddress(dto);
        log.info("배송지 등록 완료 - userId: {}, addressNm: {}", dto.getUserId(), dto.getAddressNm());
    }

    @Override
    @Transactional
    public void modifyAddress(MemberAddressDto dto) {
        validateRequiredFields(dto);

        if ("Y".equals(dto.getDefaultYn())) {
            memberAddressMapper.updateResetDefaultAddress(dto.getUserId());
        }
        int result = memberAddressMapper.updateAddress(dto);
        if (result == 0) {
            log.warn("배송지 수정 실패 (없거나 권한 없음) - addressSn: {}, userId: {}", dto.getAddressSn(), dto.getUserId());
            throw new FinalProjectException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        log.info("배송지 수정 완료 - addressSn: {}", dto.getAddressSn());
    }

    @Override
    @Transactional
    public void removeAddress(Long addressSn, String userId) {
        int result = memberAddressMapper.deleteAddress(addressSn, userId);
        if (result == 0) {
            log.warn("배송지 삭제 실패 (없거나 권한 없음) - addressSn: {}, userId: {}", addressSn, userId);
            throw new FinalProjectException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        log.info("배송지 삭제 완료 - addressSn: {}", addressSn);
    }

    @Override
    public List<MemberAddressDto> getAddressListByUserId(String userId) {
        return memberAddressMapper.selectAddressListByUserId(userId);
    }

    @Override
    public MemberAddressDto getAddressByAddressSn(Long addressSn, String userId) {
        MemberAddressDto dto = memberAddressMapper.selectAddressByAddressSn(addressSn);
        if (dto == null) {
            throw new FinalProjectException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!dto.getUserId().equals(userId)) {
            throw new FinalProjectException(ErrorCode.ADDRESS_ACCESS_DENIED);
        }
        return dto;
    }

    @Override
    @Transactional
    public void changeDefaultAddress(String userId, Long addressSn) {
        // 대상 주소가 본인 소유인지 확인
        MemberAddressDto target = memberAddressMapper.selectAddressByAddressSn(addressSn);
        if (target == null) {
            throw new FinalProjectException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        if (!target.getUserId().equals(userId)) {
            throw new FinalProjectException(ErrorCode.ADDRESS_ACCESS_DENIED);
        }

        memberAddressMapper.updateResetDefaultAddress(userId);
        int result = memberAddressMapper.updateSetDefaultAddress(addressSn, userId);
        if (result == 0) {
            throw new FinalProjectException(ErrorCode.ADDRESS_NOT_FOUND);
        }
        log.info("기본 배송지 변경 완료 - userId: {}, addressSn: {}", userId, addressSn);
    }

    private void validateRequiredFields(MemberAddressDto dto) {
        if (dto.getAddressNm() != null && dto.getAddressNm().length() > 10) {
            throw new FinalProjectException(ErrorCode.ADDRESS_NAME_TOO_LONG);
        }
        if (dto.getReceiverNm() == null || dto.getReceiverNm().isBlank()) {
            throw new FinalProjectException(ErrorCode.ADDRESS_RECEIVER_NAME_REQUIRED);
        }
        if (dto.getReceiverTel() == null || dto.getReceiverTel().isBlank()) {
            throw new FinalProjectException(ErrorCode.ADDRESS_RECEIVER_TEL_REQUIRED);
        }
    }
}
