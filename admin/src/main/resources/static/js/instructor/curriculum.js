let selectedCurriculumId = null;
let selectedCourseSn = null;
let editingCurriculumId = null;
let editingCourseSn = null;
let editingLectureSn = null;
let courseCache = [];
let lectureCache = [];

// ─── Curriculum ──────────────────────────────────────────────

function selectCurriculum(curriculumId) {
    if (selectedCurriculumId === curriculumId) return;
    selectedCurriculumId = curriculumId;
    selectedCourseSn = null;

    document.querySelectorAll('.curriculum-item').forEach(el => el.classList.remove('active'));
    const el = document.getElementById('curriculum-item-' + curriculumId);
    if (el) el.classList.add('active');

    document.getElementById('btn-add-course').disabled = false;
    resetLecturePanel();
    loadCourses(curriculumId);
}

function showCurriculumForm() {
    editingCurriculumId = null;
    document.getElementById('curriculum-title-input').value = '';
    document.getElementById('curriculum-form').classList.remove('hidden');
    document.getElementById('curriculum-title-input').focus();
}

function editCurriculum(event, curriculumId) {
    event.stopPropagation();
    editingCurriculumId = curriculumId;
    const li = document.getElementById('curriculum-item-' + curriculumId);
    const title = li ? (li.dataset.title || '') : '';
    document.getElementById('curriculum-title-input').value = title;
    document.getElementById('curriculum-form').classList.remove('hidden');
    document.getElementById('curriculum-title-input').focus();
}

function hideCurriculumForm() {
    editingCurriculumId = null;
    document.getElementById('curriculum-title-input').value = '';
    document.getElementById('curriculum-form').classList.add('hidden');
}

function submitCurriculumForm() {
    const title = document.getElementById('curriculum-title-input').value.trim();
    if (!title) { alert('커리큘럼 제목을 입력하세요.'); return; }

    const url = editingCurriculumId
        ? `/instructor/curriculum/modify/${editingCurriculumId}`
        : '/instructor/curriculum/save';
    const method = editingCurriculumId ? 'PUT' : 'POST';

    fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ title }),
    }).then(res => {
        if (!res.ok) throw new Error();
        location.reload();
    }).catch(() => alert('저장에 실패했습니다.'));
}

function deleteCurriculum(event, curriculumId) {
    event.stopPropagation();
    if (!confirm('커리큘럼을 삭제하시겠습니까?')) return;
    fetch(`/instructor/curriculum/delete/${curriculumId}`, { method: 'DELETE' })
        .then(res => {
            if (!res.ok) throw new Error();
            location.reload();
        }).catch(() => alert('삭제에 실패했습니다.'));
}

// ─── Course ──────────────────────────────────────────────────

function loadCourses(curriculumId) {
    const list = document.getElementById('course-list');
    const placeholder = document.getElementById('course-placeholder');
    hideCourseForm();

    list.innerHTML = '<li class="panel-loading">불러오는 중…</li>';
    list.classList.remove('hidden');
    placeholder.classList.add('hidden');

    fetch(`/instructor/curriculum/${curriculumId}/courses`)
        .then(res => res.json())
        .then(data => { courseCache = data; renderCourseList(data); })
        .catch(() => {
            list.innerHTML = '<li class="panel-empty-state"><p>강좌를 불러오지 못했습니다.</p></li>';
        });
}

function renderCourseList(courses) {
    const list = document.getElementById('course-list');
    const badge = document.getElementById('course-badge');
    badge.textContent = courses.length;
    badge.classList.remove('hidden');

    if (courses.length === 0) {
        list.innerHTML = `<li class="panel-empty-state">
            <i class="fa-solid fa-inbox text-slate-300 text-2xl mb-2"></i>
            <p>등록된 강좌가 없습니다.</p>
        </li>`;
        return;
    }

    list.innerHTML = courses.map(c => `
        <li id="course-item-${c.courseSn}" class="course-item panel-item" onclick="selectCourse(${c.courseSn})">
            <div class="min-w-0 flex-1">
                <div class="item-title">${escHtml(c.courseNm)}</div>
                ${c.courseExplnCn ? `<div class="item-meta">${escHtml(c.courseExplnCn)}</div>` : ''}
            </div>
            <div class="item-actions">
                <span class="badge-opnn ${c.opnnYn === 'Y' ? 'badge-opnn-open' : 'badge-opnn-closed'}">${c.opnnYn === 'Y' ? '공개' : '비공개'}</span>
                <button class="btn-icon" onclick="showCourseEdit(event,${c.courseSn})" title="수정"><i class="fa-solid fa-pen text-[10px]"></i></button>
                <button class="btn-icon btn-icon-danger" onclick="deleteCourse(event,${c.courseSn})" title="삭제"><i class="fa-solid fa-trash text-[10px]"></i></button>
            </div>
        </li>
    `).join('');
}

