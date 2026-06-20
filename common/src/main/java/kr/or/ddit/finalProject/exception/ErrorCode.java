package kr.or.ddit.finalProject.exception;

import org.springframework.http.HttpStatus;

/**
 * 프로젝트 전반에서 발생할 수 있는 다양한 예외 상황을 정의하는 열거형 클래스 각 예외 상황에 대한 고유한 코드와 메시지를 포함하여, 예외
 * 처리 시 일관된 방식으로 사용할 수
 * 있음
 */
//@formatter:off
public enum ErrorCode {

    
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "알림에 대한 접근 권한이 없습니다."),

    // 요청 관련
    NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),

    // 강좌 관련
    COURSE_NOT_FOUND(HttpStatus.NOT_FOUND, "강좌를 찾을 수 없습니다."),

    // 수능 관련
    NOT_SUPPORTED_EXAM_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 시험 유형입니다."),

    // 캘린더 관련
    CALENDAR_EVENT_NOT_FOUND(HttpStatus.NOT_FOUND, "캘린더 이벤트를 찾을 수 없습니다."),
    CALENDAR_SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "캘린더 일정을 찾을 수 없습니다."),

    INVALID_DATE_FORMAT(HttpStatus.BAD_REQUEST, "날짜 형식이 올바르지 않습니다. yyyy-MM-dd 형식으로 입력해주세요."),

    // 교재 관련
    TEXTBOOK_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 교재입니다."),
    LECTURE_NOT_FOUND(HttpStatus.NOT_FOUND, "강의를 찾을 수 없습니다."),

    // 사용자 관련
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
    USERNAME_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 사용중인 아이디입니다."),
    AUTHENTICATION_FAILED(HttpStatus.UNAUTHORIZED, "인증에 실패했습니다."),
    USERNAME_OR_PASSWORD_INCORRECT(HttpStatus.UNAUTHORIZED, "아이디 또는 비밀번호가 올바르지 않습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "비밀번호를 입력해주세요."),
    ACCOUNT_UNUSABLE(HttpStatus.FORBIDDEN, "사용할 수 없는 계정입니다."),
    ACCOUNT_SUSPENDED(HttpStatus.FORBIDDEN, "정지 기간 중인 계정입니다. 관리자에게 문의하세요."),
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


    // 게시글 관련
    POST_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "게시글 저장에 실패했습니다."),
    POST_NOT_FOUND(HttpStatus.NOT_FOUND, "게시글을 찾을 수 없습니다."),
    QNA_ACCESS_DENIED(HttpStatus.FORBIDDEN, "비공개 QnA 게시글에 대한 접근 권한이 없습니다."),


    // File
    FILE_UPLOAD_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 업로드에 실패했습니다."),
    FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "파일을 찾을 수 없습니다."),
    FILE_EMPTY(HttpStatus.BAD_REQUEST, "업로드할 파일이 없습니다."),
    CANT_ACCESS_FILE(HttpStatus.INTERNAL_SERVER_ERROR, "파일에 접근할 수 없습니다."),
    FILE_ACCESS_DENIED(HttpStatus.FORBIDDEN, "파일에 대한 접근 권한이 없습니다."),
    FILE_INFO_SAVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "파일 정보 저장에 실패했습니다."),
    INVALID_FILE_TYPE_TO_CLOUDINARY(HttpStatus.BAD_REQUEST,
            "Cloudinary에 업로드할 수 없는 파일 형식입니다. PDF, 이미지 파일만 업로드할 수 있습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 파일 형식입니다.  PDF, 이미지 파일만 업로드할 수 있습니다."), // Rest API에서 사용

    FILE_TYPE_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "PDF, 이미지, 동영상, Zip 파일만 업로드할 수 있습니다."),
    FILE_READ_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "파일 내용을 읽는 중 오류가 발생했습니다."),
    INVALID_FILE_CONTEXT(HttpStatus.BAD_REQUEST,
            "유효하지 않은 파일 컨텍스트입니다. FILE_CTX_TYPE 열거형에 정의된 값만 사용할 수 있습니다."),
    FILE_SIZE_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 크기가 10MB를 초과할 수 없습니다."), // Rest API에서 사용
    FILE_IDS_REQUIRED(HttpStatus.BAD_REQUEST, "파일 ID 목록이 비어있습니다."),
    FILE_IDS_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "파일 ID는 최대 5개까지 조회할 수 있습니다."),
    // 결재 관련
    APPROVAL_TEMPLATE_NOT_FOUND(HttpStatus.NOT_FOUND, "결재 양식을 찾을 수 없습니다."),
    APPROVAL_NOT_FOUND(HttpStatus.NOT_FOUND, "결재 문서를 찾을 수 없습니다."),
    APPROVAL_DELETE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "결재 문서 삭제에 실패했습니다."),
    APPROVAL_NOT_DRAFT(HttpStatus.BAD_REQUEST, "결재 문서가 DRAFT 상태가 아닙니다."),
    APPROVAL_NOT_AUTHORIZED(HttpStatus.FORBIDDEN, "결재 문서에 대한 권한이 없습니다."),
    CANNOT_CANCEL_APPROVAL(HttpStatus.BAD_REQUEST, "결재 문서를 취소할 수 없습니다."),
    CANNOT_APPROVE_APPROVAL(HttpStatus.BAD_REQUEST, "결재 문서를 승인할 수 없습니다."),
    FAILED_TO_APPROVE_APPROVAL(HttpStatus.INTERNAL_SERVER_ERROR, "결재 승인 처리에 실패했습니다."),
    // 결제 관련

    // KAKAO

    KAKAO_PAY_READY_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오페이 결제 준비에 실패했습니다."),
    KAKAO_PAY_APPROVE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "카카오페이 결제 승인에 실패했습니다."),
    // TOSS

    ALREADY_PROCESSED_PAYMENT(HttpStatus.BAD_REQUEST, "이미 처리된 결제입니다."),
    // UTIL
    JSON_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "JSON 처리에 실패했습니다."),
    // 직원 관련
    EMPLOYEE_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "직원 등록에 실패했습니다."),
    EMPLOYEE_ID_GENERATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "직원 ID 생성에 실패했습니다."),
    EMPLOYEE_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "직원 정보 수정에 실패했습니다."),
    EMPLOYEE_RETIRE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "직원 퇴사 처리에 실패했습니다."),
    EMPLOYEE_ALREADY_RETIRED(HttpStatus.BAD_REQUEST, "이미 퇴사 처리된 직원입니다."),
    // 업무 일지 관련
    JOURNAL_NOT_FOUND(HttpStatus.NOT_FOUND, "일지를 찾을 수 없습니다."),
    JOURNAL_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인이 작성한 일지만 수정·삭제할 수 있습니다."),
    // 강사 약력 관련
    CAREER_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 약력 항목만 수정·삭제할 수 있습니다."),
    // 문항 관련
    QUESTION_NOT_FOUND(HttpStatus.NOT_FOUND, "문항을 찾을 수 없습니다."),
    QUESTION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인이 등록한 문항만 수정·삭제할 수 있습니다."),
    // 시험 관련
    EXAM_NOT_FOUND(HttpStatus.NOT_FOUND, "시험을 찾을 수 없습니다."),
    EXAM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인이 등록한 시험만 수정·삭제할 수 있습니다."),
    // 회원 관련
    MEMBER_ID_GENETATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "회원 ID 생성에 실패했습니다."),
    MEMBER_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "회원 등록에 실패했습니다."),
    MEMBER_UPDATE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "회원 정보 수정에 실패했습니다."),
    MEMBER_RETIRE_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "회원 탈퇴 처리에 실패했습니다."),
    MEMBER_ALREADY_RETIRED(HttpStatus.BAD_REQUEST, "이미 탈퇴 처리된 회원입니다."),


    STUDENT_NOT_FOUND(HttpStatus.NOT_FOUND, "학생을 찾을 수 없습니다."),
    INVALID_JOIN_LINK(HttpStatus.BAD_REQUEST, "유효하지 않은 회원가입 링크입니다."),
    EXPIRED_JOIN_LINK(HttpStatus.BAD_REQUEST, "만료된 회원가입 링크입니다."),
    PARENT_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "부모 회원 등록에 실패했습니다."),

    // 쪽지 관련
    POST_MESSAGE_CREATE_FAIL(HttpStatus.INTERNAL_SERVER_ERROR, "쪽지 발송에 실패했습니다."),

    // 교재 재고 관련
    INVENTORY_NOT_FOUND(HttpStatus.NOT_FOUND, "재고 정보를 찾을 수 없습니다."),
    INVALID_STOCK_TYPE(HttpStatus.BAD_REQUEST, "유효하지 않은 입출고 유형 코드입니다."),
    INSUFFICIENT_STOCK(HttpStatus.BAD_REQUEST, "재고가 부족합니다."),

    // 장바구니 관련
    CART_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "장바구니 항목을 찾을 수 없습니다."),
    CART_ITEM_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 장바구니에 담긴 상품입니다."),

    // 추천 아이템 관련
    FEATURED_ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "추천 항목을 찾을 수 없습니다."),
    FEATURED_ITEM_LIMIT_EXCEEDED(HttpStatus.CONFLICT, "추천 항목은 최대 11개까지 등록할 수 있습니다."),
    // 주문/결제 관련
    ORDER_NOT_FOUND(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다."),
    ORDER_ALREADY_PAID(HttpStatus.CONFLICT, "이미 처리된 주문입니다."),
    ORDER_AMOUNT_MISMATCH(HttpStatus.BAD_REQUEST, "주문 금액이 일치하지 않습니다."),
    ORDER_CANNOT_CANCEL(HttpStatus.BAD_REQUEST, "결제 완료 상태의 주문만 취소 요청할 수 있습니다."),
    ORDER_CANCEL_ALREADY_APPROVED(HttpStatus.CONFLICT, "이미 처리된 취소 요청입니다."),
    // 취소/환불 관련
    CANCEL_REASON_REQUIRED(HttpStatus.BAD_REQUEST, "취소 사유를 선택해주세요."),
    CANCEL_REASON_TOO_LONG(HttpStatus.BAD_REQUEST, "상세 사유는 500자 이내로 입력해주세요."),
    // 배송 관련
    SHIPPING_NOT_FOUND(HttpStatus.NOT_FOUND, "배송 정보를 찾을 수 없습니다."),
    SHIPPING_ALREADY_IN_PROGRESS(HttpStatus.CONFLICT, "이미 배송이 시작되어 주소를 변경할 수 없습니다."),
    SHIPPING_STATUS_REQUIRED(HttpStatus.BAD_REQUEST, "배송 상태를 입력해주세요."),
    SHIPPING_INVOICE_REQUIRED(HttpStatus.BAD_REQUEST, "배송중 상태로 변경 시 송장번호는 필수입니다."),
    SHIPPING_BUYER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "구매자 이름을 입력해주세요."),
    SHIPPING_BUYER_TEL_REQUIRED(HttpStatus.BAD_REQUEST, "구매자 연락처를 입력해주세요."),
    SHIPPING_ORD_SN_REQUIRED(HttpStatus.BAD_REQUEST, "주문 번호가 필요합니다."),
    // 배송지 관련
    ADDRESS_NOT_FOUND(HttpStatus.NOT_FOUND, "배송지를 찾을 수 없습니다."),
    ADDRESS_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 배송지만 수정·삭제할 수 있습니다."),
    ADDRESS_RECEIVER_NAME_REQUIRED(HttpStatus.BAD_REQUEST, "수령인 이름을 입력해주세요."),
    ADDRESS_RECEIVER_TEL_REQUIRED(HttpStatus.BAD_REQUEST, "수령인 연락처를 입력해주세요."),
    ADDRESS_NAME_TOO_LONG(HttpStatus.BAD_REQUEST, "배송지명은 10자 이내로 입력해주세요."),
    // 쿠폰 관련
    COUPON_NOT_FOUND(HttpStatus.NOT_FOUND, "존재하지 않는 쿠폰입니다."),
    COUPON_INACTIVE(HttpStatus.BAD_REQUEST, "비활성화된 쿠폰입니다."),
    COUPON_INVALID_DISCOUNT(HttpStatus.BAD_REQUEST, "할인 방식에 맞는 할인값을 입력해주세요."),
    COUPON_ISSUE_TARGET_REQUIRED(HttpStatus.BAD_REQUEST, "발급 대상 사용자 ID가 필요합니다."),
    COUPON_DELETE_FAILED(HttpStatus.CONFLICT, "발급 이력이 있는 쿠폰은 삭제할 수 없습니다."),
    COUPON_CODE_NOT_FOUND(HttpStatus.NOT_FOUND, "유효하지 않은 쿠폰 번호입니다."),
    COUPON_ALREADY_ISSUED(HttpStatus.CONFLICT, "이미 발급받은 쿠폰입니다."),

    // 포인트 관련
    POINT_INVALID_AMOUNT(HttpStatus.BAD_REQUEST, "포인트 금액은 1 이상이어야 합니다."),
    POINT_INVALID_TYPE(HttpStatus.BAD_REQUEST, "쿠폰 유형은 포인트로 처리할 수 없습니다."),
    POINT_MINIMUM_USAGE(HttpStatus.BAD_REQUEST, "포인트는 1,000p 이상부터 사용 가능합니다."),
    POINT_INSUFFICIENT_BALANCE(HttpStatus.BAD_REQUEST, "포인트 잔액이 부족합니다."),
    POINT_ISSUE_USER_NOT_FOUND(HttpStatus.NOT_FOUND, "발급 대상 회원을 찾을 수 없습니다.");

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
// @formatter:on
