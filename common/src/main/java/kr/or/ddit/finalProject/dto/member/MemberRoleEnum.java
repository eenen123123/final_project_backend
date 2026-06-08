package kr.or.ddit.finalProject.dto.member;

public enum MemberRoleEnum {
    ROLE_ADMIN("ADMIN"), // 관리자
    ROLE_USER("USER"), // 일반 사용자
    ROLE_PARENT("PARENT"), // 학부모
    ROLE_STUDENT("STUDENT"); // 학생

    private final String role;

    MemberRoleEnum(String role) {
        this.role = role;
    }

    /**
     * 회원 역할 문자열을 반환합니다.
     * (ROLE_ 접두어는 떼어내고 반환)
     *
     * @return 회원 역할 문자열 (예: "ADMIN", "USER", "PARENT", "STUDENT")
     */
    public String getRole() {
        return role;
    }
}