function selectCourse(courseSn) {
    if (selectedCourseSn === courseSn) return;
    selectedCourseSn = courseSn;

    document.querySelectorAll('.course-item').forEach(el => el.classList.remove('active'));
    const el = document.getElementById('course-item-' + courseSn);
    if (el) el.classList.add('active');

    document.getElementById('btn-add-lecture').disabled = false;
    loadLectures(courseSn);
}

function showCourseForm() {
    editingCourseSn = null;
    document.getElementById('course-nm-input').value = '';
    document.getElementById('course-expln-input').value = '';
    document.getElementById('course-opnn-input').value = 'Y';
    document.getElementById('course-form').classList.remove('hidden');
    document.getElementById('course-nm-input').focus();
}

function showCourseEdit(event, courseSn) {
    event.stopPropagation();
    const course = courseCache.find(c => c.courseSn === courseSn);
    if (!course) return;
    editingCourseSn = courseSn;
    document.getElementById('course-nm-input').value = course.courseNm || '';
    document.getElementById('course-expln-input').value = course.courseExplnCn || '';
    document.getElementById('course-opnn-input').value = course.opnnYn || 'Y';
    document.getElementById('course-form').classList.remove('hidden');
    document.getElementById('course-nm-input').focus();
}

function hideCourseForm() {
    editingCourseSn = null;
    document.getElementById('course-form').classList.add('hidden');
}

function submitCourseForm() {
    const courseNm = document.getElementById('course-nm-input').value.trim();
    if (!courseNm) { alert('강좌명을 입력하세요.'); return; }

    const body = {
        courseNm,
        courseExplnCn: document.getElementById('course-expln-input').value.trim() || null,
        opnnYn: document.getElementById('course-opnn-input').value,
    };

    const url = editingCourseSn
        ? `/instructor/curriculum/courses/${editingCourseSn}`
        : `/instructor/curriculum/${selectedCurriculumId}/courses`;
    const method = editingCourseSn ? 'PUT' : 'POST';

    fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
        .then(res => {
            if (!res.ok) throw new Error();
            hideCourseForm();
            loadCourses(selectedCurriculumId);
        }).catch(() => alert('저장에 실패했습니다.'));
}

function deleteCourse(event, courseSn) {
    event.stopPropagation();
    if (!confirm('강좌를 삭제하시겠습니까?')) return;
    fetch(`/instructor/curriculum/courses/${courseSn}`, { method: 'DELETE' })
        .then(res => {
            if (!res.ok) throw new Error();
            if (selectedCourseSn === courseSn) resetLecturePanel();
            loadCourses(selectedCurriculumId);
        }).catch(() => alert('삭제에 실패했습니다.'));
}

// ─── Lecture ─────────────────────────────────────────────────

function loadLectures(courseSn) {
    const list = document.getElementById('lecture-list');
    const placeholder = document.getElementById('lecture-placeholder');
    hideLectureForm();

    list.innerHTML = '<li class="panel-loading">불러오는 중…</li>';
    list.classList.remove('hidden');
    placeholder.classList.add('hidden');

    fetch(`/instructor/curriculum/courses/${courseSn}/lectures`)
        .then(res => res.json())
        .then(data => { lectureCache = data; renderLectureList(data); })
        .catch(() => {
            list.innerHTML = '<li class="panel-empty-state"><p>강의를 불러오지 못했습니다.</p></li>';
        });
}

