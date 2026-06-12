package kr.or.ddit.finalProject.service.featured;

import java.util.List;

import kr.or.ddit.finalProject.dto.featured.FeaturedItemDto;
import kr.or.ddit.finalProject.paging.PaginationInfo;

public interface FeaturedItemService {

    List<FeaturedItemDto> retrieveFeaturedItems(PaginationInfo<FeaturedItemDto> paginationInfo);

    List<FeaturedItemDto> retrieveAllFeaturedItems();

    FeaturedItemDto retrieveFeaturedItemBySn(Long featuredSn);

    int retrieveFeaturedItemsCount();
    int retrieveFeaturedItemsCountByType(String prodType);

    List<FeaturedItemDto> retrieveAllCourses();

    List<FeaturedItemDto> retrieveAllTextbooks();

    void addFeaturedItem(FeaturedItemDto dto);

    void modifyFeaturedItem(FeaturedItemDto dto);

    void removeFeaturedItem(Long featuredSn);
}
