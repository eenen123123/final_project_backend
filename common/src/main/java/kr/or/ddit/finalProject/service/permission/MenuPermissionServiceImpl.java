package kr.or.ddit.finalProject.service.permission;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import kr.or.ddit.finalProject.dto.permission.MenuPermissionDto;
import kr.or.ddit.finalProject.mapper.MenuPermissionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MenuPermissionServiceImpl implements MenuPermissionService {

    private final MenuPermissionMapper mapper;

    @Override
    @Cacheable("menuPermissions")
    public Map<String, Boolean> loadAll() {
        return mapper.selectAll().stream()
                .collect(Collectors.toMap(
                        dto -> dto.getMenuCd() + ":" + dto.getJobGrade(),
                        dto -> "Y".equals(dto.getAllowed())
                ));
    }

    @Override
    public boolean isAllowed(String menuCd, String jobGrade) {
        Boolean allowed = loadAll().get(menuCd + ":" + jobGrade);
        return Boolean.TRUE.equals(allowed);
    }

    @Override
    @Transactional
    @CacheEvict(value = "menuPermissions", allEntries = true)
    public void saveAll(List<MenuPermissionDto> permissions) {
        for (MenuPermissionDto dto : permissions) {
            mapper.upsert(dto);
        }
    }
}
