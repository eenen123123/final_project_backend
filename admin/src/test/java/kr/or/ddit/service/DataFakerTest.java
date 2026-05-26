package kr.or.ddit.service;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import kr.or.ddit.finalProject.dto.member.MemberDto;
import kr.or.ddit.finalProject.mapper.MemberMapper;
import lombok.extern.slf4j.Slf4j;
import net.datafaker.Faker;

@Slf4j
@SpringBootTest
public class DataFakerTest {
    Faker faker = new Faker(Locale.KOREAN);

    @Autowired
    MemberMapper memberMapper;

    /**
     * 전체 회원 더미 데이터 생성 테스트
     * 하나의 반복문 안에서 모든 컬럼 데이터를 조립하여 하나의 Member 객체 형태를 구성해야 한다.
     */
    @Test
    void testGenerateAllDummyData() {
        int insertCount = 0;
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String userId = null;

        for (int i = 2; i < 101; i++) {
            
            // 1. USER_ID (VARCHAR2 20)
            // 영문 소문자와 숫자를 조합하여 5~15자리 아이디를 생성한다.
            if (i < 10) {
                userId = "testuser0" + i;
            } else {
                userId = "testuser" + i;
            }

            // 2. USER_ENPSWD (VARCHAR2 256)
            // // $2a$10$Vtw2SmlhNR1zjwKtsY3Cn.DLGnvGfEPNzXLOleJ3shDsH9FibR9xG
            String userEnpswd = "$2a$10$Vtw2SmlhNR1zjwKtsY3Cn.DLGnvGfEPNzXLOleJ3shDsH9FibR9xG";

            // 3. USER_NAME (VARCHAR2 100)
            String familyName = faker.name().lastName();
            String name = faker.name().firstName();
            if(name.length() == 1 || familyName.length() >= 2) {
                i--;
                continue;
            }
            String userName = familyName + name;

            // 4. USER_ENRRNO (VARCHAR2 256)
            // 정규식을 활용하여 13자리 주민등록번호 원본 형태를 생성한다.
            // 이후 DB에 넣기 직전 서비스 단에서 AES-256 암호화를 적용해야 한다.
            String userEnrrno = faker.regexify("\\d{6}[1-4]\\d{6}");

            // 5. USER_GNDR_CD (CHAR 1)
            // 데이터 정합성을 위해 주민등록번호 7번째 자리를 기준으로 성별을 판별한다.
            char genderDigit = userEnrrno.charAt(6);
            String userGndrCd = (genderDigit == '1' || genderDigit == '3') ? "M" : "F";

            // 6. USER_BRDT (DATE)
            // 생년월일을 지정된 날짜 포맷으로 생성한다.
            String userBrdt = sdf.format(faker.date().birthday());
            LocalDate parsedDate = LocalDate.parse(userBrdt);

            // 7. USER_TELNO (VARCHAR2 11)
            // DB 컬럼 사이즈가 11바이트이므로, 생성된 전화번호에서 하이픈(-)을 제거해야 한다.
            String userTelno = faker.phoneNumber().cellPhone().replaceAll("-", "");

            // 8. USER_EMAIL_ADDR (VARCHAR2 300)
            String[] domains = {"naver.com", "gmail.com", "daum.net", "kakao.com"};
        
            // 배열 중에서 무작위로 도메인을 하나 선택한다.
            String randomDomain = domains[faker.random().nextInt(domains.length)];
                    
            // 생성해둔 아이디와 무작위 도메인을 깔끔하게 결합한다.
            String userEmailAddr = userId + "@" + randomDomain;

            // 9. 주소 정보 (USER_ZIP, USER_ADDR, USER_DADDR)
            String userZip = faker.address().zipCode();
            String userAddr = faker.address().state() + " " + faker.address().city() + " " + faker.address().streetName();
            String userDaddr = faker.address().secondaryAddress();

            // 10. USER_PROFILE (VARCHAR2 500)
            // 프로필에 사용될 임의의 아바타 이미지 URL을 생성한다.
            // String userProfile = faker.internet().avatar();

            // 11. ENABLE (VARCHAR2 1)
            // 계정 활성화 상태 기본값인 '1'을 할당한다.
            String enable = "1";

            // JOIN_DT, REG_DATE, MOD_DATE는 DB의 DEFAULT CURRENT_TIMESTAMP 설정에 의해 
            // 쿼리 실행 시 자동 생성되므로 애플리케이션 단에서 별도로 주입하지 않아도 된다.

            // 결과 확인용 로그 출력한다. (실제로는 생성된 데이터를 DTO에 담아 Mapper로 넘겨야 한다.)
            log.info("ID: {}, NAME: {}, RRN: {}, GNDR: {}, TEL: {}, ZIP: {}, ADDR: {}, DADDR: {}, BRDT: {}, EMAIL: {}, ", 
                     userId, userName, userEnrrno, userGndrCd, userTelno, userZip, userAddr, userDaddr, parsedDate, userEmailAddr);
            
            MemberDto newMember = MemberDto.builder()
                    .userId(userId)
                    .userEnpswd(userEnpswd)
                    .userName(userName)
                    .userEnrrno(userEnrrno)
                    .userGndrCd(userGndrCd)
                    .userBrdt(parsedDate)
                    .userTelno(userTelno)
                    .userEmailAddr(userEmailAddr)
                    .userZip(userZip)
                    .userAddr(userAddr)
                    .userDaddr(userDaddr)
                    // enable과 날짜 정보는 동적 쿼리에 의해 DB 기본값이 들어가므로 생략한다.
                    .build();
                    
            try {
                memberMapper.fakerMember(newMember);
                insertCount++;
                log.info("[{}] 번째 회원 Insert 성공: {}", insertCount, userId);
            } catch (Exception e) {
                log.error("Insert 중 오류 발생 (ID: {}): {}", userId, e.getMessage());
            }
        }
        log.info("총 {}건의 더미 데이터 Insert가 완료되었습니다.", insertCount);
    }

