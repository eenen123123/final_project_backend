-- 과목 테이블에 사용 여부 컬럼 추가
alter table subject add (
   use_yn char(1) default 'Y' not null
);