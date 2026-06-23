/* ==============================================================
   instructor/questions.js
   list-questions.html + detail-question.html 공용 스크립트
   ============================================================== */

// ──────────────────────────────────────────────
// 탭 전환 (list-questions.html)
// ──────────────────────────────────────────────

function switchTab(tabId, btn) {
  document.querySelectorAll('[id^="tab-"]').forEach(function(el) {
    el.classList.add('hidden');
  });
  document.querySelectorAll('.q-tab').forEach(function(b) {
    b.classList.remove('text-sky-600', 'border-sky-500',
                       'text-violet-600', 'border-violet-500');
    b.classList.add('text-slate-500', 'border-transparent');
  });

  document.getElementById(tabId).classList.remove('hidden');
  btn.classList.remove('text-slate-500', 'border-transparent');
  if (tabId === 'tab-list') btn.classList.add('text-sky-600',    'border-sky-500');
  if (tabId === 'tab-ai')   btn.classList.add('text-violet-600', 'border-violet-500');
}

// ──────────────────────────────────────────────
// 문항 추가 패널 (list-questions.html)
// ──────────────────────────────────────────────

function openQAddPanel() {
  var panel = document.getElementById('q-add-panel');
  if (!panel) return;
  panel.classList.remove('hidden');
  panel.scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function closeQAddPanel() {
  document.getElementById('q-add-panel').classList.add('hidden');
  document.getElementById('q-add-form').reset();
  var cont = document.getElementById('qa-choices');
  cont.innerHTML = '';
  ['A', 'B'].forEach(function(label) {
    cont.appendChild(qaMakeChoiceRow(label + '. '));
  });
  document.getElementById('qa-choices-wrap').classList.remove('hidden');
}

// ──────────────────────────────────────────────
// 추가 패널 — 유형 토글
// ──────────────────────────────────────────────

function onQaTypeChange() {
  var type = document.getElementById('qa-type').value;
  document.getElementById('qa-choices-wrap').classList.toggle('hidden', type !== 'MULTIPLE_CHOICE');
}

// ──────────────────────────────────────────────
// 추가 패널 — 과목 로드
// ──────────────────────────────────────────────

function qaLoadSubjects() {
  _loadSubjects(document.getElementById('qa-cl').value, 'qa-subj');
}

// ──────────────────────────────────────────────
// 추가 패널 — 선지 관리
// ──────────────────────────────────────────────

function qaMakeChoiceRow(placeholder) {
  var row = document.createElement('div');
  row.className = 'qa-choice-row flex gap-2';
  row.innerHTML =
    '<input type="text" name="choices" placeholder="' + esc(placeholder || '') + '"'
    + ' class="hm-input hm-input-sky flex-1" />'
    + '<button type="button" onclick="qaRemoveChoice(this)" class="hm-btn-danger px-3">×</button>';
  return row;
}

function qaAddChoice() {
  var rows = document.querySelectorAll('#qa-choices .qa-choice-row');
  var labels = ['A', 'B', 'C', 'D', 'E', 'F'];
  var label = labels[rows.length] || String(rows.length + 1);
  document.getElementById('qa-choices').appendChild(qaMakeChoiceRow(label + '. '));
}

function qaRemoveChoice(btn) {
  var rows = document.querySelectorAll('#qa-choices .qa-choice-row');
  if (rows.length <= 1) { alert('선지는 최소 1개 이상이어야 합니다.'); return; }
  btn.closest('.qa-choice-row').remove();
}

// ──────────────────────────────────────────────
// 필터 — 대분류 → 과목 로드 (list-questions.html)
// ──────────────────────────────────────────────

function filterLoadSubjects() {
  var clId = document.getElementById('filter-cl').value;
  _loadSubjects(clId, 'filter-subj', window._filterState && window._filterState.subjId);
}

function syncClHidden() {
  var hidden = document.getElementById('filter-cl-hidden');
  if (hidden) hidden.value = document.getElementById('filter-cl').value;
}

// ──────────────────────────────────────────────
// 수정 모드 전환 (detail-question.html)
// ──────────────────────────────────────────────

function showEdit() {
  document.getElementById('view-mode').classList.add('hidden');
  document.getElementById('edit-mode').classList.remove('hidden');
  document.getElementById('view-actions').classList.add('hidden');
  document.getElementById('edit-actions').classList.remove('hidden');
  document.getElementById('edit-actions').classList.add('flex');

  // 선지 초기화 (객관식인 경우)
  var q = window._question || {};
  var typeName = typeof q.qstnTypeCd === 'object'
    ? (q.qstnTypeCd.name || '') : (q.qstnTypeCd || '');
  var container = document.getElementById('edit-choices');
  if (container) {
    container.innerHTML = '';
    if (typeName === 'MULTIPLE_CHOICE') {
      var choices = (q.choices && q.choices.length > 0) ? q.choices : ['A. ', 'B. '];
      choices.forEach(function(c) { container.appendChild(editMakeChoiceRow(c)); });
    }
  }
}

function showView() {
  document.getElementById('view-mode').classList.remove('hidden');
  document.getElementById('edit-mode').classList.add('hidden');
  document.getElementById('view-actions').classList.remove('hidden');
  document.getElementById('edit-actions').classList.add('hidden');
  document.getElementById('edit-actions').classList.remove('flex');
}

function onEditTypeChange() {
  var type = document.getElementById('edit-type').value;
  var wrap = document.getElementById('edit-choices-wrap');
  if (wrap) wrap.classList.toggle('hidden', type !== 'MULTIPLE_CHOICE');
}

function editLoadSubjects() {
  _loadSubjects(document.getElementById('edit-cl').value, 'edit-subj');
}

function editMakeChoiceRow(text) {
  var row = document.createElement('div');
  row.className = 'edit-choice-row flex gap-2';
  row.innerHTML =
    '<input type="text" name="choices" value="' + esc(text || '') + '"'
    + ' class="hm-input hm-input-sky flex-1" />'
    + '<button type="button" onclick="editRemoveChoice(this)" class="hm-btn-danger px-3">×</button>';
  return row;
}

function editAddChoice() {
  var rows = document.querySelectorAll('#edit-choices .edit-choice-row');
  var labels = ['A', 'B', 'C', 'D', 'E', 'F'];
  var label = labels[rows.length] || String(rows.length + 1);
  document.getElementById('edit-choices').appendChild(editMakeChoiceRow(label + '. '));
}

function editRemoveChoice(btn) {
  var rows = document.querySelectorAll('#edit-choices .edit-choice-row');
  if (rows.length <= 1) { alert('선지는 최소 1개 이상이어야 합니다.'); return; }
  btn.closest('.edit-choice-row').remove();
}

// ──────────────────────────────────────────────
// AI 탭 — 과목 로드 (list-questions.html)
// ──────────────────────────────────────────────

function aiLoadSubjects() {
  _loadSubjects(document.getElementById('ai-cl').value, 'ai-subj');
}

// ──────────────────────────────────────────────
// AI 탭 — 문항 생성
// ──────────────────────────────────────────────

function aiGenerate() {
  var subjEl = document.getElementById('ai-subj');
  var subjId = subjEl.value;
  if (!subjId) { alert('과목을 선택해주세요.'); return; }
  var subjNm = subjEl.options[subjEl.selectedIndex].text;
  var diffCd = document.getElementById('ai-diff').value;

  var btn = document.getElementById('ai-gen-btn');
  btn.disabled = true;
  document.getElementById('ai-spinner').classList.remove('hidden');
  document.getElementById('ai-preview-card').classList.add('hidden');

  fetch('/instructor/questions/ai/generate', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify({ subjId: Number(subjId), subjNm: subjNm, difficulty: diffCd })
  })
    .then(function(r) {
      if (!r.ok) return r.json().then(function(e) { throw new Error(e.message || r.status); });
      return r.json();
    })
    .then(function(q) { aiRenderPreview(q, diffCd); })
    .catch(function(err) { alert('문항 생성 실패: ' + err.message); })
    .finally(function() {
      btn.disabled = false;
      document.getElementById('ai-spinner').classList.add('hidden');
    });
}

