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

-- 3. DEFAULT 값 변경
alter table classroom modify
   class_stat_cd default 'WAITING';

-- 4. CHECK 제약조건 추가
alter table classroom
   add constraint chk_class_stat_cd
      check ( class_stat_cd in ( 'RECRUITING',
                                 'ACTIVE',
                                 'CLOSED',
                                 'WAITING' ) );

-- 5. 컬럼 코멘트 업데이트
comment on column classroom.class_stat_cd is
   '클래스룸 상태코드 (RECRUITING=모집중, ACTIVE=운영중, CLOSED=종료, WAITING=대기)';