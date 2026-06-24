-- CLASSROOM_MEMBER.ENRL_STAT_CD: 숫자 코드 → enum 이름 변경

-- 1. 기존 체크 제약 제거
alter table classroom_member drop constraint ck_cm_enrl_stat;

-- 2. 컬럼 타입 변경 (char(2) → varchar2(10))
alter table classroom_member modify (
   enrl_stat_cd varchar2(10 byte) default 'ENROLLED'
);

-- 3. 기존 데이터 마이그레이션
update classroom_member
   set
   enrl_stat_cd = 'ENROLLED'
 where enrl_stat_cd = '01';
update classroom_member
   set
   enrl_stat_cd = 'COMPLETED'
 where enrl_stat_cd = '02';
update classroom_member
   set
   enrl_stat_cd = 'WITHDRAWN'
 where enrl_stat_cd = '03';
update classroom_member
   set
   enrl_stat_cd = 'CANCELLED'
 where enrl_stat_cd = '04';

-- 4. 새 체크 제약 추가
alter table classroom_member
   add constraint ck_cm_enrl_stat
      check ( enrl_stat_cd in ( 'ENROLLED',
                                'COMPLETED',
                                'WITHDRAWN',
                                'CANCELLED' ) );

-- 5. 컬럼 코멘트 갱신
comment on column classroom_member.enrl_stat_cd is
   '수강 상태 (ENROLLED: 수강중, COMPLETED: 수강완료, WITHDRAWN: 중도탈퇴, CANCELLED: 등록취소)';

commit;