function aiRenderPreview(q, diffCd) {
  document.getElementById('ai-stem').value   = q.stem || '';
  document.getElementById('ai-answer').value = q.corrAnswCn || '';
  document.getElementById('ai-score').value  = q.allocScr != null ? q.allocScr : 1;
  document.getElementById('ai-expl').value   = q.explnCn || '';

  var diffColors = {
    EASY:   'bg-emerald-50 text-emerald-700',
    MEDIUM: 'bg-amber-50 text-amber-700',
    HARD:   'bg-red-50 text-red-700'
  };
  var diffLabels = { EASY: '쉬움', MEDIUM: '보통', HARD: '어려움' };
  var badge = document.getElementById('ai-diff-badge');
  badge.className   = 'text-xs font-semibold px-2 py-0.5 rounded ' + (diffColors[diffCd] || '');
  badge.textContent = diffLabels[diffCd] || diffCd;

  var cont = document.getElementById('ai-choices');
  cont.innerHTML = '';
  (q.choices || []).forEach(function(c) { cont.appendChild(aiMakeChoiceRow(c)); });

  window._aiGeneratedQuestion = q;
  document.getElementById('ai-preview-card').classList.remove('hidden');
}

function aiMakeChoiceRow(text) {
  var row = document.createElement('div');
  row.className = 'ai-choice-row flex gap-2';
  row.innerHTML =
    '<input type="text" value="' + esc(text || '') + '"'
    + ' class="hm-input hm-input-sky flex-1 ai-choice-input" />'
    + '<button type="button" onclick="aiRemoveChoice(this)" class="hm-btn-danger px-3">×</button>';
  return row;
}

