package kr.or.ddit.finalProject.dto.member;

public enum MemberRoleEnum {
    ROLE_ADMIN("ADMIN"), ROLE_USER("USER"), ROLE_PARENT("PARENT"), ROLE_STUDENT("STUDENT");

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
