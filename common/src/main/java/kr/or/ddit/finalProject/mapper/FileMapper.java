package kr.or.ddit.finalProject.mapper;

import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.file.FileDto;

@Mapper
public interface FileMapper {

    int insertFileInfo(FileDto fileDto);

    FileDto findContextByFileId(long fileId);
}