function aiAddChoice() {
  document.getElementById('ai-choices').appendChild(aiMakeChoiceRow(''));
}

function aiRemoveChoice(btn) {
  var rows = document.querySelectorAll('#ai-choices .ai-choice-row');
  if (rows.length <= 1) { alert('선지는 최소 1개 이상이어야 합니다.'); return; }
  btn.closest('.ai-choice-row').remove();
}

function aiReset() {
  document.getElementById('ai-preview-card').classList.add('hidden');
  window._aiGeneratedQuestion = null;
}

function aiSave() {
  var subjEl = document.getElementById('ai-subj');
  var subjId = subjEl.value;
  if (!subjId) { alert('과목을 선택해주세요.'); return; }

  var stem    = document.getElementById('ai-stem').value.trim();
  var answer  = document.getElementById('ai-answer').value.trim();
  var score   = document.getElementById('ai-score').value;
  var expl    = document.getElementById('ai-expl').value.trim();
  var diffCd  = document.getElementById('ai-diff').value;
  var choices = Array.from(document.querySelectorAll('#ai-choices .ai-choice-input'))
                     .map(function(el) { return el.value.trim(); })
                     .filter(Boolean);

  if (!stem) { alert('문제 본문을 입력해주세요.'); return; }

  fetch('/instructor/questions/ai/save', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({
      subjId:     Number(subjId),
      qstnTypeCd: 'MULTIPLE_CHOICE',
      diffCd:     diffCd,
      stem:       stem,
      choices:    choices,
      corrAnswCn: answer,
      allocScr:   score ? Number(score) : 1,
      explnCn:    expl || null,
      aiGenYn:    'Y'
    })
  })
    .then(function(r) {
      if (!r.ok) return r.json().then(function(e) { throw new Error(e.message || r.status); });
      return r.json();
    })
    .then(function() {
      alert('문항이 저장되었습니다.');
      location.reload();
    })
    .catch(function(err) { alert('저장 실패: ' + err.message); });
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
    .then(function(r) { return r.json(); })
    .then(function(list) {
      sel.innerHTML = '<option value="">전체</option>';
      list.forEach(function(s) {
        var opt = document.createElement('option');
        opt.value = s.subjId;
        opt.textContent = s.subjNm;
        if (selectedId && s.subjId == selectedId) opt.selected = true;
        sel.appendChild(opt);
      });
    })
    .catch(function() {
      sel.innerHTML = '<option value="">로드 실패</option>';
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

// ──────────────────────────────────────────────
// XSS 방어 유틸
// ──────────────────────────────────────────────

function esc(str) {
  if (str == null) return '';
  return String(str)
    .replace(/&/g,  '&amp;')
    .replace(/</g,  '&lt;')
    .replace(/>/g,  '&gt;')
    .replace(/"/g,  '&quot;')
    .replace(/'/g,  '&#39;');
}
