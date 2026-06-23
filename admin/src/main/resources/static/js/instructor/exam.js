/* ==============================================================
   instructor/exam.js
   문항 관리 / 시험 관리 / AI 문항 생성 탭 제어
   ============================================================== */

// ──────────────────────────────────────────────
// 탭 전환
// ──────────────────────────────────────────────

/**
 * @param {string} tabId   - 'tab-q' | 'tab-e' | 'tab-ai'
 * @param {HTMLElement} btn - 클릭된 탭 버튼
 */
function switchTab(tabId, btn) {
  document.querySelectorAll('[id^="tab-"]').forEach(function(el) {
    el.classList.add('hidden');
  });
  document.querySelectorAll('.exam-tab').forEach(function(b) {
    b.classList.remove('text-sky-600', 'border-sky-500',
                       'text-emerald-600', 'border-emerald-500',
                       'text-violet-600', 'border-violet-500');
    b.classList.add('text-slate-500', 'border-transparent');
  });

  document.getElementById(tabId).classList.remove('hidden');

  btn.classList.remove('text-slate-500', 'border-transparent');
  if (tabId === 'tab-q')  btn.classList.add('text-sky-600',     'border-sky-500');
  if (tabId === 'tab-e')  btn.classList.add('text-emerald-600', 'border-emerald-500');
  if (tabId === 'tab-ai') btn.classList.add('text-violet-600',  'border-violet-500');
}

/** data-tab 속성으로 탭을 프로그래매틱하게 활성화합니다. */
function activateTab(tabId) {
  var btn = document.querySelector('[data-tab="' + tabId + '"]');
  if (btn) switchTab(tabId, btn);
}

// ──────────────────────────────────────────────
// 문항 추가 패널
// ──────────────────────────────────────────────

