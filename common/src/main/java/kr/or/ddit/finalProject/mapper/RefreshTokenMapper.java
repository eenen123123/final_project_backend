package kr.or.ddit.finalProject.mapper;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import kr.or.ddit.finalProject.dto.user.RefreshTokenDto;

@Mapper
public interface RefreshTokenMapper {
    Optional<RefreshTokenDto> findByUserId(String userId);

    void deleteRefreshToken(String userId);

    Optional<RefreshTokenDto> findByToken(String refreshTokenHash);

    // 사용자 ID로 리프레시 토큰이 존재하면 업데이트, 없으면 삽입
    void upsertRefreshToken(RefreshTokenDto refreshTokenDto);
}
