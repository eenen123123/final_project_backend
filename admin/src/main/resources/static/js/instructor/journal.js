(function () {
    const TITLE_MAX = 200;

    var editorCreate = null;
    var editorEdit   = null;

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

    /* ── Toast UI 에디터 초기화 ── */
    function initEditors() {
        if (typeof toastui === 'undefined') return;

        const { Editor } = toastui;
        const p = Editor.plugin || {};
        const plugins = [p.colorSyntax].filter(Boolean);

        const baseOptions = {
            height: '300px',
            initialEditType: 'wysiwyg',
            previewStyle: 'tab',
            language: 'ko-KR',
            usageStatistics: false,
            plugins,
        };

        const createEl = document.getElementById('editor-create');
        if (createEl) {
            editorCreate = new Editor({ el: createEl, ...baseOptions });
        }

        const editEl = document.getElementById('editor-edit');
        if (editEl) {
            editorEdit = new Editor({ el: editEl, ...baseOptions });
            const initInput = document.getElementById('edit-cont-init');
            if (initInput && initInput.value) editorEdit.setHTML(initInput.value);
        }
    }

    /* 에디터 HTML에서 태그를 제거한 순수 텍스트 (비어있는지 확인용) */
    function getEditorText(editor) {
        if (!editor) return '';
        return editor.getHTML().replace(/<[^>]*>/g, '').trim();
    }

    /* ── 폼 바인딩 ── */
    function bindForm(formId, titleId, countId, dtId, contErrId, getEditor) {
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

            const editor = getEditor();
            if (!getEditorText(editor)) {
                if (contErr) contErr.classList.add('visible');
                valid = false;
            } else {
                if (contErr) contErr.classList.remove('visible');
                /* 제출 직전 hidden input에 HTML 주입 */
                const contHidden = form.querySelector('input[name="jrnlCont"]');
                if (editor && contHidden) contHidden.value = editor.getHTML();
            }

            if (!valid) {
                e.preventDefault();
                const firstError = form.querySelector('.is-error');
                if (firstError) firstError.focus();
            }
        });
    }

    initEditors();

    bindForm('form-create', 'create-title', 'create-title-count', 'create-dt', 'create-cont-error', () => editorCreate);
    bindForm('form-edit',   'edit-title',   'edit-title-count',   'edit-dt',   'edit-cont-error',   () => editorEdit);

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
})();
