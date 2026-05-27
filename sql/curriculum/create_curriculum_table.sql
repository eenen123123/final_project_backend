create table curriculum_master (
   curriculum_id number primary key,         -- 커리큘럼 고유 번호 (시퀀스 사용)
   title         varchar2(200) not null,     -- 커리큘럼 제목 (예: 웹 개발자 양성 과정)
   instructor_id varchar2(50) not null,      -- 담당 강사 ID (외래키 역할)
   use_yn        char(1) default 'Y',        -- 삭제 여부 ('Y': 노출, 'N': 소프트 딜리트)
    
    -- 이력 관리 공통 컬럼
   rgtr_id       varchar2(50) not null,      -- 최초등록자ID
   reg_dt        date default sysdate,       -- 최초등록시점
   last_mdfr_id  varchar2(50),               -- 최종수정자ID (등록 직후에는 NULL 가능)
   mdfcn_dt      date default sysdate        -- 최종수정시점
);

-- 테이블 및 컬럼 주석(COMMENT) 추가 (협업 시 필수)
comment on table curriculum_master is
   '커리큘럼 마스터 테이블';
comment on column curriculum_master.curriculum_id is
   '커리큘럼 마스터 PK';
comment on column curriculum_master.title is
   '커리큘럼 제목';
comment on column curriculum_master.instructor_id is
   '강사 ID';
comment on column curriculum_master.use_yn is
   '사용 여부(Y/N)';
comment on column curriculum_master.rgtr_id is
   '최초등록자ID';
comment on column curriculum_master.reg_dt is
   '최초등록시점';
comment on column curriculum_master.last_mdfr_id is
   '최종수정자ID';
comment on column curriculum_master.mdfcn_dt is
   '최종수정시점';


create table curriculum_detail (
   detail_id     number primary key,         -- 상세 행 고유 번호 (시퀀스 사용)
   curriculum_id number not null,            -- 마스터 테이블 연관 ID (FK)
   row_order     number not null,            -- 표에서 행의 순서 (그리드 정렬용 필수)
   week_info     varchar2(100),              -- 주차 정보 (예: 1주차)
   topic         varchar2(200),              -- 주제 (예: Java 기초)
   content       varchar2(4000),             -- 내용 (예: 변수, 조건문)
    
    -- 이력 관리 공통 컬럼
   rgtr_id       varchar2(50) not null,      -- 최초등록자ID
   reg_dt        date default sysdate,       -- 최초등록시점
   last_mdfr_id  varchar2(50),               -- 최종수정자ID
   mdfcn_dt      date default sysdate,       -- 최종수정시점
    
    -- 외래키 제약조건 설정 (마스터가 지워지거나 관리될 때 연쇄 작용 정의)
   constraint fk_curriculum_master foreign key ( curriculum_id )
      references curriculum_master ( curriculum_id )
);

comment on table curriculum_detail is
   '커리큘럼 상세(그리드 행) 테이블';
comment on column curriculum_detail.detail_id is
   '커리큘럼 상세 PK';
comment on column curriculum_detail.curriculum_id is
   '커리큘럼 마스터 FK';
comment on column curriculum_detail.row_order is
   '표 내부 행 순서';
comment on column curriculum_detail.week_info is
   '주차 정보';
comment on column curriculum_detail.topic is
   '강의 주제';
comment on column curriculum_detail.content is
   '상세 강의 내용';
comment on column curriculum_detail.rgtr_id is
   '최초등록자ID';
comment on column curriculum_detail.reg_dt is
   '최초등록시점';
comment on column curriculum_detail.last_mdfr_id is
   '최종수정자ID';
comment on column curriculum_detail.mdfcn_dt is
   '최종수정시점';


create sequence seq_curriculum_master start with 1 increment by 1;
create sequence seq_curriculum_detail start with 1 increment by 1;

commit;