alter table exam add class_sn number(19);
alter table exam
   add constraint fk_exam_class foreign key ( class_sn )
      references classroom ( class_sn );