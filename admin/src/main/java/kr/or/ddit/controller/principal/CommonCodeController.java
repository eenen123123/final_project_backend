package kr.or.ddit.controller.principal;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import kr.or.ddit.finalProject.dto.code.ComClDto;
import kr.or.ddit.finalProject.dto.common.CommonCodeDto;
import kr.or.ddit.finalProject.dto.staff.AdminActivityType;
import kr.or.ddit.finalProject.mapper.common.CommonCodeMapper;
import kr.or.ddit.service.AdminActivityApprovalService;
import kr.or.ddit.service.CommonCodeService;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/admin/common-codes")
@RequiredArgsConstructor
public class CommonCodeController {

    private final CommonCodeService commonCodeService;
    private final CommonCodeMapper commonCodeMapper;
    private final AdminActivityApprovalService approvalService;
    @GetMapping
    public String page() {
        return "admin:/principal/common_code_management";
    }

    // ── 드롭다운 로딩 (USE_YN='Y', 전 페이지 공용) ────────────────
    @GetMapping("/options/{clCode}")
    @ResponseBody
    public ResponseEntity<List<CommonCodeDto>> getOptions(@PathVariable String clCode) {
        return ResponseEntity.ok(commonCodeMapper.selectByClCode(clCode));
    }

    // ── 분류(COM_CL) 관리 ─────────────────────────────────────────
    @GetMapping("/api/groups")
    @ResponseBody
    public ResponseEntity<List<ComClDto>> getGroups() {
        return ResponseEntity.ok(commonCodeService.getGroups());
    }

    @PostMapping("/api/groups")
    @ResponseBody
    public ResponseEntity<Void> createGroup(
        @RequestBody ComClDto dto,
        Authentication auth
    ) {
        if (commonCodeService.existsGroup(dto.getClCode())) {
            return ResponseEntity.status(409).build();
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dto", dto);
        approvalService.submitForApproval(auth.getName(), AdminActivityType.COMMON_CODE_GROUP_CREATE, dto.getClCode() + " " + dto.getClCdNm(), data);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/groups/{clCode}")
    @ResponseBody
    public ResponseEntity<Void> updateGroup(
        @PathVariable String clCode,
        @RequestBody ComClDto dto,
        Authentication auth
    ) {
        dto.setClCode(clCode);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dto", dto);
        approvalService.submitForApproval(auth.getName(), AdminActivityType.COMMON_CODE_GROUP_UPDATE, clCode, data);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/groups/{clCode}")
    @ResponseBody
    public ResponseEntity<Void> deleteGroup(
        @PathVariable String clCode,
        Authentication auth
    ) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("clCode", clCode);
        approvalService.submitForApproval(auth.getName(), AdminActivityType.COMMON_CODE_GROUP_DELETE, clCode, data);
        return ResponseEntity.ok().build();
    }

    // ── 코드(COM_CD) 관리 ─────────────────────────────────────────
    @GetMapping("/api/{clCode}/codes")
    @ResponseBody
    public ResponseEntity<List<CommonCodeDto>> getCodes(@PathVariable String clCode) {
        return ResponseEntity.ok(commonCodeService.getAllCodes(clCode));
    }

    @PostMapping("/api/{clCode}/codes")
    @ResponseBody
    public ResponseEntity<Void> createCode(
        @PathVariable String clCode,
        @RequestBody CommonCodeDto dto,
        Authentication auth
    ) {
        dto.setClCode(clCode);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dto", dto);
        approvalService.submitForApproval(auth.getName(), AdminActivityType.COMMON_CODE_CREATE, "[" + clCode + "] " + dto.getComCd() + " " + dto.getComCdNm(), data);
        return ResponseEntity.ok().build();
    }

    @PutMapping("/api/{clCode}/codes/{comCd}")
    @ResponseBody
    public ResponseEntity<Void> updateCode(
        @PathVariable String clCode,
        @PathVariable String comCd,
        @RequestBody CommonCodeDto dto,
        Authentication auth
    ) {
        dto.setClCode(clCode);
        dto.setComCd(comCd);
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("dto", dto);
        approvalService.submitForApproval(auth.getName(), AdminActivityType.COMMON_CODE_UPDATE, "[" + clCode + "] " + comCd, data);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/api/{clCode}/codes/{comCd}")
    @ResponseBody
    public ResponseEntity<Void> deleteCode(
        @PathVariable String clCode,
        @PathVariable String comCd,
        Authentication auth
    ) {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("clCode", clCode);
        data.put("comCd", comCd);
        approvalService.submitForApproval(auth.getName(), AdminActivityType.COMMON_CODE_DELETE, "[" + clCode + "] " + comCd, data);
        
        return ResponseEntity.ok().build();
    }
}
