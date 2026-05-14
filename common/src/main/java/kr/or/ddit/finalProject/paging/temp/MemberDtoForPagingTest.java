package kr.or.ddit.finalProject.paging.temp;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

// 페이징 처리 테스트를 위해 임시로 만든 클래스
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberDtoForPagingTest implements Serializable {

    private int rnum;

    private String memId;

    private transient String memPass;

    private String memName;

    private transient String memRegno1;
    private transient String memRegno2;

    private transient LocalDate memBir;

    private String memZip;

    private String memAdd1;

    private String memAdd2;

    private String memHometel;

    private String memComtel;

    private String memHp;

    private String memMail;

    private String memJob;
    private String memLike;
    private String memMemorial;

    private transient LocalDate memMemorialday;

    @PositiveOrZero
    private Integer memMileage;
    private boolean memDelete;
    private List<String> memRoles;

}