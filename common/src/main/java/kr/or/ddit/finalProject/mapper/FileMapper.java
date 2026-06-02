package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.file.FileDto;

@Mapper
public interface FileMapper {

    int insertFileInfo(FileDto fileDto);

    FileDto findContextByFileServerId(long fileServerId);

    int selectNextFileGroupId();

    int insertFileGroup(@Param("atchFileId") int atchFileId);

    List<FileDto> selectFilesByGroupId(@Param("groupId") int groupId);

    int softDeleteFile(@Param("atchFileDtlSn") Integer atchFileDtlSn,
            @Param("delUserId") String delUserId);

    // fileIds 중 userId 소유인 파일 수 반환 — size()와 비교해 전체 소유 여부 확인
    int countOwnedFiles(@Param("fileIds") List<Long> fileIds, @Param("userId") String userId);

    // 게시글 저장 완료 후 이미지 파일들의 CTX_TYPE, CTX_ID 일괄 업데이트
    int updateFileContext(@Param("fileIds") List<Long> fileIds,
            @Param("ctxType") String ctxType,
            @Param("ctxId") long ctxId);
}
