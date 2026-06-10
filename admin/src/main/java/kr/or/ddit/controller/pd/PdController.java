package kr.or.ddit.controller.pd;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import kr.or.ddit.service.pd.PdService;
import lombok.RequiredArgsConstructor;


@Controller
@RequestMapping("/admin/media")
@RequiredArgsConstructor
public class PdController {
    private final PdService pdService;

    /**
     * 라이브 VOD 아카이빙
     * @return
     */
    @GetMapping("/live")
    public String getVodLive() {
        return "admin:/pd/live_archiving";
    }

    /**
     * 영상 관련 공지 연동
     * @return
     */
    @GetMapping("/notices")
    public String getNotices() {
        return "admin:/pd/media_notices";
    }

    /**
     * 영상 업로드 및 게시
     * @return
     */
    @GetMapping("/upload")
    public String getUpload(Model model) {
        model.addAttribute("courseOptions", pdService.getCourseOptions());
        return "admin:/pd/media_upload";
    }

    @GetMapping("/media-list")
    public String getMediaList(Model model) {
        model.addAttribute("courseOptions", pdService.getCourseOptions());
        return "admin:/pd/media_list";
    }


    /**
     * 강의 파일 관리
     * @return
     */
    @GetMapping("/files")
    public String getFiles() {
        return "admin:/pd/media_files";
    }

    /**
     * 강의 정보 최적화
     */
    @GetMapping("/optimize")
    public String getOptimize() {
        return "admin:/pd/media_optimize";
    }

    /**
     * 품질 및 호환성 관리
     */
    @GetMapping("/quality")
    public String getQuality() {
        return "admin:/pd/media_quality";
    }



}
