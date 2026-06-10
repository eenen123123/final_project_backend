alter table course drop column subj_cl_id; -- 과목 대분류 ID 컬럼 제거

alter table course drop column atch_file_id; -- 첨부파일 ID 컬럼 제거
alter table course drop column prod_mthd_cd; -- 제작 방식 컬럼 제거
alter table course drop column prereq_course_sn; -- 선수과목 컬럼 제거