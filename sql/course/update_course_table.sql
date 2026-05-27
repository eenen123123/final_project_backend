-- 1. COURSE: 커리큘럼 연결
alter table course add curriculum_id number(20,0);
alter table course
   add constraint fk_course_curriculum foreign key ( curriculum_id )
      references curriculum_master ( curriculum_id );
comment on column course.curriculum_id is
   '연결된 커리큘럼 마스터 ID';

-- 2. COURSE: 수강료 추가
alter table course add course_price number(10,0) default 0 not null;
comment on column course.course_price is
   '강좌 수강료 (무료 강좌는 0)';

commit;