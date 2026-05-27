-- CLASS: CLASS_STAT_CD → USE_YN 으로 교체
--    (데이터 없으니 DROP 후 재추가가 제일 깔끔)
alter table classroom drop column class_stat_cd;
alter table classroom add use_yn char(1) default 'Y' not null;
comment on column classroom.use_yn is
   '운영 여부 (Y: 운영중, N: 종료)';

commit;