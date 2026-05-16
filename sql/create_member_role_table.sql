-- ============================================================
-- 권한 관리 테이블 생성 (MEMBER_ROLE)
-- ============================================================
create table member_role (
   authrt_sn     number(22) generated always as identity, -- 권한일련번호 (PK) | BIGINT → NUMBER(22)
   user_id       varchar2(20) not null,                     -- 사용자아이디 (FK) | MEMBER 참조
   authrt_cd     varchar2(20) not null,                     -- 권한코드 | ADMIN / USER / GUEST 등
   authrt_nm     varchar2(100) null,                         -- 권한명
   mdfr_id       varchar2(20) null,                         -- 수정자아이디
   mdfcn_dt      timestamp null,                         -- 수정일시
   frst_reg_dt   timestamp default current_timestamp not null, -- 최초등록일시
   frst_rgtr_id  varchar2(20) not null,                     -- 최초등록자아이디
   last_mdfr_id  varchar2(20) null,                         -- 최종수정자아이디
    -- ── 신규 추가 권장 컬럼 ──────────────────────────────────
   authrt_expln  varchar2(4000) null,                         -- 권한설명 (운영 시 필수)
   use_yn        char(1) default 'Y' not null,         -- 사용여부 | Y:사용 / N:미사용
   last_mdfcn_dt timestamp null,                         -- 최종수정일시 (이력 추적용)

    -- Primary Key
   constraint pk_member_role primary key ( authrt_sn ),

    -- Foreign Key
   constraint fk_member_role_user foreign key ( user_id )
      references member ( user_id ),

    -- Check 제약
   constraint ck_member_role_use_yn check ( use_yn in ( 'Y',
                                                        'N' ) )
);

-- ============================================================
-- 인덱스 (USER_ID 기준 조회 최적화)
-- ============================================================
create index idx_member_role_user_id on
   member_role (
      user_id
   );

-- ============================================================
-- 테이블 / 컬럼 코멘트
-- ============================================================
comment on table member_role is
   '권한 관리';

comment on column member_role.authrt_sn is
   '권한일련번호 (PK, AUTO INCREMENT)';
comment on column member_role.user_id is
   '사용자아이디 (FK → MEMBER.USER_ID)';
comment on column member_role.authrt_cd is
   '권한코드 (ADMIN/USER/GUEST 등)';
comment on column member_role.authrt_nm is
   '권한명';
comment on column member_role.mdfr_id is
   '수정자아이디';
comment on column member_role.mdfcn_dt is
   '수정일시';
comment on column member_role.frst_reg_dt is
   '최초등록일시';
comment on column member_role.frst_rgtr_id is
   '최초등록자아이디';
comment on column member_role.last_mdfr_id is
   '최종수정자아이디';
comment on column member_role.authrt_expln is
   '권한설명';
comment on column member_role.use_yn is
   '사용여부 (Y:사용, N:미사용)';
comment on column member_role.last_mdfcn_dt is
   '최종수정일시 (이력 추적용)';