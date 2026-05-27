-- COM_CL 공통 코드 사용
-- QnA 게시판 답변상태 => CL_CODE: 104
-- QnA 게시판 카테고리 => CL_CODE: 105

-- QnA 답변상태
insert into com_cl (
   cl_code,cl_cd_nm,cl_cd_expln,use_yn,rgtr_id
) values ( '104','QnA답변상태','QnA 답변 상태','Y','admin' );

insert into com_cd (
   cl_code,com_cd,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '104','01','답변대기','QnA 답변 대기','Y','admin' );

insert into com_cd (
   cl_code,com_cd,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '104','02','답변완료','QnA 답변 완료','Y','admin' );

-- QnA 카테고리
insert into com_cl (
   cl_code,cl_cd_nm,cl_cd_expln,use_yn,rgtr_id
) values ( '105','QnA카테고리','QnA 카테고리','Y','admin' );

insert into com_cd (
   cl_code,com_cd,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '105','01','수강문의','QnA 수강 문의','Y','admin' );

insert into com_cd (
   cl_code,com_cd,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '105','02','결제문의','QnA 결제 문의','Y','admin' );

insert into com_cd (
   cl_code,com_cd,com_cd_nm,com_cd_expln,use_yn,rgtr_id
) values ( '105','03','기타','QnA 기타 문의','Y','admin' );

commit;