-- 1. 컬럼 크기 확장 (Oracle)
alter table instructor_board modify (
   board_type_cd varchar2(20)
);

-- 2. 데이터 마이그레이션
update instructor_board
   set
   board_type_cd = 'NOTICE'
 where board_type_cd = '02';
update instructor_board
   set
   board_type_cd = 'QNA'
 where board_type_cd = '03';
update instructor_board
   set
   board_type_cd = 'DATAROOM'
 where board_type_cd = '04';
commit;