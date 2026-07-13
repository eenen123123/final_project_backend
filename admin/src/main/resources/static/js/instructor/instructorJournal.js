document.addEventListener('DOMContentLoaded', function () {
    const TITLE_MAX = 200;

    /* ── 검색 필터 토글 (펼침 상태 기억) ── */
    const btnToggleFilter = document.getElementById('btn-toggle-filter');
    const filterArea = document.getElementById('filter-area');
    const FILTER_STATE_KEY = 'journalFilterOpen';
    if (btnToggleFilter && filterArea) {
        const savedOpen = localStorage.getItem(FILTER_STATE_KEY);
        if (savedOpen === 'true') filterArea.classList.remove('hidden');
        else if (savedOpen === 'false') filterArea.classList.add('hidden');

        btnToggleFilter.addEventListener('click', function () {
            filterArea.classList.toggle('hidden');
            localStorage.setItem(FILTER_STATE_KEY, !filterArea.classList.contains('hidden'));
        });
    }

    /* ── 인라인 에러 헬퍼 ── */
    function setError(inputEl, errorEl, msg) {
        if (inputEl) inputEl.classList.add('is-error');
        errorEl.textContent = msg;
        errorEl.classList.add('visible');
    }
    function clearError(inputEl, errorEl) {
        if (inputEl) inputEl.classList.remove('is-error');
        errorEl.classList.remove('visible');
    }
    function updateCount(countEl, hintEl, len) {
        countEl.textContent = len;
        hintEl.classList.toggle('warn', len >= TITLE_MAX * 0.9 && len < TITLE_MAX);
        hintEl.classList.toggle('over', len >= TITLE_MAX);
    }

    /* ── TipTap 에디터 초기화 ── */
    function initEditors() {
        if (typeof TipTapEditor === 'undefined') return;

        if (document.getElementById('journal-viewer')) {
            const initInput = document.getElementById('journal-cont-init');
            const content = initInput ? initInput.value : '';
            let initial = '';
            let htmlToRestore = null;
            if (content) {
                try { JSON.parse(content); initial = content; }
                catch { htmlToRestore = content; }
            }
            TipTapEditor.mount('journal-viewer', {
                editable: false,
                initialContent: initial,
                imageUrlResolver: (fileId) => `/admin/files/${fileId}/view`,
            });
            if (htmlToRestore) {
                const t = setInterval(() => {
                    const el = document.querySelector('#journal-viewer .tiptap');
                    if (el && el.editor) { el.editor.commands.setContent(htmlToRestore); clearInterval(t); }
                }, 50);
                setTimeout(() => clearInterval(t), 3000);
            }
        }

        if (document.getElementById('editor-create')) {
            TipTapEditor.mount('editor-create', {
                editable: true,
                outputInputId: 'create-cont',
                imageUrlResolver: (fileId) => `/admin/files/${fileId}/view`,
            });
        }

        if (document.getElementById('editor-edit')) {
            const initInput = document.getElementById('edit-cont-init');
            TipTapEditor.mount('editor-edit', {
                editable: true,
                outputInputId: 'edit-cont',
                initialContent: initInput ? initInput.value : '',
                imageUrlResolver: (fileId) => `/admin/files/${fileId}/view`,
            });
        }
    }

    /* ── 폼 바인딩 ── */
    function bindForm(formId, titleId, countId, dtId, contHiddenId, contErrId) {
        const form = document.getElementById(formId);
        if (!form) return;

        const titleEl  = document.getElementById(titleId);
        const countEl  = document.getElementById(countId);
        const hintEl   = countEl ? countEl.closest('.form-hint') : null;
        const dtEl     = document.getElementById(dtId);
        const titleErr = document.getElementById(titleId + '-error');
        const dtErr    = document.getElementById(dtId    + '-error');
        const contErr  = document.getElementById(contErrId);

        if (titleEl && countEl) {
            updateCount(countEl, hintEl, titleEl.value.length);
            titleEl.addEventListener('input', () => {
                updateCount(countEl, hintEl, titleEl.value.length);
                if (titleEl.value.trim()) clearError(titleEl, titleErr);
            });
        }

        if (dtEl) dtEl.addEventListener('change', () => { if (dtEl.value) clearError(dtEl, dtErr); });

        form.addEventListener('submit', function (e) {
            let valid = true;

            if (!titleEl.value.trim()) {
                setError(titleEl, titleErr, '제목을 입력해주세요.');
                valid = false;
            } else if (titleEl.value.length > TITLE_MAX) {
                setError(titleEl, titleErr, `제목은 ${TITLE_MAX}자 이내로 입력해주세요.`);
                valid = false;
            } else {
                clearError(titleEl, titleErr);
            }

            if (!dtEl.value) {
                setError(dtEl, dtErr, '날짜를 선택해주세요.');
                valid = false;
            } else {
                clearError(dtEl, dtErr);
            }

            const contHidden = document.getElementById(contHiddenId);
            const contText   = contHidden ? contHidden.value.replace(/<[^>]*>/g, '').trim() : '';
            if (!contText) {
                if (contErr) contErr.classList.add('visible');
                valid = false;
            } else {
                if (contErr) contErr.classList.remove('visible');
            }

            if (!valid) {
                e.preventDefault();
                const firstError = form.querySelector('.is-error');
                if (firstError) {
                    firstError.focus();
                } else {
                    const editorEl = form.querySelector('[id^="editor-"]');
                    if (editorEl) editorEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                }
            }
        });
    }

    initEditors();

    bindForm('form-create', 'create-title', 'create-title-count', 'create-dt', 'create-cont', 'create-cont-error');
    bindForm('form-edit',   'edit-title',   'edit-title-count',   'edit-dt',   'edit-cont',   'edit-cont-error');

    /* ── 삭제 확인 모달 ── */
    window.openDeleteModal = function () {
        const m = document.getElementById('modal-delete-journal');
        m.classList.remove('hidden');
        m.classList.add('flex');
    };
    window.closeDeleteModal = function () {
        const m = document.getElementById('modal-delete-journal');
        m.classList.add('hidden');
        m.classList.remove('flex');
    };
    window.confirmDelete = function () {
        document.getElementById('form-delete').submit();
    };
});
