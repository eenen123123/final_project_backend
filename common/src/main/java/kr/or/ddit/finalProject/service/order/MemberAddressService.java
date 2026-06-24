package kr.or.ddit.finalProject.service.order;

import java.util.List;
import kr.or.ddit.finalProject.dto.order.MemberAddressDto;

public interface MemberAddressService {
    // 배송지 등록
    void registerAddress(MemberAddressDto memberAddressDto);
    
    // 배송지 수정
    void modifyAddress(MemberAddressDto memberAddressDto);

    // 배송지 제거
    void removeAddress(Long addressSn, String userId);

    // 회원별 배송지 목록 조회
    List<MemberAddressDto> getAddressListByUserId(String userId);

    // 배송지 상세 조회 (소유자 검증 포함)
    MemberAddressDto getAddressByAddressSn(Long addressSn, String userId);

    // 기본 배송지 변경
    void changeDefaultAddress(String userId, Long addressSn);

}
