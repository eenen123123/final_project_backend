-- CLASS: USE_YN → CLASS_STAT_CD 로 교체
--    (데이터 없으니 DROP 후 재추가가 제일 깔끔)
ALTER TABLE classroom DROP COLUMN use_yn;
ALTER TABLE classroom ADD class_stat_cd CHAR(2) DEFAULT '04' NOT NULL;
COMMENT ON COLUMN classroom.class_stat_cd IS
    '클래스룸 상태코드 (01=모집중, 02=운영중, 03=종료, 04=대기)';

COMMIT;
