package kr.or.ddit.finalProject.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;

/**
 * 객체를 JSON 형태로 예쁘게 출력하는 유틸리티 클래스
 * 주로 디버깅이나 로깅 목적으로 사용되며, 객체의 내용을 쉽게 확인할 수 있도록 도와줌
 * JSON 변환 과정에서 발생할 수 있는 예외를 FinalProjectException으로 래핑하여 처리
 */
public class PrintPrettyObject {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 객체를 JSON 형태로 예쁘게 반환하는 메서드
     * 
     * @param obj 출력할 객체 
     * @return 객체를 JSON 형태로 예쁘게 표현한 문자열
     * @throws FinalProjectException JSON 처리 과정에서 예외가 발생할 경우 래핑하여 던짐
     */
    public static String toPrettyString(Object obj) {
        if (obj == null) {
            return "null";
        }
        try {
            return objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(obj);
        } catch (JsonProcessingException e) {
            throw new FinalProjectException(ErrorCode.JSON_PROCESSING_FAILED, e);

        }
    }
}
