package kr.or.ddit.finalProject.dto.student;

import java.io.Serializable;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentDto implements Serializable {

    private String stdUserId; // MEMBER 테이블(PK)
    private String enrlSchlNm;
    private String prntUserId;
    private String prntTelno;
}
