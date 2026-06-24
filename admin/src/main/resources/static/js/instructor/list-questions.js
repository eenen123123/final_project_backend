/* ==============================================================
   instructor/list-questions.js
   list-questions.html 전용 스크립트 (필터 + 과목 드롭다운 복원)
   ============================================================== */

// ──────────────────────────────────────────────
// 필터 — 대분류 → 과목 로드
// ──────────────────────────────────────────────

function filterLoadSubjects() {
  var clId = document.getElementById('filter-cl').value;
  _loadSubjects(clId, 'filter-subj', window._filterState && window._filterState.subjId);
}

function syncClHidden() {
  var hidden = document.getElementById('filter-cl-hidden');
  if (hidden) hidden.value = document.getElementById('filter-cl').value;
}

/** 빈 값인 select/hidden을 폼 제출에서 제외해 빈 문자열 → Long 변환 오류 방지 */
function filterBeforeSubmit() {
  ['filter-subj', 'filter-cl-hidden'].forEach(function(id) {
    var el = document.getElementById(id);
    if (el && !el.value) el.removeAttribute('name');
  });
}

// ──────────────────────────────────────────────
// 공용 — 과목 목록 AJAX 로드
// ──────────────────────────────────────────────

function _loadSubjects(clId, targetId, selectedId) {
  var sel = document.getElementById(targetId);
  if (!sel) return;
  if (!clId) {
    sel.innerHTML = '<option value="">전체</option>';
    return;
  }
  sel.innerHTML = '<option value="">로딩 중...</option>';
  fetch('/instructor/questions/ai/subjects?subjClId=' + encodeURIComponent(clId))
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(list) {
      sel.innerHTML = '<option value="">전체</option>';
      list.forEach(function(s) {
        var opt = document.createElement('option');
        opt.value = s.subjId;
        opt.textContent = s.subjNm;
        if (selectedId && s.subjId == selectedId) opt.selected = true;
        sel.appendChild(opt);
      });
      if (window.initCustomSelect) window.initCustomSelect(sel);
    })
    .catch(function(err) {
      console.error('[과목 로드 실패]', err);
      sel.innerHTML = '<option value="">로드 실패</option>';
      if (window.initCustomSelect) window.initCustomSelect(sel);
    });
}

// ──────────────────────────────────────────────
// 페이지 로드 — 필터 상태 복원
// ──────────────────────────────────────────────

(function initPage() {
  var state = window._filterState;
  if (state && state.subjClId) {
    var clSel = document.getElementById('filter-cl');
    if (clSel) {
      clSel.value = state.subjClId;
      _loadSubjects(state.subjClId, 'filter-subj', state.subjId);
    }
  }
})();
