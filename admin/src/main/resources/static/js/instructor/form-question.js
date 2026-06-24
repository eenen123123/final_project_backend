/* ==============================================================
   instructor/form-question.js
   form-question.html 전용 스크립트 (등록 / 수정 / AI 채우기)
   ============================================================== */

// ──────────────────────────────────────────────
// 페이지 초기화
// ──────────────────────────────────────────────

(function init() {
  var q  = window._question;
  var ss = window._subjectState;

  // 선지 초기화
  var choices = (q && q.choices && q.choices.length > 0)
    ? q.choices : ['A. ', 'B. '];
  var container = document.getElementById('fq-choices');
  if (container) {
    container.innerHTML = '';
    choices.forEach(function(c) { container.appendChild(fqMakeChoiceRow(c)); });
  }

  // 수정 모드: 대분류 → 과목 복원
  if (ss && ss.subjClId) {
    var clSel = document.getElementById('fq-cl');
    if (clSel) {
      clSel.value = ss.subjClId;
      _loadSubjectsTo(ss.subjClId, 'fq-subj', ss.subjId);
    }
  }
})();

// ──────────────────────────────────────────────
// 폼 — 유형 토글
// ──────────────────────────────────────────────

function fqOnTypeChange() {
  var type = document.getElementById('fq-type').value;
  document.getElementById('fq-choices-wrap').classList.toggle('hidden', type !== 'MULTIPLE_CHOICE');
}

// ──────────────────────────────────────────────
// 폼 — 과목 로드
// ──────────────────────────────────────────────

function fqLoadSubjects() {
  _loadSubjectsTo(document.getElementById('fq-cl').value, 'fq-subj');
}

// ──────────────────────────────────────────────
// 폼 — 선지 관리
// ──────────────────────────────────────────────

function fqMakeChoiceRow(text) {
  var row = document.createElement('div');
  row.className = 'fq-choice-row flex gap-2';
  row.innerHTML =
    '<input type="text" name="choices" value="' + esc(text || '') + '"'
    + ' class="hm-input hm-input-sky flex-1" />'
    + '<button type="button" onclick="fqRemoveChoice(this)" class="hm-btn-danger px-3">×</button>';
  return row;
}

function fqAddChoice() {
  var rows   = document.querySelectorAll('#fq-choices .fq-choice-row');
  var labels = ['A', 'B', 'C', 'D', 'E', 'F'];
  var label  = labels[rows.length] || String(rows.length + 1);
  document.getElementById('fq-choices').appendChild(fqMakeChoiceRow(label + '. '));
}

function fqRemoveChoice(btn) {
  var rows = document.querySelectorAll('#fq-choices .fq-choice-row');
  if (rows.length <= 1) { alert('선지는 최소 1개 이상이어야 합니다.'); return; }
  btn.closest('.fq-choice-row').remove();
}

// ──────────────────────────────────────────────
// AI — 원버튼 폼 채우기
// ──────────────────────────────────────────────

function fqAiFill() {
  var subjEl = document.getElementById('fq-subj');
  var subjId = subjEl ? subjEl.value : '';
  if (!subjId) { alert('과목을 선택해주세요.'); return; }
  var subjNm = subjEl.options[subjEl.selectedIndex].text;
  var diffCd = document.getElementById('fq-diff').value || 'MEDIUM';

  var btn = document.getElementById('ai-fill-btn');
  btn.disabled = true;
  document.getElementById('ai-spinner').classList.remove('hidden');

  var extraPromptEl = document.getElementById('ai-extra-prompt');
  var extraPrompt = extraPromptEl ? extraPromptEl.value.trim() : '';

  fetch('/instructor/questions/ai/generate', {
    method:  'POST',
    headers: { 'Content-Type': 'application/json' },
    body:    JSON.stringify({ subjId: Number(subjId), subjNm: subjNm, difficulty: diffCd, extraPrompt: extraPrompt || null })
  })
    .then(function(r) {
      if (!r.ok) return r.json().then(function(e) { throw new Error(e.message || r.status); });
      return r.json();
    })
    .then(function(q) { fqFillForm(q); })
    .catch(function(err) { alert('AI 생성 실패: ' + err.message); })
    .finally(function() {
      btn.disabled = false;
      document.getElementById('ai-spinner').classList.add('hidden');
    });
}

function fqFillForm(q) {
  // 유형 — AI는 항상 객관식
  document.getElementById('fq-type').value = 'MULTIPLE_CHOICE';
  fqOnTypeChange();
  if (window.initCustomSelect) window.initCustomSelect(document.getElementById('fq-type'));

  // 본문
  document.getElementById('fq-stem').value = q.stem || '';

  // 선지
  var container = document.getElementById('fq-choices');
  container.innerHTML = '';
  (q.choices || []).forEach(function(c) { container.appendChild(fqMakeChoiceRow(c)); });

  // 정답 / 배점 / 해설
  document.getElementById('fq-answer').value = q.corrAnswCn || '';
  document.getElementById('fq-score').value  = q.allocScr != null ? q.allocScr : 1;
  document.getElementById('fq-expl').value   = q.explnCn || '';

  // AI 생성 여부 hidden 필드
  var aiYnInput = document.getElementById('fq-ai-gen-yn');
  if (!aiYnInput) {
    aiYnInput = document.createElement('input');
    aiYnInput.type = 'hidden';
    aiYnInput.name = 'aiGenYn';
    aiYnInput.id   = 'fq-ai-gen-yn';
    document.getElementById('q-form').appendChild(aiYnInput);
  }
  aiYnInput.value = 'Y';
}

// ──────────────────────────────────────────────
// 공용 — 과목 AJAX 로드
// ──────────────────────────────────────────────

function _loadSubjectsTo(clId, targetId, selectedId) {
  var sel = document.getElementById(targetId);
  if (!sel) return;
  if (!clId) {
    sel.innerHTML = '<option value="">대분류 선택</option>';
    return;
  }
  sel.innerHTML = '<option value="">로딩 중...</option>';
  fetch('/instructor/questions/ai/subjects?subjClId=' + encodeURIComponent(clId))
    .then(function(r) {
      if (!r.ok) throw new Error('HTTP ' + r.status);
      return r.json();
    })
    .then(function(list) {
      if (list.length === 0) {
        sel.innerHTML = '<option value="">과목 없음</option>';
      } else {
        sel.innerHTML = '<option value="">선택</option>';
        list.forEach(function(s) {
          var opt = document.createElement('option');
          opt.value = s.subjId;
          opt.textContent = s.subjNm;
          if (selectedId && s.subjId == selectedId) opt.selected = true;
          sel.appendChild(opt);
        });
      }
      if (window.initCustomSelect) window.initCustomSelect(sel);
    })
    .catch(function(err) {
      console.error('[과목 로드 실패] targetId=' + targetId + ', clId=' + clId, err);
      sel.innerHTML = '<option value="">로드 실패</option>';
      if (window.initCustomSelect) window.initCustomSelect(sel);
    });
}

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
