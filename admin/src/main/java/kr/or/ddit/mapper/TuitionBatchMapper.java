package kr.or.ddit.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 원비 청구 배치 매퍼
 * - 결제일 3일 전 월 청구 선생성
 * - 결제일 경과 미납 → 연체 전환
 * - 누적 미납 N회 이상 → 블랙리스트 자동 등록
 */
@Mapper
public interface TuitionBatchMapper {

    /** 결제일(가입일 기준, 말일 보정) 3일 이내 도래 학생의 당월 청구를 '미납'으로 선생성.
     *  청구액 = 활동 등록 강좌(ENRL_STAT_CD='01')의 COURSE_PRICE 합계. (반환: 생성 건수) */
    int generateMonthlyBills();

    /** 결제일(DUE_DT) 경과한 미납(01) 청구를 연체(02)로 전환. (반환: 전환 건수) */
    int markOverdueBills();

    /** 누적 미납/연체 threshold회 이상 학생을 블랙리스트 이력에 기록 (미등록자만). */
    int registerBlacklistHistory(@Param("threshold") int threshold);

    /** 누적 미납/연체 threshold회 이상 학생을 블랙리스트에 등록 (미등록자만). (반환: 등록 건수) */
    int registerBlacklist(@Param("threshold") int threshold);
}