function openQAddPanel() {
  activateTab('tab-q');
  document.getElementById('e-add-panel').classList.add('hidden');
  document.getElementById('q-add-panel').classList.remove('hidden');
  document.getElementById('q-add-panel').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function closeQAddPanel() {
  document.getElementById('q-add-panel').classList.add('hidden');
  document.getElementById('q-add-form').reset();
  // 선지를 초기 2개로 복원
  var cont = document.getElementById('qa-choices');
  cont.innerHTML = '';
  ['A', 'B'].forEach(function(label) {
    cont.appendChild(qaMakeChoiceRow(label + '. '));
  });
  document.getElementById('qa-choices-wrap').classList.remove('hidden');
}

// ──────────────────────────────────────────────
// 시험 등록 패널
// ──────────────────────────────────────────────

function openEAddPanel() {
  activateTab('tab-e');
  document.getElementById('q-add-panel').classList.add('hidden');
  document.getElementById('e-add-panel').classList.remove('hidden');
  document.getElementById('e-add-panel').scrollIntoView({ behavior: 'smooth', block: 'start' });
}

function closeEAddPanel() {
  document.getElementById('e-add-panel').classList.add('hidden');
  document.getElementById('e-add-form').reset();
  document.getElementById('ea-qstn-input').value = '';
  document.getElementById('ea-qstn-hidden').innerHTML = '';
}

// ──────────────────────────────────────────────
// 문항 추가 패널 — 과목 로드 (대분류 → 과목)
// ──────────────────────────────────────────────

function qaLoadSubjects() {
  var clId = document.getElementById('qa-cl').value;
  var subjSelect = document.getElementById('qa-subj');
  if (!clId) {
    subjSelect.innerHTML = '<option value="">대분류 선택</option>';
    return;
  }
  subjSelect.innerHTML = '<option value="">로딩 중...</option>';
  fetch('/instructor/exams/ai/subjects?subjClId=' + encodeURIComponent(clId))
    .then(function(r) { return r.json(); })
    .then(function(list) {
      subjSelect.innerHTML = '<option value="">선택</option>';
      list.forEach(function(s) {
        var opt = document.createElement('option');
        opt.value = s.subjId;
        opt.textContent = s.subjNm;
        subjSelect.appendChild(opt);
      });
    })
    .catch(function() {
      subjSelect.innerHTML = '<option value="">로드 실패</option>';
    });
}

// ──────────────────────────────────────────────
// 문항 추가 패널 — 유형 변경 시 선지 영역 토글
// ──────────────────────────────────────────────

function onQaTypeChange() {
  var type = document.getElementById('qa-type').value;
  document.getElementById('qa-choices-wrap').classList.toggle('hidden', type !== 'MULTIPLE_CHOICE');
}

// ──────────────────────────────────────────────
// 문항 추가 패널 — 선지 관리
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
// 시험 등록 패널 — 문항 SN → hidden input 변환
// (form onsubmit 시 호출됨)
// ──────────────────────────────────────────────

function buildEaQstnInputs() {
  var container = document.getElementById('ea-qstn-hidden');
  container.innerHTML = '';
  var raw = document.getElementById('ea-qstn-input').value;
  raw.split(',').forEach(function(s) {
    var sn = s.trim();
    if (!sn) return;
    var input = document.createElement('input');
    input.type  = 'hidden';
    input.name  = 'qstnSnList';
    input.value = sn;
    container.appendChild(input);
  });
}

// ──────────────────────────────────────────────
// 문항 상세 모달
// ──────────────────────────────────────────────

function openQDetail(sn) {
  var q = (window._questions || []).find(function(x) { return x.qstnSn == sn; });
  if (!q) return;

  var typeName = typeof q.qstnTypeCd === 'object'
    ? (q.qstnTypeCd.name || '') : (q.qstnTypeCd || '');

  var typeLabels = { MULTIPLE_CHOICE: '객관식', SHORT_ANSWER: '단답형', ESSAY: '서술형' };
  var typeBgMap  = {
    MULTIPLE_CHOICE: 'bg-blue-50 text-blue-700',
    SHORT_ANSWER:    'bg-emerald-50 text-emerald-700',
    ESSAY:           'bg-amber-50 text-amber-700'
  };
  document.getElementById('qd-type').innerHTML =
    '<span class="inline-block text-xs font-semibold rounded px-2 py-0.5 '
    + (typeBgMap[typeName] || '') + '">'
    + esc(typeLabels[typeName] || typeName) + '</span>'
    + (q.aiGenYn === 'Y'
      ? ' <span class="inline-block text-xs font-bold px-1.5 py-0.5 rounded-full ml-1"'
        + ' style="background:linear-gradient(135deg,#ede9fe,#e0f2fe);color:#6d28d9;">AI</span>'
      : '');

  var diffName = typeof q.diffCd === 'object' ? (q.diffCd.name || '') : (q.diffCd || '');
  var diffLabels = { EASY: '쉬움', MEDIUM: '보통', HARD: '어려움' };
  document.getElementById('qd-diff').textContent   = diffLabels[diffName] || diffName || '-';
  document.getElementById('qd-score').textContent  = q.allocScr != null ? q.allocScr + '점' : '-';
  document.getElementById('qd-stem').textContent   = q.stem || '';
  document.getElementById('qd-answer').textContent = q.corrAnswCn || '-';
  document.getElementById('qd-date').textContent   = q.regDt || '';

  // 선지
  var choicesWrap = document.getElementById('qd-choices-wrap');
  if (typeName === 'MULTIPLE_CHOICE' && q.choices && q.choices.length > 0) {
    var ul = document.getElementById('qd-choices');
    ul.innerHTML = '';
    q.choices.forEach(function(c) {
      var li = document.createElement('li');
      li.className = 'text-slate-600';
      li.textContent = c;
      ul.appendChild(li);
    });
    choicesWrap.classList.remove('hidden');
  } else {
    choicesWrap.classList.add('hidden');
  }

  // 해설
  var explWrap = document.getElementById('qd-expl-wrap');
  if (q.explnCn) {
    document.getElementById('qd-expl').textContent = q.explnCn;
    explWrap.classList.remove('hidden');
  } else {
    explWrap.classList.add('hidden');
  }

  document.getElementById('qd-delete-form').action = '/instructor/exams/questions/' + sn + '/delete';
  window._currentQstnSn = sn;
  openModal('modal-q-detail', true);
}

// ──────────────────────────────────────────────
// 문항 수정 모달
// ──────────────────────────────────────────────

function onQeTypeChange() {
  var type = document.getElementById('qe-type').value;
  document.getElementById('qe-choices-wrap').classList.toggle('hidden', type !== 'MULTIPLE_CHOICE');
}

function qeMakeChoiceRow(text) {
  var row = document.createElement('div');
  row.className = 'qe-choice-row flex gap-2';
  row.innerHTML =
    '<input type="text" name="choices" value="' + esc(text || '') + '"'
    + ' class="hm-input hm-input-sky flex-1" />'
    + '<button type="button" onclick="qeRemoveChoice(this)" class="hm-btn-danger px-3">×</button>';
  return row;
}

function qeAddChoice() {
  var rows = document.querySelectorAll('#qe-choices .qe-choice-row');
  var labels = ['A', 'B', 'C', 'D', 'E', 'F'];
  var label = labels[rows.length] || String(rows.length + 1);
  document.getElementById('qe-choices').appendChild(qeMakeChoiceRow(label + '. '));
}

function qeRemoveChoice(btn) {
  var rows = document.querySelectorAll('#qe-choices .qe-choice-row');
  if (rows.length <= 1) { alert('선지는 최소 1개 이상이어야 합니다.'); return; }
  btn.closest('.qe-choice-row').remove();
}

function openQEdit() {
  var sn = window._currentQstnSn;
  var q  = (window._questions || []).find(function(x) { return x.qstnSn == sn; });
  if (!q) return;

  var typeName = typeof q.qstnTypeCd === 'object'
    ? (q.qstnTypeCd.name || '') : (q.qstnTypeCd || '');

  document.getElementById('qe-form').action    = '/instructor/exams/questions/' + sn + '/edit';
  document.getElementById('qe-type').value     = typeName;
  document.getElementById('qe-stem').value     = q.stem || '';
  document.getElementById('qe-answer').value   = q.corrAnswCn || '';
  document.getElementById('qe-score').value    = q.allocScr != null ? q.allocScr : '';
  document.getElementById('qe-expl').value     = q.explnCn || '';

  // 선지 채우기 (객관식만)
  var choicesWrap = document.getElementById('qe-choices-wrap');
  var choicesCont = document.getElementById('qe-choices');
  choicesCont.innerHTML = '';
  if (typeName === 'MULTIPLE_CHOICE') {
    var choices = (q.choices && q.choices.length > 0) ? q.choices : ['A. ', 'B. '];
    choices.forEach(function(c) { choicesCont.appendChild(qeMakeChoiceRow(c)); });
    choicesWrap.classList.remove('hidden');
  } else {
    choicesWrap.classList.add('hidden');
  }

  closeModal('modal-q-detail', true);
  openModal('modal-q-edit', true);
}

// ──────────────────────────────────────────────
// 시험 상세 모달
// ──────────────────────────────────────────────

function openEDetail(sn) {
  var e = (window._exams || []).find(function(x) { return x.examSn == sn; });
  if (!e) return;

  document.getElementById('ed-name').textContent  = e.examRegNm || '';
  document.getElementById('ed-start').textContent = e.examStrtDt || '-';
  document.getElementById('ed-end').textContent   = e.examEndDt  || '-';
  document.getElementById('ed-cnt').textContent   = (e.qstnCnt != null ? e.qstnCnt : 0) + '개';
  document.getElementById('ed-date').textContent  = e.examRegDt  || '';
  document.getElementById('ed-delete-form').action = '/instructor/exams/' + sn + '/delete';

  window._currentExamSn = sn;
  openModal('modal-e-detail', true);
}

// ──────────────────────────────────────────────
// 시험 수정 모달
// ──────────────────────────────────────────────

function openEEdit() {
  var sn = window._currentExamSn;
  var e  = (window._exams || []).find(function(x) { return x.examSn == sn; });
  if (!e) return;

  function toDatetimeLocal(val) {
    if (!val) return '';
    return val.trim().replace(' ', 'T').substring(0, 16);
  }

  document.getElementById('ee-form').action  = '/instructor/exams/' + sn + '/edit';
  document.getElementById('ee-name').value   = e.examRegNm || '';
  document.getElementById('ee-start').value  = toDatetimeLocal(e.examStrtDt);
  document.getElementById('ee-end').value    = toDatetimeLocal(e.examEndDt);

  closeModal('modal-e-detail', true);
  openModal('modal-e-edit', true);
}

// ──────────────────────────────────────────────
// AI 탭 — 과목 로드
// ──────────────────────────────────────────────

function aiLoadSubjects() {
  var clId = document.getElementById('ai-cl').value;
  var subjSelect = document.getElementById('ai-subj');
  if (!clId) {
    subjSelect.innerHTML = '<option value="">대분류 먼저 선택</option>';
    return;
  }
  subjSelect.innerHTML = '<option value="">로딩 중...</option>';
  fetch('/instructor/exams/ai/subjects?subjClId=' + encodeURIComponent(clId))
    .then(function(r) { return r.json(); })
    .then(function(list) {
      subjSelect.innerHTML = '<option value="">선택</option>';
      list.forEach(function(s) {
        var opt = document.createElement('option');
        opt.value = s.subjId;
        opt.textContent = s.subjNm;
        subjSelect.appendChild(opt);
      });
    })
    .catch(function() {
      subjSelect.innerHTML = '<option value="">로드 실패</option>';
    });
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

  fetch('/instructor/exams/ai/generate', {
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

  var diffColors = { EASY: 'bg-emerald-50 text-emerald-700', MEDIUM: 'bg-amber-50 text-amber-700', HARD: 'bg-red-50 text-red-700' };
  var diffLabels = { EASY: '쉬움', MEDIUM: '보통', HARD: '어려움' };
  var badge = document.getElementById('ai-diff-badge');
  badge.className   = 'text-xs font-semibold px-2 py-0.5 rounded ' + (diffColors[diffCd] || '');
  badge.textContent = diffLabels[diffCd] || diffCd;

  var choicesCont = document.getElementById('ai-choices');
  choicesCont.innerHTML = '';
  (q.choices || []).forEach(function(c) { choicesCont.appendChild(aiMakeChoiceRow(c)); });

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

  var stem   = document.getElementById('ai-stem').value.trim();
  var answer = document.getElementById('ai-answer').value.trim();
  var score  = document.getElementById('ai-score').value;
  var expl   = document.getElementById('ai-expl').value.trim();
  var diffCd = document.getElementById('ai-diff').value;
  var choices = Array.from(document.querySelectorAll('#ai-choices .ai-choice-input'))
                     .map(function(el) { return el.value.trim(); })
                     .filter(Boolean);

  if (!stem) { alert('문제 본문을 입력해주세요.'); return; }

  fetch('/instructor/exams/ai/save', {
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
// ESC 키 — 열린 모달 닫기
// ──────────────────────────────────────────────

document.addEventListener('keydown', function(e) {
  if (e.key !== 'Escape') return;
  var modals = ['modal-q-edit', 'modal-q-detail', 'modal-e-edit', 'modal-e-detail'];
  for (var i = 0; i < modals.length; i++) {
    var el = document.getElementById(modals[i]);
    if (el && !el.classList.contains('hidden')) {
      closeModal(modals[i], true);
      return;
    }
  }
});

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
