package kr.or.ddit.finalProject.mapper.pay;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.pay.PayHistDto;

@Mapper
public interface PayHistMapper {

    int insertPayHist(PayHistDto payHistDto);

    PayHistDto selectPayHistByOrdSn(@Param("ordSn") Long ordSn);
}
