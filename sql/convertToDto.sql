-- DTO 클래스 생성 쿼리
-- 사용법: 아래 두 DEFINE 값을 바꾼 뒤 F5(스크립트 실행)
--   TABLE_NAME : DB 테이블명 (대문자)         예) COURSE, BOARD, MEMBER
--   PKG_NAME   : dto 하위 패키지명 (소문자)   예) course, board, user

DEFINE TABLE_NAME = 'MEMBER'
DEFINE PKG_NAME   = 'member'

SELECT line FROM (

    -- 패키지 선언
    SELECT 0  AS sq, 'package kr.or.ddit.finalProject.dto.&PKG_NAME;'   AS line FROM DUAL
    UNION ALL SELECT  1, ''                                               FROM DUAL

    -- 임포트
    UNION ALL SELECT  2, 'import java.io.Serializable;'                   FROM DUAL
    UNION ALL SELECT  3, 'import java.time.LocalDate;'                    FROM DUAL
    UNION ALL SELECT  4, 'import java.time.LocalDateTime;'                FROM DUAL
    UNION ALL SELECT  5, ''                                               FROM DUAL
    UNION ALL SELECT  6, 'import lombok.AllArgsConstructor;'              FROM DUAL
    UNION ALL SELECT  7, 'import lombok.Builder;'                         FROM DUAL
    UNION ALL SELECT  8, 'import lombok.Data;'                            FROM DUAL
    UNION ALL SELECT  9, 'import lombok.NoArgsConstructor;'               FROM DUAL
    UNION ALL SELECT 10, ''                                               FROM DUAL

    -- 클래스 선언
    UNION ALL SELECT 11, '@Data'                                          FROM DUAL
    UNION ALL SELECT 12, '@Builder'                                       FROM DUAL
    UNION ALL SELECT 13, '@NoArgsConstructor'                             FROM DUAL
    UNION ALL SELECT 14, '@AllArgsConstructor'                            FROM DUAL
    UNION ALL SELECT 15,
        'public class ' ||
        REPLACE(INITCAP(REPLACE('&TABLE_NAME', '_', ' ')), ' ', '') ||
        'Dto implements Serializable {'
    FROM DUAL
    UNION ALL SELECT 16, '' FROM DUAL

    -- 필드 목록 (컬럼 순서대로)
    UNION ALL
    SELECT
        100 + c.COLUMN_ID AS sq,
        '    private ' ||
        CASE
            WHEN c.DATA_TYPE = 'NUMBER' AND NVL(c.DATA_PRECISION, 38) >= 10 THEN 'Long'
            WHEN c.DATA_TYPE = 'NUMBER'                                       THEN 'Integer'
            WHEN c.DATA_TYPE = 'DATE'                                         THEN 'LocalDate'
            WHEN c.DATA_TYPE LIKE 'TIMESTAMP%'                                THEN 'LocalDateTime'
            WHEN c.DATA_TYPE = 'BLOB'                                         THEN 'byte[]'
            ELSE 'String'
        END || ' ' ||
        REPLACE(SUBSTR(INITCAP('a' || c.COLUMN_NAME), 2), '_', '') || ';' ||
        CASE WHEN cc.COMMENTS IS NOT NULL THEN ' // ' || cc.COMMENTS ELSE '' END
        AS line
    FROM COLS c
    LEFT JOIN USER_COL_COMMENTS cc
        ON  cc.TABLE_NAME  = c.TABLE_NAME
        AND cc.COLUMN_NAME = c.COLUMN_NAME
    WHERE c.TABLE_NAME = '&TABLE_NAME'

    -- 닫는 괄호
    UNION ALL SELECT 9999, '}' FROM DUAL

) ORDER BY sq;
