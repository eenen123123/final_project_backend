-- ============================================================
-- 1. MEMBER 테이블 INSERT (먼저 실행)
-- ============================================================
insert into member (
   user_id,
   user_enpswd,
   user_nm,
   user_enrrno,
   user_gndr_cd,
   user_brdt,
   join_dt,
   user_telno,
   user_eml_addr,
   user_zip,
   user_addr,
   user_daddr,
   prfl_img_path_nm,
   reg_dt,
   mdfcn_dt
) values ( 'testuser01',                          -- USER_ID       (최대 20자)
           'ENC_DUMMY_PASSWORD_256BIT',           -- USER_ENPSWD   (암호화된 값 자리)
           '홍길동',                               -- USER_NM
           null,                                  -- USER_ENRRNO   (주민번호 없으면 NULL)
           'M',                                   -- USER_GNDR_CD  M/F/U
           '19900101',                            -- USER_BRDT     YYYYMMDD
           current_timestamp,                     -- JOIN_DT
           '01012345678',                         -- USER_TELNO
           'hong@test.com',                       -- USER_EML_ADDR
           '06292',                               -- USER_ZIP      (5자리)
           '서울특별시 강남구 테헤란로 123',          -- USER_ADDR
           '456호',                               -- USER_DADDR
           '/images/profile/testuser01.png',      -- PRFL_IMG_PATH_NM
           current_timestamp,                     -- REG_DT
           current_timestamp                      -- MDFCN_DT
            );

-- ============================================================
-- 2. MEMBER_ROLE 테이블 INSERT (MEMBER 다음 실행)
-- ============================================================
insert into member_role (
   user_id,
   authrt_cd,
   authrt_nm,
   mdfr_id,
   mdfcn_dt,
   frst_reg_dt,
   frst_rgtr_id,
   last_mdfr_id,
   authrt_expln,
   use_yn,
   last_mdfcn_dt
) values ( 'testuser01',                          -- USER_ID       (MEMBER 참조)
           'USER',                                -- AUTHRT_CD     ADMIN / USER / GUEST
           '일반 사용자',                          -- AUTHRT_NM
           null,                                  -- MDFR_ID       (최초 등록이라 NULL)
           null,                                  -- MDFCN_DT      (수정 없으면 NULL)
           current_timestamp,                     -- FRST_REG_DT
           'testuser01',                          -- FRST_RGTR_ID  (본인이 등록)
           null,                                  -- LAST_MDFR_ID
           '기본 사용자 권한입니다.',               -- AUTHRT_EXPLN
           'Y',                                   -- USE_YN        Y / N
           null                                   -- LAST_MDFCN_DT
            );

-- ============================================================
-- 3. 커밋
-- ============================================================
commit;

-- ============================================================
-- 4. 확인 쿼리
-- ============================================================
select m.user_id,
       m.user_nm,
       mr.authrt_cd,
       mr.authrt_nm,
       mr.use_yn
  from member m
  join member_role mr
on m.user_id = mr.user_id
 where m.user_id = 'testuser01';