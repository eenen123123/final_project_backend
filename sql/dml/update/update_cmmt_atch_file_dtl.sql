-- 1. 강사 일반 게시판 (CLASS_SN IS NULL)
update cmmt_atch_file_dtl f
   set ctx_type = 'INSTRUCTOR_BOARD',
       ctx_id = (
          select to_char(ib.post_sn)
            from instructor_board ib
           where ib.atch_file_id = to_number(f.ctx_id)
             and ib.class_sn is null
       )
 where f.ctx_type = 'INSTRUCTOR'
   and exists (
   select 1
     from instructor_board ib
    where ib.atch_file_id = to_number(f.ctx_id)
      and ib.class_sn is null
);

-- 2. 클래스룸 공지사항
update cmmt_atch_file_dtl f
   set ctx_type = 'CLASSROOM_NOTICE',
       ctx_id = (
          select to_char(ib.post_sn)
            from instructor_board ib
           where ib.atch_file_id = to_number(f.ctx_id)
             and ib.class_sn is not null
             and ib.board_type_cd = 'NOTICE'
       )
 where f.ctx_type = 'INSTRUCTOR'
   and exists (
   select 1
     from instructor_board ib
    where ib.atch_file_id = to_number(f.ctx_id)
      and ib.class_sn is not null
      and ib.board_type_cd = 'NOTICE'
);

-- 3. 클래스룸 자료실
update cmmt_atch_file_dtl f
   set ctx_type = 'CLASSROOM_DATAROOM',
       ctx_id = (
          select to_char(ib.post_sn)
            from instructor_board ib
           where ib.atch_file_id = to_number(f.ctx_id)
             and ib.class_sn is not null
             and ib.board_type_cd = 'DATAROOM'
       )
 where f.ctx_type = 'INSTRUCTOR'
   and exists (
   select 1
     from instructor_board ib
    where ib.atch_file_id = to_number(f.ctx_id)
      and ib.class_sn is not null
      and ib.board_type_cd = 'DATAROOM'
);

commit;