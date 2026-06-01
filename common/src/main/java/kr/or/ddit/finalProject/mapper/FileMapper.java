package kr.or.ddit.finalProject.mapper;

import java.util.List;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import kr.or.ddit.finalProject.dto.file.FileDto;

@Mapper
public interface FileMapper {

    int insertFileInfo(FileDto fileDto);

    FileDto findContextByFileId(long fileId);

    int selectNextFileGroupId();

    int insertFileGroup(@Param("atchFileId") int atchFileId);

    List<FileDto> selectFilesByGroupId(@Param("groupId") int groupId);

    int softDeleteFile(@Param("atchFileDtlSn") Integer atchFileDtlSn,
            @Param("delUserId") String delUserId);

}
