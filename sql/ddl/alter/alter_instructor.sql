-- INSTRUCTOR 테이블에 강사 공개 프로필 이미지 컬럼 추가
-- (MEMBER.USER_PROFILE은 계정 프로필 사진 전용이므로 별도 저장)
alter table instructor add (
   instr_profile_img varchar2(500)
);

-- 컬럼 추가
alter table instructor add instr_uuid varchar2(36);

-- 기존 강사 데이터에 UUID 채우기
update instructor
   set
   instr_uuid = regexp_replace(
      lower(rawtohex(sys_guid())),
      '(.{8})(.{4})(.{4})(.{4})(.{12})',
      '\1-\2-\3-\4-\5'
   );
commit;

-- UNIQUE 제약 추가
alter table instructor add constraint uq_instr_uuid unique ( instr_uuid );