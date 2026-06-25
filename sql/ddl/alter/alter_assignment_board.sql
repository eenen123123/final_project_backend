-- 과제 재제출 가능 여부 컬럼 추가
alter table assignment_board add (
   resbmt_alld_yn char(1) default 'N' not null
);