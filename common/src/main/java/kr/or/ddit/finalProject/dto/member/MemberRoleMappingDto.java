package kr.or.ddit.finalProject.dto.member;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberRoleMappingDto implements Serializable {

    private String userId; // 기본키 (PK)
    private String userRoleCd;
}