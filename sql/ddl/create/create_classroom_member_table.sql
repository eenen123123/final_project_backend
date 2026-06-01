-- 시퀀스
create sequence seq_classroom_member start with 1 increment by 1 nocache nocycle;

-- 테이블
create table classroom_member (
   enrl_sn      number(20,0) not null,
   class_sn     number(20,0) not null,
   user_id      varchar2(20 byte) not null,
   enrl_stat_cd char(2 byte) default '01' not null,
   rgtr_id      varchar2(20 byte),
   reg_dt       timestamp default current_timestamp,
   last_mdfr_id varchar2(20 byte),
   mdfcn_dt     timestamp default current_timestamp,
   constraint pk_classroom_member primary key ( enrl_sn ),
   constraint fk_cm_class_sn foreign key ( class_sn )
      references classroom ( class_sn ),
   constraint fk_cm_user_id foreign key ( user_id )
      references member ( user_id ),
   constraint uq_cm_class_user unique ( class_sn,
                                        user_id ),
   constraint ck_cm_enrl_stat
      check ( enrl_stat_cd in ( '01',
                                '02',
                                '03',
                                '04' ) )
);

-- 컬럼 코멘트
comment on column classroom_member.enrl_sn is
   'PK · 시퀀스';
comment on column classroom_member.class_sn is
   'CLASSROOM.CLASS_SN 참조';
comment on column classroom_member.user_id is
   'MEMBER.USER_ID 참조';
comment on column classroom_member.enrl_stat_cd is
   '수강 상태 (01: 수강중, 02: 수강완료, 03: 중도탈퇴, 04: 등록취소)';
comment on column classroom_member.rgtr_id is
   '최초등록자ID';
comment on column classroom_member.reg_dt is
   '최초등록시점';
comment on column classroom_member.last_mdfr_id is
   '최종수정자ID';
comment on column classroom_member.mdfcn_dt is
   '최종수정시점';

commit;