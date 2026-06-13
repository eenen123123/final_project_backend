package kr.or.ddit.finalProject.service.featured;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import kr.or.ddit.finalProject.dto.featured.FeaturedItemDto;
import kr.or.ddit.finalProject.exception.ErrorCode;
import kr.or.ddit.finalProject.exception.FinalProjectException;
import kr.or.ddit.finalProject.mapper.featured.FeaturedItemMapper;
import kr.or.ddit.finalProject.paging.PaginationInfo;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FeaturedItemServiceImpl implements FeaturedItemService {

    private final FeaturedItemMapper featuredItemMapper;

    @Override
    public List<FeaturedItemDto> retrieveFeaturedItems(PaginationInfo<FeaturedItemDto> paginationInfo) {
        return featuredItemMapper.selectFeaturedItems(paginationInfo);
    }

    @Override
    public List<FeaturedItemDto> retrieveAllFeaturedItems() {
        return featuredItemMapper.selectAllFeaturedItems();
    }

    @Override
    public FeaturedItemDto retrieveFeaturedItemBySn(Long featuredSn) {
        FeaturedItemDto item = featuredItemMapper.selectFeaturedItemBySn(featuredSn);
        if (item == null) {
            throw new FinalProjectException(ErrorCode.FEATURED_ITEM_NOT_FOUND);
        }
        return item;
    }

    @Override
    public int retrieveFeaturedItemsCount() {
        return featuredItemMapper.selectFeaturedItemsCount();
    }

    @Override
    public int retrieveFeaturedItemsCountByType(String prodType) {
        return featuredItemMapper.selectFeaturedItemsCountByType(prodType);
    }

    @Override
    public List<FeaturedItemDto> retrieveAllCourses() {
        return featuredItemMapper.selectAllCourses();
    }

    @Override
    public List<FeaturedItemDto> retrieveAllTextbooks() {
        return featuredItemMapper.selectAllTextbooks();
    }

    @Override
    @Transactional
    public void addFeaturedItem(FeaturedItemDto dto) {
        if (featuredItemMapper.selectFeaturedItemsCount() >= 11) {
            throw new FinalProjectException(ErrorCode.FEATURED_ITEM_LIMIT_EXCEEDED);
        }
        featuredItemMapper.insertFeaturedItem(dto);
    }

    @Override
    @Transactional
    public void modifyFeaturedItem(FeaturedItemDto dto) {
        int updated = featuredItemMapper.updateFeaturedItem(dto);
        if (updated == 0) {
            throw new FinalProjectException(ErrorCode.FEATURED_ITEM_NOT_FOUND);
        }
    }

    @Override
    @Transactional
    public void removeFeaturedItem(Long featuredSn) {
        int deleted = featuredItemMapper.deleteFeaturedItem(featuredSn);
        if (deleted == 0) {
            throw new FinalProjectException(ErrorCode.FEATURED_ITEM_NOT_FOUND);
        }
    }
}
