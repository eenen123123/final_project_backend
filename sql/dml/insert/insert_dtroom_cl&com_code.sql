-- COM_CL 에 자료실 분류 추가
insert into com_cl (
   cl_code,cl_cd_nm,cl_cd_expln,use_yn,rgtr_id
) values ( '106','자료실카테고리','자료실 카테고리','Y','admin' );
insert into com_cl (
   cl_code,cl_cd_nm,cl_cd_expln,use_yn,rgtr_id
) values ( '107','접근제한','자료실 접근 제한','Y','admin' );

-- COM_CD 에 자료실 카테고리 추가
insert into com_cd (
   com_cd,cl_code,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '01','106','공지/안내자료','공지 및 안내 자료','Y','admin' );
insert into com_cd (
   com_cd,cl_code,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '02','106','입시정보','입시 관련 정보','Y','admin' );
insert into com_cd (
   com_cd,cl_code,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '03','106','학습자료','학습 관련 자료','Y','admin' );
insert into com_cd (
   com_cd,cl_code,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '04','106','기타','기타','Y','admin' );

-- COM_CD 에 접근제한 추가
insert into com_cd (
   com_cd,cl_code,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '01','107','전체공개','전체 공개','Y','admin' );
insert into com_cd (
   com_cd,cl_code,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '02','107','회원전용','회원 전용','Y','admin' );

commit;