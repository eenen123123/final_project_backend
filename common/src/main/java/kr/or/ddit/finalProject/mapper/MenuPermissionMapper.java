package kr.or.ddit.finalProject.mapper;

import java.util.List;
import kr.or.ddit.finalProject.dto.permission.MenuPermissionDto;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface MenuPermissionMapper {

    List<MenuPermissionDto> selectAll();

    void upsert(MenuPermissionDto dto);
}
