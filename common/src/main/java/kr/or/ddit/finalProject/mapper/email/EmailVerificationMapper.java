package kr.or.ddit.finalProject.mapper.email;

import java.util.Optional;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import kr.or.ddit.finalProject.dto.email.EmailVerificationDto;

@Mapper
public interface EmailVerificationMapper {
    void insertEmailVerification(EmailVerificationDto emailVerificationDto);

    EmailVerificationDto selectEmailVerificationDtoByEmail(String email);



    Optional<Integer> selectIdExistingEmailVerificationId(String email);

    void deleteEmailVerificationById(int id);

    void deleteEmailVerificationByEmail(String email);

    void updateEmailVerifiedAt(int validId);

    EmailVerificationDto isEmailVerified(String email);
}
