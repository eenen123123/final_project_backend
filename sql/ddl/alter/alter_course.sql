alter table course drop column subj_cl_id; -- 과목 대분류 ID 컬럼 제거

alter table course drop column atch_file_id; -- 첨부파일 ID 컬럼 제거
alter table course drop column prod_mthd_cd; -- 제작 방식 컬럼 제거
alter table course drop column prereq_course_sn; -- 선수과목 컬럼 제거

-- 커리큘럼 내에서 SORT_ORD의 중복을 방지하기 위한 UNIQUE 제약 조건 추가
alter table course add constraint uq_course_curriculum_sort unique ( curriculum_id,
                                                                     sort_ord );