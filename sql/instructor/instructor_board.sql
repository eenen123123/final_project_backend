alter table instructor_board add use_yn char(1) default 'Y' not null;
-- 기존 데이터는 DEFAULT 'Y'로 자동 설정됨
commit;