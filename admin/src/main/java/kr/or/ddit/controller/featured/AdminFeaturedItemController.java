package kr.or.ddit.controller.featured;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

import kr.or.ddit.finalProject.dto.cart.ProductType;
import kr.or.ddit.finalProject.dto.featured.FeaturedItemDto;
import kr.or.ddit.finalProject.service.featured.FeaturedItemService;
import kr.or.ddit.finalProject.service.file.CloudinaryUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Controller
@RequestMapping("/admin/featured")
@RequiredArgsConstructor
public class AdminFeaturedItemController {

    private final FeaturedItemService featuredItemService;
    private final CloudinaryUploadService cloudinaryUploadService;

    // GET /admin/featured/insert — 등록 페이지
    @GetMapping("/insert")
    public String featuredInsertForm() {
        return "admin:/featured/featured_insert";
    }

    // GET /admin/featured/{featuredSn}/edit — 수정 페이지
    @GetMapping("/{featuredSn}/edit")
    public String featuredEditForm(@PathVariable Long featuredSn, Model model) {
        model.addAttribute("item", featuredItemService.retrieveFeaturedItemBySn(featuredSn));
        return "admin:/featured/featured_edit";
    }

    // GET /admin/featured/popup/products — 상품 선택 팝업 (레이아웃 없음)
    @GetMapping("/popup/products")
    public String productSelectPopup(Model model) {
        model.addAttribute("courses", featuredItemService.retrieveAllCourses());
        model.addAttribute("textbooks", featuredItemService.retrieveAllTextbooks());
        return "featured/featured_popup";
    }

    // GET /admin/featured — 페이지 렌더링
    @GetMapping
    public String featuredList(Model model) {
        List<FeaturedItemDto> featuredItems = featuredItemService.retrieveAllFeaturedItems();
        int totalCount = featuredItems.size();
        int courseFeaturedCount = (int) featuredItems.stream()
                .filter(i -> i.getProdType() == ProductType.COURSE)
                .count();
        model.addAttribute("featuredItems", featuredItems);
        model.addAttribute("totalCount", totalCount);
        model.addAttribute("courseFeaturedCount", courseFeaturedCount);
        model.addAttribute("textbookFeaturedCount", totalCount - courseFeaturedCount);
        return "admin:/featured/featured_list";
    }

    // POST /admin/featured/api/upload-image — 커스텀 이미지 Cloudinary 업로드
    @PostMapping("/api/upload-image")
    @ResponseBody
    public ResponseEntity<Map<String, String>> uploadImage(@RequestParam MultipartFile image) throws java.io.IOException {
        String url = cloudinaryUploadService.uploadFileToCloudinary(image);
        return ResponseEntity.ok(Map.of("url", url));
    }

    // GET /admin/featured/api — 목록 AJAX (전체)
    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<List<FeaturedItemDto>> getFeaturedItems() {
        return ResponseEntity.ok(featuredItemService.retrieveAllFeaturedItems());
    }

    // POST /admin/featured/api — 등록 AJAX
    @PostMapping("/api")
    @ResponseBody
    public ResponseEntity<Void> addFeaturedItem(@RequestBody FeaturedItemDto dto,
            Authentication authentication) {
        dto.setRgtrId(authentication.getName());
        featuredItemService.addFeaturedItem(dto);
        return ResponseEntity.ok().build();
    }

    // PUT /admin/featured/api/{featuredSn} — 수정 AJAX
    @PutMapping("/api/{featuredSn}")
    @ResponseBody
    public ResponseEntity<Void> modifyFeaturedItem(@PathVariable Long featuredSn,
            @RequestBody FeaturedItemDto dto) {
        dto.setFeaturedSn(featuredSn);
        featuredItemService.modifyFeaturedItem(dto);
        return ResponseEntity.noContent().build();
    }

    // DELETE /admin/featured/api/{featuredSn} — 삭제 AJAX
    @DeleteMapping("/api/{featuredSn}")
    @ResponseBody
    public ResponseEntity<Void> removeFeaturedItem(@PathVariable Long featuredSn) {
        featuredItemService.removeFeaturedItem(featuredSn);
        return ResponseEntity.noContent().build();
    }
}
