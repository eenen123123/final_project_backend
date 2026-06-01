update com_cd
   set
   com_cd_nm = '공지사항'
 where cl_code = '100'
   and com_cd = '02';

update com_cd
   set
   com_cd_nm = '자료실'
 where cl_code = '100'
   and com_cd = '04';

update com_cd
   set
   com_cd_nm = 'NOTICE'
 where cl_code = '100'
   and com_cd = '02';

update com_cd
   set
   com_cd_nm = 'DATA_ROOM'
 where cl_code = '100'
   and com_cd = '04';

select *
  from com_cd
 where cl_code = '100';

commit;
rollback;