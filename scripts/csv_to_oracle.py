import oracledb
import csv
import os
import re
import glob
import unicodedata
from dotenv import load_dotenv

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))

# =============================================
# Oracle DB 연결 설정 - 값은 backend/.env 에서 가져옴
#   db_host / db_port / db_service / db_username / db_password
# =============================================
load_dotenv(os.path.join(SCRIPT_DIR, "..", ".env"))

DB_HOST = os.getenv("db_host")
DB_PORT = os.getenv("db_port")
DB_SERVICE = os.getenv("db_service")
DB_USER = os.getenv("db_username")
DB_PASSWORD = os.getenv("db_password")

# =============================================
# CSV 파일 경로 설정
# =============================================
# 파일명 규칙 예시 (연도는 뒤쪽 'YYYY학년도'):
#   수능:    대학수학능력시험 등급구분-표준점수_2026학년도.csv
#   6월모평: 6월 모의평가 등급구분-표준점수_2026학년도.csv
#   9월모평: 9월 모의평가 등급구분-표준점수_2026학년도.csv
CSV_DIR = os.path.join(SCRIPT_DIR, "data")

INSERT_SQL = """
    INSERT INTO SUNEUNG_GRADE_CUT
        (YEAR, EXAM_TYPE, SUBJECT, SUBJ_CL_ID, GRADE, CUT_SCORE, PEOPLE, RATIO)
    VALUES
        (:1, :2, :3, :4, :5, :6, :7, :8)
"""

# 과목명 표기 통일 - 공공데이터 연도별 표기 흔들림 보정 (최신 표기로 통일)
SUBJECT_ALIASES = {
    '한국 지리': '한국지리',
    '세계 지리': '세계지리',
}

# 과목 -> 대분류(SUBJ_CL_ID). 여기 없는 과목(제2외국어/한문, 직업탐구)은 import 시 제외됨.
SUBJECT_CLASSIFICATION = {
    # 국어 (1)
    '국어': 1,
    # 수학 (2) - 개편 전 가/나형 포함
    '수학': 2, '수학 가형': 2, '수학 나형': 2,
    # 영어 (3)
    '영어': 3,
    # 사회탐구 (4)
    '생활과 윤리': 4, '윤리와 사상': 4, '한국지리': 4, '세계지리': 4,
    '동아시아사': 4, '세계사': 4, '경제': 4, '정치와 법': 4, '사회·문화': 4,
    # 과학탐구 (5)
    '물리학 Ⅰ': 5, '화학 Ⅰ': 5, '생명과학 Ⅰ': 5, '지구과학 Ⅰ': 5,
    '물리학 Ⅱ': 5, '화학 Ⅱ': 5, '생명과학 Ⅱ': 5, '지구과학 Ⅱ': 5,
    # 한국사 (21)
    '한국사': 21,
}

def parse_int(val):
    """숫자 파싱 - 빈값, '-' 처리"""
    val = val.strip().replace(',', '')
    if val in ('', '-', 'N/A'):
        return None
    try:
        return int(float(val))
    except ValueError:
        return None

def parse_float(val):
    """소수 파싱 - 빈값, '-' 처리"""
    val = val.strip().replace(',', '')
    if val in ('', '-', 'N/A'):
        return None
    try:
        return float(val)
    except ValueError:
        return None

def get_exam_info_from_filename(filename):
    """
    파일명에서 연도(학년도), 시험구분 추출
    예: 대학수학능력시험 등급구분-표준점수_2026학년도.csv -> (2026, '수능')
        6월 모의평가 등급구분-표준점수_2021학년도.csv     -> (2021, '6월모평')
    """
    # macOS 파일명은 NFD라 'YYYY학년도' 매칭 위해 NFC로 정규화
    base = unicodedata.normalize('NFC', os.path.basename(filename))

    m = re.search(r'(\d{4})학년도', base)
    if not m:
        raise ValueError(f"파일명에서 연도(YYYY학년도)를 찾을 수 없음: {base}")
    year = int(m.group(1))

    # exam_type 은 Java ExamType enum 값과 일치 (VARCHAR2(10))
    if '6월' in base:
        exam_type = 'JUNE_MOCK'
    elif '9월' in base:
        exam_type = 'SEPT_MOCK'
    else:
        exam_type = 'CSAT'

    return year, exam_type

def open_csv(filepath):
    """인코딩이 파일마다 달라(cp949/utf-8) 순서대로 시도"""
    for enc in ('utf-8-sig', 'cp949'):
        try:
            f = open(filepath, encoding=enc)
            f.readline()
            f.seek(0)
            return f
        except UnicodeDecodeError:
            f.close()
    raise UnicodeError(f"인코딩을 판별할 수 없음: {filepath}")

def process_csv(filepath, cursor, year, exam_type):
    rows = []
    f = open_csv(filepath)
    with f:
        reader = csv.DictReader(f)
        for row in reader:
            grade     = parse_int(row.get('등급', ''))
            subject   = unicodedata.normalize('NFC', row.get('과목', '').strip())
            cut_score = parse_int(row.get('구분 점수', ''))
            people    = parse_int(row.get('인원(명)', ''))
            ratio     = parse_float(row.get('비율(퍼센트)', ''))

            # 표기 통일 후 대분류 매핑. 매핑에 없는 과목(제2외국어/한문, 직업탐구)은 제외
            subject = SUBJECT_ALIASES.get(subject, subject)
            subj_cl_id = SUBJECT_CLASSIFICATION.get(subject)

            if not subject or grade is None or subj_cl_id is None:
                continue

            rows.append((year, exam_type, subject, subj_cl_id, grade, cut_score, people, ratio))

    cursor.executemany(INSERT_SQL, rows)
    print(f"  {len(rows)}건 insert 완료")

def main():
    missing = [k for k, v in {
        "db_host": DB_HOST, "db_port": DB_PORT, "db_service": DB_SERVICE,
        "db_username": DB_USER, "db_password": DB_PASSWORD,
    }.items() if not v]
    if missing:
        print(f".env에 값이 비어있음: {', '.join(missing)}")
        return

    dsn = oracledb.makedsn(DB_HOST, int(DB_PORT), service_name=DB_SERVICE)
    connection = oracledb.connect(user=DB_USER, password=DB_PASSWORD, dsn=dsn)
    cursor = connection.cursor()

    csv_files = glob.glob(os.path.join(CSV_DIR, "*.csv"))
    if not csv_files:
        print(f"CSV 파일 없음: {CSV_DIR}")
        return

    for filepath in sorted(csv_files):
        print(f"처리중: {os.path.basename(filepath)}")
        try:
            year, exam_type = get_exam_info_from_filename(filepath)
            process_csv(filepath, cursor, year, exam_type)
            connection.commit()
        except Exception as e:
            print(f"  오류: {e}")
            connection.rollback()

    cursor.close()
    connection.close()
    print("완료")

if __name__ == "__main__":
    main()
