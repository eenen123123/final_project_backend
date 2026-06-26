package kr.or.ddit.config;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

import kr.or.ddit.finalProject.service.permission.MenuPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MenuPermissionAuthorizationManager
        implements AuthorizationManager<RequestAuthorizationContext> {

    private static final List<String> JOB_GRADES =
            Arrays.asList("Z001", "A001", "A002", "A003", "A004", "T001", "T002", "T003");

    private final MenuPermissionService menuPermissionService;

    @Override
    public AuthorizationDecision check(Supplier<Authentication> authSupplier,
                                       RequestAuthorizationContext context) {
        Authentication auth = authSupplier.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        // 원장(D400 / Z001) — 모든 메뉴 허용
        if (hasAuthority(auth, "D400") || hasAuthority(auth, "Z001")) {
            return new AuthorizationDecision(true);
        }

        String uri = context.getRequest().getRequestURI();
        String menuCode = MenuUrlRegistry.findMenuCode(uri);

        // 매핑된 메뉴가 없으면 ADMIN 로그인 여부만 확인
        if (menuCode == null) {
            return new AuthorizationDecision(hasRole(auth, "ADMIN"));
        }

        String jobGrade = extractJobGrade(auth);
        if (jobGrade == null) {
            return new AuthorizationDecision(false);
        }

        return new AuthorizationDecision(menuPermissionService.isAllowed(menuCode, jobGrade));
    }

    private boolean hasAuthority(Authentication auth, String authority) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(authority));
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }

    private String extractJobGrade(Authentication auth) {
        return auth.getAuthorities().stream()
                .map(a -> a.getAuthority())
                .filter(JOB_GRADES::contains)
                .findFirst()
                .orElse(null);
    }
}
