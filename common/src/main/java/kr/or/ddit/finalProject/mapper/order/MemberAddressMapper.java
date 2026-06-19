package kr.or.ddit.finalProject.mapper.order;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.order.MemberAddressDto;

@Mapper
public interface MemberAddressMapper {
    
    int insertMemberAddress(MemberAddressDto memberAddressDto);

    int updateAddress(MemberAddressDto memberAddressDto);

    int deleteAddress(@Param("addressSn") Long addressSn, @Param("userId") String userId);

    List<MemberAddressDto> selectAddressListByUserId(@Param("userId") String userId);
    
    MemberAddressDto selectAddressByAddressSn(@Param("addressSn") Long addressSn);

    int updateResetDefaultAddress(@Param("userId") String userId);

    int updateSetDefaultAddress(@Param("addressSn") Long addressSn, @Param("userId") String userId);
}
