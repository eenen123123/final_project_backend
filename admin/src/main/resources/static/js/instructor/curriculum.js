let selectedCurriculumId = null;
let editingCurriculumId = null;

// ─── Curriculum ──────────────────────────────────────────────

function selectCurriculum(curriculumId) {
    if (selectedCurriculumId === curriculumId) return;
    selectedCurriculumId = curriculumId;

    document.querySelectorAll('.curriculum-item').forEach(el => el.classList.remove('active'));
    const el = document.getElementById('curriculum-item-' + curriculumId);
    if (el) el.classList.add('active');

    document.getElementById('btn-add-course').disabled = false;
    hideAddCourseForm();
    loadCourses(curriculumId);
}

function showCurriculumForm() {
    editingCurriculumId = null;
    document.getElementById('curriculum-title-input').value = '';
    document.getElementById('curriculum-strt-input').value = '';
    document.getElementById('curriculum-end-input').value = '';
    document.getElementById('curriculum-expln-input').value = '';
    document.getElementById('curriculum-form').classList.remove('hidden');
    document.getElementById('curriculum-title-input').focus();
}

function editCurriculum(event, curriculumId) {
    event.stopPropagation();
    editingCurriculumId = curriculumId;
    const li = document.getElementById('curriculum-item-' + curriculumId);
    if (!li) return;
    document.getElementById('curriculum-title-input').value = li.dataset.title || '';
    document.getElementById('curriculum-strt-input').value = li.dataset.strt || '';
    document.getElementById('curriculum-end-input').value = li.dataset.end || '';
    document.getElementById('curriculum-expln-input').value = li.dataset.expln || '';
    document.getElementById('curriculum-form').classList.remove('hidden');
    document.getElementById('curriculum-title-input').focus();
}

function hideCurriculumForm() {
    editingCurriculumId = null;
    document.getElementById('curriculum-form').classList.add('hidden');
}

function submitCurriculumForm() {
    const title = document.getElementById('curriculum-title-input').value.trim();
    if (!title) { alert('커리큘럼 제목을 입력하세요.'); return; }

    const body = {
        title,
        strtDt: document.getElementById('curriculum-strt-input').value || null,
        endDt: document.getElementById('curriculum-end-input').value || null,
        explnCn: document.getElementById('curriculum-expln-input').value.trim() || null,
    };

    const url = editingCurriculumId
        ? `/instructor/curriculum/modify/${editingCurriculumId}`
        : '/instructor/curriculum/save';
    const method = editingCurriculumId ? 'PUT' : 'POST';

    fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(body),
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

// ─── Course (매핑) ────────────────────────────────────────────

function loadCourses(curriculumId) {
    const list = document.getElementById('course-list');
    const placeholder = document.getElementById('course-placeholder');
    hideAddCourseForm();

    list.innerHTML = '<li class="panel-loading">불러오는 중…</li>';
    list.classList.remove('hidden');
    placeholder.classList.add('hidden');

    fetch(`/instructor/curriculum/${curriculumId}/courses`)
        .then(res => res.json())
        .then(data => renderCourseList(data))
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

    list.innerHTML = courses.map((c, idx) => `
        <li id="course-item-${c.courseSn}" class="panel-item">
            <div class="course-sort-ord">${idx + 1}</div>
            <div class="min-w-0 flex-1">
                <a href="/admin/course/detail?courseSn=${c.courseSn}"
                   class="item-title hover:text-orange-500 transition-colors"
                   onclick="event.stopPropagation()"
                   target="_blank">
                    ${escHtml(c.courseNm)}
                    <i class="fa-solid fa-arrow-up-right-from-square text-[9px] text-slate-300 ml-1"></i>
                </a>
            </div>
            <div class="item-actions">
                <span class="badge-opnn ${c.opnnYn === 'Y' ? 'badge-opnn-open' : 'badge-opnn-closed'}">
                    ${c.opnnYn === 'Y' ? '공개' : '비공개'}
                </span>
                <button class="btn-icon" title="위로"
                        onclick="moveCourse(event, ${c.courseSn}, 'up')"
                        ${idx === 0 ? 'disabled style="opacity:0.3;cursor:not-allowed"' : ''}>
                    <i class="fa-solid fa-chevron-up text-[10px]"></i>
                </button>
                <button class="btn-icon" title="아래로"
                        onclick="moveCourse(event, ${c.courseSn}, 'down')"
                        ${idx === courses.length - 1 ? 'disabled style="opacity:0.3;cursor:not-allowed"' : ''}>
                    <i class="fa-solid fa-chevron-down text-[10px]"></i>
                </button>
                <button class="btn-icon btn-icon-danger" title="제거"
                        onclick="removeCourse(event, ${c.courseSn})">
                    <i class="fa-solid fa-xmark text-[10px]"></i>
                </button>
            </div>
        </li>
    `).join('');
}

function showAddCourseForm() {
    const select = document.getElementById('available-course-select');
    select.innerHTML = '<option value="">불러오는 중…</option>';

    fetch('/instructor/curriculum/available-courses')
        .then(res => res.json())
        .then(courses => {
            if (courses.length === 0) {
                select.innerHTML = '<option value="">추가 가능한 강좌가 없습니다.</option>';
            } else {
                select.innerHTML = '<option value="">강좌를 선택하세요</option>' +
                    courses.map(c => `<option value="${c.courseSn}">${escHtml(c.courseNm)}</option>`).join('');
            }
        })
        .catch(() => { select.innerHTML = '<option value="">불러오기 실패</option>'; });

    document.getElementById('add-course-form').classList.remove('hidden');
}

function hideAddCourseForm() {
    document.getElementById('add-course-form').classList.add('hidden');
}

function submitAddCourseForm() {
    const courseSn = document.getElementById('available-course-select').value;
    if (!courseSn) { alert('추가할 강좌를 선택하세요.'); return; }

    fetch(`/instructor/curriculum/${selectedCurriculumId}/courses`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ courseSn: Number(courseSn) }),
    }).then(res => {
        if (!res.ok) return res.text().then(t => { throw new Error(t); });
        hideAddCourseForm();
        loadCourses(selectedCurriculumId);
    }).catch(e => alert(e.message || '추가에 실패했습니다.'));
}

function removeCourse(event, courseSn) {
    event.stopPropagation();
    if (!confirm('이 강좌를 커리큘럼에서 제거하시겠습니까?\n강좌 자체는 삭제되지 않습니다.')) return;

    fetch(`/instructor/curriculum/${selectedCurriculumId}/courses/${courseSn}`, { method: 'DELETE' })
        .then(res => {
            if (!res.ok) return res.text().then(t => { throw new Error(t); });
            loadCourses(selectedCurriculumId);
        }).catch(e => alert(e.message || '제거에 실패했습니다.'));
}

function moveCourse(event, courseSn, direction) {
    event.stopPropagation();
    const endpoint = direction === 'up' ? 'move-up' : 'move-down';

    fetch(`/instructor/curriculum/${selectedCurriculumId}/courses/${courseSn}/${endpoint}`, {
        method: 'PUT',
    }).then(res => {
        if (!res.ok) return res.text().then(t => { throw new Error(t); });
        loadCourses(selectedCurriculumId);
    }).catch(e => alert(e.message || '순서 변경에 실패했습니다.'));
}

// ─── Util ────────────────────────────────────────────────────

function escHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;').replace(/'/g, '&#39;');
}
