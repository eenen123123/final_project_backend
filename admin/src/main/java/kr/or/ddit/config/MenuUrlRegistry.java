package kr.or.ddit.config;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * URL 경로 prefix → 메뉴 코드 매핑.
 * 더 구체적인 경로가 먼저 오도록 순서를 지킨다.
 */
public final class MenuUrlRegistry {

    private static final Map<String, String> PREFIX_MAP = new LinkedHashMap<>();

    static {
        // ── 원장 업무 ──
        PREFIX_MAP.put("/admin/monitoring",           "academic_monitoring");
        PREFIX_MAP.put("/admin/system",               "system_log");
        PREFIX_MAP.put("/admin/finance",              "finance_analysis");
        PREFIX_MAP.put("/admin/quality",              "quality");
        PREFIX_MAP.put("/admin/settings/permissions", "perm_settings");

        // ── 행정팀장 업무 ──
        PREFIX_MAP.put("/admin/instructors/monitor",  "instructor_monitor");
        PREFIX_MAP.put("/admin/consultation",         "consultation");
        PREFIX_MAP.put("/admin/retention",            "retention");

        // ── 행정 업무 ──
        PREFIX_MAP.put("/admin/students",             "user_management");
        PREFIX_MAP.put("/admin/org",                  "org_management");
        PREFIX_MAP.put("/admin/blacklist",            "blacklist");
        PREFIX_MAP.put("/admin/billing",              "billing");
        PREFIX_MAP.put("/admin/attendance",           "billing");        // 출결도 수납 범주
        PREFIX_MAP.put("/admin/textbook",             "textbook");
        PREFIX_MAP.put("/admin/logistics",            "orders");
        PREFIX_MAP.put("/admin/featured",             "featured");
        PREFIX_MAP.put("/admin/coupon",               "coupon");
        PREFIX_MAP.put("/admin/expenses",             "billing");
        PREFIX_MAP.put("/admin/salary",               "billing");
        PREFIX_MAP.put("/admin/hr",                   "org_management");
        PREFIX_MAP.put("/admin/parent",               "user_management");
        PREFIX_MAP.put("/admin/subject",              "subject");
        PREFIX_MAP.put("/admin/common-codes",         "common_codes");
        PREFIX_MAP.put("/admin/certificates",         "certificate_manage");

        // ── 강사 업무 ──
        PREFIX_MAP.put("/instructor/questions",       "questions");
        PREFIX_MAP.put("/instructor/curriculum",      "curriculum");
        PREFIX_MAP.put("/classroom",                  "classroom");
        PREFIX_MAP.put("/instructor/board",           "instructor_board");
        PREFIX_MAP.put("/instructor/work-log",        "work_log");
        PREFIX_MAP.put("/instructor/my-page",         "my_page");
        PREFIX_MAP.put("/instructor",                 "classroom");      // fallback

        // ── 공통 업무 ──
        PREFIX_MAP.put("/admin/notifications",        "messages");
        PREFIX_MAP.put("/admin/messenger",            "messenger");
        PREFIX_MAP.put("/admin/approval",             "approval");
        PREFIX_MAP.put("/admin/schedule",             "schedule");
        PREFIX_MAP.put("/admin/course",               "course_view");
    }

    private MenuUrlRegistry() {}

    /** URI 에 대응하는 메뉴 코드를 반환. 없으면 null. */
    public static String findMenuCode(String uri) {
        for (Map.Entry<String, String> entry : PREFIX_MAP.entrySet()) {
            if (uri.startsWith(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }
}
