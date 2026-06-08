-- SUBJECT_CLASSIFICATION (대분류)
insert into subject_classification (
   subj_cl_nm,
   use_yn,
   rgtr_id
) values ( '국어',
           'Y',
           'admin' );
insert into subject_classification (
   subj_cl_nm,
   use_yn,
   rgtr_id
) values ( '수학',
           'Y',
           'admin' );
insert into subject_classification (
   subj_cl_nm,
   use_yn,
   rgtr_id
) values ( '영어',
           'Y',
           'admin' );
insert into subject_classification (
   subj_cl_nm,
   use_yn,
   rgtr_id
) values ( '사회탐구',
           'Y',
           'admin' );
insert into subject_classification (
   subj_cl_nm,
   use_yn,
   rgtr_id
) values ( '과학탐구',
           'Y',
           'admin' );

-- SUBJECT (과목) — SUBJ_CL_ID는 위에서 채번된 실제 값으로 맞춰주세요
-- 국어 (SUBJ_CL_ID = 1)
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 1,
           '독서',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 1,
           '문학',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 1,
           '화법과 작문',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 1,
           '언어와 매체',
           'admin' );

-- 수학 (SUBJ_CL_ID = 2)
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 2,
           '수학Ⅰ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 2,
           '수학Ⅱ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 2,
           '확률과 통계',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 2,
           '미적분',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 2,
           '기하',
           'admin' );

-- 영어 (SUBJ_CL_ID = 3)
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 3,
           '영어Ⅰ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 3,
           '영어Ⅱ',
           'admin' );

-- 사회탐구 (SUBJ_CL_ID = 4)
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '생활과 윤리',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '윤리와 사상',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '한국지리',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '세계지리',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '동아시아사',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '세계사',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '경제',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '정치와 법',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 4,
           '사회·문화',
           'admin' );

-- 과학탐구 (SUBJ_CL_ID = 5)
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 5,
           '물리학Ⅰ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 5,
           '화학Ⅰ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 5,
           '생명과학Ⅰ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 5,
           '지구과학Ⅰ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 5,
           '물리학Ⅱ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 5,
           '화학Ⅱ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 5,
           '생명과학Ⅱ',
           'admin' );
insert into subject (
   subj_cl_id,
   subj_nm,
   rgtr_id
) values ( 5,
           '지구과학Ⅱ',
           'admin' );

commit;