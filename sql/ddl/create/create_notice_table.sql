-- 공지사항 게시판 테이블 생성
-- COM_CL 공통 코드 사용
-- 공지사항 유형 => CL_CODE: 103

create table notice (
   post_sn        number(20,0) not null,notice_type_cd char(2) null,constraint pk_notice primary key ( post_sn )
);

comment on column notice.post_sn is
   '기본키(PK) - BOARD 참조';
comment on column notice.notice_type_cd is
   'COM_CD 공통코드 참조 (CL_CODE: 103)';

-- 공통 코드 추가

insert into com_cl (
   cl_code,cl_cd_nm,cl_cd_expln,use_yn,rgtr_id
) values ( '103','공지사항유형','공지사항 게시판 유형','Y','admin' );

insert into com_cd (
   cl_code,com_cd,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '103','01','일반공지','일반 공지사항','Y','admin' );

insert into com_cd (
   cl_code,com_cd,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '103','02','이벤트','이벤트 공지사항','Y','admin' );

insert into com_cd (
   cl_code,com_cd,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '103','03','점검','시스템 점검 공지','Y','admin' );

commit;