    @Test
    void testFakerRoleMapping() {
        List<String> roleList = List.of("ROLE_INSTRUCTOR", "ROLE_PD", "ROLE_PRINCIPAL", "ROLE_MANAGER", "ROLE_STAFF", "ROLE_USER", "ROLE_STUDENT", "ROLE_PARENT");

        // 관리자 계열 권한들
        List<String> adminRoles = List.of("ROLE_INSTRUCTOR", "ROLE_PD", "ROLE_PRINCIPAL", "ROLE_MANAGER", "ROLE_STAFF");

        for (int i=2; i<101; i++) {

            // 사용자 ID 포맷팅
            String userId = String.format("testuser%02d", i);

            // roleList 랜덤 추출
            int randomIndex = faker.random().nextInt(roleList.size());
            String randomRole = roleList.get(randomIndex);

            // 추출된 권한이 관리자 리스트에 있는지 체크
            if (adminRoles.contains(randomRole)) {
                // 관리자 권한인 경우 2개의 권한 insert

                // ADMIN 부여
                memberMapper.fakerRoleMapping(userId, "ROLE_ADMIN");
                log.info("[ADMIN 그룹 - 1] USERID : {}, ROLE : ROLE_ADMIN", userId);
            
                // 두 번째: 랜덤으로 뽑힌 상세 관리자 권한 부여
                memberMapper.fakerRoleMapping(userId, randomRole);
                log.info("[ADMIN 그룹 - 2] USERID : {}, ROLE : {}", userId, randomRole);
            } else {
                // 일반 사용자 권한인 경우 1개의 권한 insert
                memberMapper.fakerRoleMapping(userId, randomRole);
                log.info("[USER 그룹 - 단일] USERID : {}, ROLE : {}", userId, randomRole);
            }
        }
    }
}
