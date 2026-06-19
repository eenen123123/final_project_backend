-- 1. 컬럼 크기 확장
alter table classroom modify
   class_stat_cd varchar2(20);

-- 2. 기존 데이터 마이그레이션
update classroom
   set
   class_stat_cd =
      case class_stat_cd
         when '01' then
            'RECRUITING'
         when '02' then
            'ACTIVE'
         when '03' then
            'CLOSED'
         when '04' then
            'WAITING'
         else
            class_stat_cd
      end;
commit;

-- 3. CHECK 제약조건 추가
alter table classroom
   add constraint chk_class_stat_cd
      check ( class_stat_cd in ( 'RECRUITING',
                                 'ACTIVE',
                                 'CLOSED',
                                 'WAITING' ) );