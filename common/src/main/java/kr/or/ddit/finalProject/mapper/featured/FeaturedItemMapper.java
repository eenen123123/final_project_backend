package kr.or.ddit.finalProject.mapper.featured;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.featured.FeaturedItemDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

@Mapper
public interface FeaturedItemMapper {

    List<FeaturedItemDto> selectFeaturedItems(PaginationInfo<FeaturedItemDto> paginationInfo);

    List<FeaturedItemDto> selectAllFeaturedItems();

    FeaturedItemDto selectFeaturedItemBySn(@Param("featuredSn") Long featuredSn);

    int selectFeaturedItemsCount();
    int selectFeaturedItemsCountByType(@Param("prodType") String prodType);

    List<FeaturedItemDto> selectAllCourses();

    List<FeaturedItemDto> selectAllTextbooks();

    int insertFeaturedItem(FeaturedItemDto dto);

    int updateFeaturedItem(FeaturedItemDto dto);

    int deleteFeaturedItem(@Param("featuredSn") Long featuredSn);
}
