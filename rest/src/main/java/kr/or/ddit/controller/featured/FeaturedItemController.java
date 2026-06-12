package kr.or.ddit.controller.featured;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import kr.or.ddit.finalProject.dto.featured.FeaturedItemDto;
import kr.or.ddit.finalProject.service.featured.FeaturedItemService;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/featured")
@RequiredArgsConstructor
public class FeaturedItemController {

    private final FeaturedItemService featuredItemService;

    // GET /api/featured
    @GetMapping
    public ResponseEntity<List<FeaturedItemDto>> getFeaturedItems() {
        return ResponseEntity.ok(featuredItemService.retrieveAllFeaturedItems());
    }
}
