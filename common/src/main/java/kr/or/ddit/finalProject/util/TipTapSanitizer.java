package kr.or.ddit.finalProject.util;

import org.jsoup.Jsoup;
import org.jsoup.safety.Safelist;

/**
 * TipTap 에디터 출력 HTML의 XSS 정제 유틸리티.
 * EDITOR_SAFELIST를 한 곳에서 관리하여 모든 컨트롤러/서비스가 동일한 허용 규칙을 공유한다.
 */
public final class TipTapSanitizer {

    private TipTapSanitizer() {}

    private static final Safelist SAFELIST = Safelist.relaxed()
            .preserveRelativeLinks(true)
            .addTags("del", "s", "hr", "input", "mark")
            .addAttributes("input", "type", "checked", "disabled")
            .addAttributes("span", "style")
            .addAttributes("p", "style")
            .addAttributes("h1", "style").addAttributes("h2", "style").addAttributes("h3", "style")
            .addAttributes("h4", "style").addAttributes("h5", "style").addAttributes("h6", "style")
            .addAttributes("img", "src", "data-file-id", "width")
            .addAttributes("mark", "data-color", "style")
            .addProtocols("img", "src", "http", "https", "data");

    public static String clean(String html) {
        if (html == null) return null;
        return Jsoup.clean(html, SAFELIST);
    }
}
