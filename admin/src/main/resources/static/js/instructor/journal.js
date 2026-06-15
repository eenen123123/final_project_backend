(function () {
    const TITLE_MAX = 200;

    function setError(inputEl, errorEl, msg) {
        inputEl.classList.add('is-error');
        errorEl.textContent = msg;
        errorEl.classList.add('visible');
    }
    function clearError(inputEl, errorEl) {
        inputEl.classList.remove('is-error');
        errorEl.classList.remove('visible');
    }
    function updateCount(countEl, hintEl, len) {
        countEl.textContent = len;
        hintEl.classList.toggle('warn', len >= TITLE_MAX * 0.9 && len < TITLE_MAX);
        hintEl.classList.toggle('over', len >= TITLE_MAX);
    }

    function bindForm(formId, titleId, countId, dtId, contId) {
        const form = document.getElementById(formId);
        if (!form) return;

        const titleEl = document.getElementById(titleId);
        const countEl = document.getElementById(countId);
        const hintEl  = countEl ? countEl.closest('.form-hint') : null;
        const dtEl    = document.getElementById(dtId);
        const contEl  = document.getElementById(contId);

        const titleErr = document.getElementById(titleId + '-error');
        const dtErr    = document.getElementById(dtId    + '-error');
        const contErr  = document.getElementById(contId  + '-error');

        if (titleEl && countEl) {
            updateCount(countEl, hintEl, titleEl.value.length);
            titleEl.addEventListener('input', () => {
                updateCount(countEl, hintEl, titleEl.value.length);
                if (titleEl.value.trim()) clearError(titleEl, titleErr);
            });
        }

        if (dtEl)   dtEl.addEventListener('change', () => { if (dtEl.value) clearError(dtEl, dtErr); });
        if (contEl) contEl.addEventListener('input',  () => { if (contEl.value.trim()) clearError(contEl, contErr); });

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

            if (!contEl.value.trim()) {
                setError(contEl, contErr, '내용을 입력해주세요.');
                valid = false;
            } else {
                clearError(contEl, contErr);
            }

            if (!valid) {
                e.preventDefault();
                form.querySelector('.is-error').focus();
            }
        });
    }

    bindForm('form-create', 'create-title', 'create-title-count', 'create-dt', 'create-cont');
    bindForm('form-edit',   'edit-title',   'edit-title-count',   'edit-dt',   'edit-cont');

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
