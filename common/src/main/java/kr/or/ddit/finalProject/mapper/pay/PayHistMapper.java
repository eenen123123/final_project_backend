package kr.or.ddit.finalProject.mapper.pay;

import org.apache.ibatis.annotations.Mapper;

import kr.or.ddit.finalProject.dto.pay.PayHistDto;

@Mapper
public interface PayHistMapper {

    int insertPayHist(PayHistDto payHistDto);
}
