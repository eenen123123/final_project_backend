package kr.or.ddit.finalProject.dto.file;

import java.time.LocalDateTime;

/**
 * 파일 서버에서 반환하는 파일 정보 DTO
 * 
 * 
 * 
 * 응답 예시:
 * 
    ID: 13

    Original Filename: 59447b03eb3c1f37046c3fcbb3f0e7e5ed48cbbc76fefc6053960c6f11e40257.png

    Content Type: image/png

    File Type: IMAGE

    File Size: 2894059 bytes

    URL: https://paste.maerchen.dev/api/storage/files/13/view

    View URL: https://paste.maerchen.dev/api/storage/files/13/view

    Download URL: https://paste.maerchen.dev/api/storage/files/13/download

    Uploaded By: anonymous

    Uploaded At: 2026-05-18T13:56:58.980220325
 * 
 * 
 * 
 * 
 *  
 */

public record StoredFileResponse(Long id, // 파일 ID
        String originalFilename, // 원본 파일 이름
        String contentType, // 파일의 MIME 타입 (예: image/png, application/pdf 등)
        String fileType, // 파일 유형 (예: IMAGE, DOCUMENT 등)
        Long fileSize, // 파일 크기 (바이트 단위)
        String url, // 파일 접근 URL
        String viewUrl, // 파일 뷰어 URL
        String downloadUrl, // 파일 다운로드 URL
        String uploadedBy, // 업로드한 사용자
        LocalDateTime uploadedAt // 업로드 시각
) {
}
