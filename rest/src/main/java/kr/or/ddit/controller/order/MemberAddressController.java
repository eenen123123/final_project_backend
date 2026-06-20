package kr.or.ddit.controller.order;

import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import kr.or.ddit.finalProject.dto.order.MemberAddressDto;
import kr.or.ddit.finalProject.service.order.MemberAddressService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/addresses")
public class MemberAddressController {
    
    private final MemberAddressService memberAddressService;

    // 배송지 목록 조회
    @GetMapping
    public ResponseEntity<List<MemberAddressDto>> getMyAddressList(Authentication authentication){
        String userId = authentication.getName();
        log.info("배송지 목록 조회 요청 - user: {}", userId);

        List<MemberAddressDto> list = memberAddressService.getAddressListByUserId(userId);
        return ResponseEntity.ok(list);
    }

    // 배송지 단건 상세 조회
    @GetMapping("/detail")
    public ResponseEntity<MemberAddressDto> getAddressDetail(
            @RequestParam("addressSn") Long addressSn, Authentication authentication) {

        String userId = authentication.getName();
        log.info("배송지 상세 조회 요청 - addressSn: {}, user: {}", addressSn, userId);
        return ResponseEntity.ok(memberAddressService.getAddressByAddressSn(addressSn, userId));
    }

    // 배송지 신규 등록
    @PostMapping
    public ResponseEntity<String> registerAddress(
            Authentication authentication,
            @RequestBody MemberAddressDto memberAddressDto) {
        memberAddressDto.setUserId(authentication.getName());
        log.info("배송지 등록 요청 - user: {}, addressNm: {}", memberAddressDto.getUserId(), memberAddressDto.getAddressNm());
        memberAddressService.registerAddress(memberAddressDto);
        return ResponseEntity.ok("SUCCESS");
    }
    
    // 배송지 정보 수정
    @PostMapping("/{addressSn}")
    public ResponseEntity<String> modifyAddress(
           @PathVariable("addressSn") Long addressSn,
           Authentication authentication,
           @RequestBody MemberAddressDto memberAddressDto){
        
        memberAddressDto.setAddressSn(addressSn);
        memberAddressDto.setUserId(authentication.getName());
        log.info("배송지 수정 요청 - addressSn: {}, user: {}", addressSn, authentication.getName());
    
        memberAddressService.modifyAddress(memberAddressDto);
        return ResponseEntity.ok("SUCCESS");
    }

    // 배송지 삭제
    @DeleteMapping("/{addressSn}")
    public ResponseEntity<String> removeAddress(
            @PathVariable Long addressSn,
            Authentication authentication) {

        String userId = authentication.getName();
        log.info("배송지 삭제 - addressSn: {}, user: {}", addressSn, userId);
        memberAddressService.removeAddress(addressSn, userId);
        return ResponseEntity.ok("SUCCESS");
    }

    // 기본 배송지 강제 변경 (목록에서 '기본 배송지 설정' 버튼 클릭 시)
    @PostMapping("/{addressSn}/default")
    public ResponseEntity<String> changeDefaultAddress(
            @PathVariable("addressSn") Long addressSn,
            Authentication authentication) {

        String userId = authentication.getName();
        log.info("기본 배송지 변경 - userId: {}, targetAddressSn: {}", userId, addressSn);

        memberAddressService.changeDefaultAddress(userId, addressSn);
        return ResponseEntity.ok("SUCCESS");
    }

}