function renderLectureList(lectures) {
    const list = document.getElementById('lecture-list');
    const badge = document.getElementById('lecture-badge');
    badge.textContent = lectures.length;
    badge.classList.remove('hidden');

    const typeLabel = { VIDEO: '영상', LIVE: '라이브', DOC: '자료', QUIZ: '퀴즈' };

    if (lectures.length === 0) {
        list.innerHTML = `<li class="panel-empty-state">
            <i class="fa-solid fa-inbox text-slate-300 text-2xl mb-2"></i>
            <p>등록된 강의가 없습니다.</p>
        </li>`;
        return;
    }

    list.innerHTML = lectures.map(l => `
        <li id="lecture-item-${l.lectureSn}" class="panel-item">
            <div class="min-w-0 flex-1">
                <div class="item-title">
                    ${escHtml(l.lectureNm)}
                    ${l.lockYn === 'Y' ? '<i class="fa-solid fa-lock text-[9px] text-slate-400 ml-1"></i>' : ''}
                </div>
                <div class="item-meta flex items-center gap-1.5">
                    ${l.lectureTypeCd ? `<span class="badge-type">${typeLabel[l.lectureTypeCd] || l.lectureTypeCd}</span>` : ''}
                    ${l.lectureDuration ? `<span>${l.lectureDuration}분</span>` : ''}
                </div>
            </div>
            <div class="item-actions">
                <span class="badge-opnn ${l.opnnYn === 'Y' ? 'badge-opnn-open' : 'badge-opnn-closed'}">${l.opnnYn === 'Y' ? '공개' : '비공개'}</span>
                <button class="btn-icon" onclick="showLectureEdit(event,${l.lectureSn})" title="수정"><i class="fa-solid fa-pen text-[10px]"></i></button>
                <button class="btn-icon btn-icon-danger" onclick="deleteLecture(event,${l.lectureSn})" title="삭제"><i class="fa-solid fa-trash text-[10px]"></i></button>
            </div>
        </li>
    `).join('');
}

function showLectureForm() {
    editingLectureSn = null;
    document.getElementById('lecture-nm-input').value = '';
    document.getElementById('lecture-type-input').value = '';
    document.getElementById('lecture-duration-input').value = '';
    document.getElementById('lecture-expln-input').value = '';
    document.getElementById('lecture-opnn-input').value = 'Y';
    document.getElementById('lecture-lock-input').value = 'N';
    document.getElementById('lecture-form').classList.remove('hidden');
    document.getElementById('lecture-nm-input').focus();
}

function showLectureEdit(event, lectureSn) {
    event.stopPropagation();
    const lecture = lectureCache.find(l => l.lectureSn === lectureSn);
    if (!lecture) return;
    editingLectureSn = lectureSn;
    document.getElementById('lecture-nm-input').value = lecture.lectureNm || '';
    document.getElementById('lecture-type-input').value = lecture.lectureTypeCd || '';
    document.getElementById('lecture-duration-input').value = lecture.lectureDuration || '';
    document.getElementById('lecture-expln-input').value = lecture.lectureExplnCn || '';
    document.getElementById('lecture-opnn-input').value = lecture.opnnYn || 'Y';
    document.getElementById('lecture-lock-input').value = lecture.lockYn || 'N';
    document.getElementById('lecture-form').classList.remove('hidden');
    document.getElementById('lecture-nm-input').focus();
}

function hideLectureForm() {
    editingLectureSn = null;
    document.getElementById('lecture-form').classList.add('hidden');
}

function submitLectureForm() {
    const lectureNm = document.getElementById('lecture-nm-input').value.trim();
    if (!lectureNm) { alert('강의명을 입력하세요.'); return; }

    const durationRaw = document.getElementById('lecture-duration-input').value;
    const body = {
        lectureNm,
        lectureTypeCd: document.getElementById('lecture-type-input').value || null,
        lectureDuration: durationRaw ? parseInt(durationRaw) : null,
        lectureExplnCn: document.getElementById('lecture-expln-input').value.trim() || null,
        opnnYn: document.getElementById('lecture-opnn-input').value,
        lockYn: document.getElementById('lecture-lock-input').value,
    };

    const url = editingLectureSn
        ? `/instructor/curriculum/courses/lectures/${editingLectureSn}`
        : `/instructor/curriculum/courses/${selectedCourseSn}/lectures`;
    const method = editingLectureSn ? 'PUT' : 'POST';

    fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) })
        .then(res => {
            if (!res.ok) throw new Error();
            hideLectureForm();
            loadLectures(selectedCourseSn);
        }).catch(() => alert('저장에 실패했습니다.'));
}

function deleteLecture(event, lectureSn) {
    event.stopPropagation();
    if (!confirm('강의를 삭제하시겠습니까?')) return;
    fetch(`/instructor/curriculum/courses/lectures/${lectureSn}`, { method: 'DELETE' })
        .then(res => {
            if (!res.ok) throw new Error();
            loadLectures(selectedCourseSn);
        }).catch(() => alert('삭제에 실패했습니다.'));
}

// ─── Util ────────────────────────────────────────────────────

function resetLecturePanel() {
    selectedCourseSn = null;
    lectureCache = [];
    document.getElementById('btn-add-lecture').disabled = true;
    document.getElementById('lecture-list').classList.add('hidden');
    document.getElementById('lecture-placeholder').classList.remove('hidden');
    document.getElementById('lecture-badge').classList.add('hidden');
    hideLectureForm();
}

