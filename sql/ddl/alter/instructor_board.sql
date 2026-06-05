-- USE_YN 컬럼 추가 (기존 데이터는 DEFAULT 'Y' 로 자동 설정)
alter table instructor_board add use_yn char(1) default 'Y' not null;

-- POST_CN 컬럼을 VARCHAR2(4000) → CLOB 으로 변경
-- Oracle은 VARCHAR2 → CLOB 직접 MODIFY 불가하므로 아래 4단계로 처리
-- 1) CLOB 타입 임시 컬럼 추가
alter table instructor_board add post_cn_clob clob;

-- 2) 기존 데이터 복사
update instructor_board
   set
   post_cn_clob = post_cn;
commit;

-- 3) 기존 VARCHAR2 컬럼 삭제
alter table instructor_board drop column post_cn;

-- 4) 임시 컬럼 이름을 원래 이름으로 변경
alter table instructor_board rename column post_cn_clob to post_cn;

commit;

alter table instructor_board add class_sn number(20) null
   references classroom ( class_sn );