update instructor_board
   set reg_dt = current_timestamp,
       mdfcn_dt = current_timestamp
 where post_sn = 999;

select *
  from instructor_board;

commit;