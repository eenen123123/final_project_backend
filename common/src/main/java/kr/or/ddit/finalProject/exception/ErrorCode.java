package kr.or.ddit.finalProject.exception;

import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전반에서 발생할 수 있는 다양한 예외 상황을 정의하는 열거형 클래스
 * 각 예외 상황에 대한 고유한 코드와 메시지를 포함하여, 예외 처리 시 일관된 방식으로 사용할 수 있음
 */
public enum ErrorCode {

    // 요청 관련
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),

    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),

    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용중인 아이디입니다."),

    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),

    USERNAME_OR_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),

    ACCOUNT_UNUSABLE(HttpStatus.FORBIDDEN, "사용할 수 없는 계정입니다."),

    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "유효하지 않은 토큰입니다."),

    USER_ID_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 존재하는 사용자 ID입니다."),

    EMAIL_NOT_VERIFIED(HttpStatus.BAD_REQUEST, "이메일 인증이 완료되지 않았습니다."),

    SIGNUP_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "회원 가입에 실패했습니다."),

    BIRTHDATE_ENRNO_MISMATCH(HttpStatus.BAD_REQUEST, "생년월일과 주민등록번호가 일치하지 않습니다."),

    GENDER_MISMATCH(HttpStatus.BAD_REQUEST, "성별과 주민등록번호의 성별 정보가 일치하지 않습니다."),
    // 채팅 관련

    CHAT_ROOM_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "채팅방 생성에 실패했습니다."),

    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "채팅방을 찾을 수 없습니다."),

    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "채팅방에 접근할 권한이 없습니다."),

    CHAT_MESSAGE_CREATION_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "채팅 메시지 생성에 실패했습니다."),
    // 서버 관련
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다."),

    DOTENV_FILE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR,
            "환경 변수 파일(.env)을 찾을 수 없습니다. .env.example 파일을 복사하여 .env 파일을 생성하고, 필요한 환경 변수를 설정해주세요."),

    // File
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),

    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),

    FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),

    CANT_ACCESS_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "파일에 접근할 수 없습니다."),

    FILE_INFO_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 정보 저장에 실패했습니다."),

    FILE_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "PDF, 이미지, 동영상, Zip 파일만 업로드할 수 있습니다."),

    FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 내용을 읽는 중 오류가 발생했습니다."),


    // 결재 관련
    APPROVAL_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "결재 양식을 찾을 수 없습니다."),

    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "결재 문서를 찾을 수 없습니다."),

    APPROVAL_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결재 문서 삭제에 실패했습니다."),

    APPROVAL_NOT_DRAFT(HttpStatus.BAD_REQUEST, "결재 문서가 DRAFT 상태가 아닙니다."),

    APPROVAL_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "결재 문서에 대한 권한이 없습니다."),

    CANNOT_CANCEL_APPROVAL(HttpStatus.BAD_REQUEST, "결재 문서를 취소할 수 없습니다."),

    // 결제 관련

    // KAKAO

    KAKAO_PAY_READY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오페이 결제 준비에 실패했습니다."),

    KAKAO_PAY_APPROVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오페이 결제 승인에 실패했습니다."),

    // TOSS

    ALREADY_PROCESSED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다."),

    // UTIL
    JSON_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 처리에 실패했습니다."),

    // 직원 관련
    EMPLOYEE_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
            "직원 등록에 실패했습니다."), EMPLOYEE_ID_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR,
                    "직원 ID 생성에 실패했습니다."),;

    // =============================

    private final HttpStatus status;
    private final String message;

    ErrorCode(HttpStatus status, String message) {
        this.status = status;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }
}
