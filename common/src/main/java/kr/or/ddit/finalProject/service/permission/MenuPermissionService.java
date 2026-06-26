package kr.or.ddit.finalProject.service.permission;

import java.util.List;
import java.util.Map;
import kr.or.ddit.finalProject.dto.permission.MenuPermissionDto;

public interface MenuPermissionService {

    /** 전체 권한 맵 반환: key = "menuCd:jobGrade", value = true/false */
    Map<String, Boolean> loadAll();

    /** 특정 (메뉴, 직급) 허용 여부 */
    boolean isAllowed(String menuCd, String jobGrade);

    /** UI에서 전달받은 목록 일괄 저장 후 캐시 무효화 */
    void saveAll(List<MenuPermissionDto> permissions);
}
