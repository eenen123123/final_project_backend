package kr.or.ddit.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.approval.ApprovalLineDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalMasterDto;
import kr.or.ddit.finalProject.dto.approval.ApprovalTemplateDto;

@Mapper
public interface ApprovalMapper {
    int insertApprovalTemplate(ApprovalTemplateDto approvalTemplateDto);

    List<ApprovalTemplateDto> selectApprovalTemplateList();

    ApprovalTemplateDto selectApprovalTemplateById(@Param("tmplCd") String tmplCd);

    int insertApprovalMaster(ApprovalMasterDto approvalMasterDto);

    int insertApprovalLine(ApprovalLineDto approvalLineDto);

    List<ApprovalMasterDto> selectMyDocs(String userId);

    List<ApprovalLineDto> selectMyPendingLines(String userId);

    List<ApprovalLineDto> selectMyProcessedLines(String userId);

    List<ApprovalLineDto> selectApprovalLinesByUserId(String userId);

    ApprovalMasterDto selectApprovalMasterByDocSn(Long aprvlDocSn);

    List<ApprovalLineDto> selectApprovalLinesByDocSn(Long aprvlDocSn);

    int updateApprovalMaster(ApprovalMasterDto approvalMasterDto);

    int deleteApprovalLinesByDocSn(@Param("aprvlDocSn") Long aprvlDocSn,
            @Param("aprvl_status") String aprvlStatus);

    int deleteApprovalMaster(@Param("aprvlDocSn") Long aprvlDocSn,
            @Param("aprvl_status") String aprvlStatus);

    int updateApprovalLine(ApprovalLineDto myLine);

    int countAllPendingLines();
}
