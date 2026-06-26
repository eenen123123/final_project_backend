package kr.or.ddit.config;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import kr.or.ddit.finalProject.service.permission.MenuPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class SidebarMenuAdvice {

    private final MenuPermissionService menuPermissionService;

    /**
     * null  → 원장(D400/Z001): 모든 메뉴 표시
     * Set   → DB 기반 허용된 메뉴 코드 집합
     */
    @ModelAttribute("allowedMenus")
    public Set<String> allowedMenus() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return Set.of();

        if (hasAuthority(auth, "D400") || hasAuthority(auth, "Z001")) {
            return null;
        }

        Map<String, Boolean> allPerms = menuPermissionService.loadAll();

        // DB에 존재하는 직급 코드 집합 (하드코딩 없이 동적으로 추출)
        Set<String> knownGrades = allPerms.keySet().stream()
                .map(key -> key.split(":")[1])
                .collect(Collectors.toSet());

        String jobGrade = auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(knownGrades::contains)
                .findFirst()
                .orElse(null);

        if (jobGrade == null) return Set.of();

        return allPerms.entrySet().stream()
                .filter(e -> e.getKey().endsWith(":" + jobGrade) && Boolean.TRUE.equals(e.getValue()))
                .map(e -> e.getKey().split(":")[0])
                .collect(Collectors.toSet());
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }
}
