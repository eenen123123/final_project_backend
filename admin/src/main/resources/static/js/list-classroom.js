/**
 * list-classroom.js
 * 강사 > 담당 클래스 관리 페이지 (list-classroom.html) 전용 스크립트
 */

var SCREEN_SIZE  = 10; // 페이지당 표시 건수
var BLOCK_SIZE   = 5;  // 페이징 블록(버튼 묶음) 크기
var currentPage  = 1;  // 상태 변경 성공 후 현재 페이지 리로드에 사용

/* ══════════════════════════════════════════════════
   목록 로드 & 렌더링
══════════════════════════════════════════════════ */

function loadClassrooms(page) {
  currentPage = page;
  fetch('/classroom/list/data?page=' + page + '&screenSize=' + SCREEN_SIZE)
    .then(function(r) { return r.json(); })
    .then(function(d) {
      renderTable(d.items);
      document.getElementById('classroom-total').textContent = d.totalCount;
      renderPaging(d.totalCount, d.page, d.screenSize);
    });
}

function renderTable(items) {
  var tbody = document.getElementById('classroom-tbody');
  if (!items || items.length === 0) {
    tbody.innerHTML =
      '<tr><td colspan="5" class="py-16 text-center text-slate-300">' +
      '<div class="flex flex-col items-center gap-3">' +
      '<i class="fa-solid fa-chalkboard text-4xl"></i>' +
      '<p class="text-sm">담당 클래스룸이 없습니다.</p>' +
      '</div></td></tr>';
    return;
  }
  tbody.innerHTML = items.map(function(c) {
    return '<tr class="hover:bg-slate-50/50 transition-colors">' +
      '<td class="py-4 px-6">' +
        '<a href="/classroom/detail/' + c.classSn + '">' +
          '<div class="flex items-center gap-3">' +
            '<div class="w-10 h-10 rounded-xl bg-sky-50 flex items-center justify-center text-sky-500 text-base shrink-0">' +
              '<i class="fa-solid fa-chalkboard"></i>' +
            '</div>' +
            '<div>' +
              '<p class="font-semibold text-slate-900 hover:text-sky-600 transition-colors">' + esc(c.classNm) + '</p>' +
              '<p class="text-xs text-slate-400 mt-0.5">' + esc(c.courseNm) + '</p>' +
            '</div>' +
          '</div>' +
        '</a>' +
      '</td>' +
      '<td class="py-4 px-6">' + statusBadge(c.classStatCd) + '</td>' +
      '<td class="py-4 px-6"><span class="text-sm font-medium text-slate-700">' + c.studentCount + '명</span></td>' +
      '<td class="py-4 px-6 text-xs text-slate-400 font-mono">' + (c.formattedRegDt || '-') + '</td>' +
      /* 상태 변경 버튼: classSn·classNm·현재 상태를 인수로 전달 */
      '<td class="py-4 px-6">' +
        '<button type="button"' +
          ' onclick="openStatusForm(' + c.classSn + ', \'' + esc(c.classNm) + '\', \'' + c.classStatCd + '\')"' +
          ' class="hm-btn-secondary text-xs px-3 py-2"' +
          ' title="상태 변경">' +
          '<i class="fa-solid fa-pen-to-square mr-1"></i>상태 변경' +
        '</button>' +
      '</td>' +
    '</tr>';
  }).join('');
}

/* 상태 코드 → 배지 HTML */
function statusBadge(code) {
  var map = {
    RECRUITING: '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-sky-50 text-sky-600"><i class="fa-solid fa-circle-dot text-[8px]"></i> 모집중</span>',
    ACTIVE:     '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-600"><i class="fa-solid fa-circle-dot text-[8px]"></i> 운영중</span>',
    CLOSED:     '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-500"><i class="fa-solid fa-circle text-[8px]"></i> 종료</span>',
    WAITING:    '<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-amber-50 text-amber-600"><i class="fa-solid fa-circle-dot text-[8px]"></i> 대기</span>'
  };
  return map[code] || '';
}

/* 페이징 컨트롤 렌더링 */
function renderPaging(total, page, screenSize) {
  var totalPage = Math.max(1, Math.ceil(total / screenSize));
  var start = Math.floor((page - 1) / BLOCK_SIZE) * BLOCK_SIZE + 1;
  var end   = Math.min(start + BLOCK_SIZE - 1, totalPage);
  var box   = document.getElementById('classroom-paging');
  var btn = function(label, p, disabled, active) {
    return '<button ' + (disabled ? 'disabled' : 'onclick="loadClassrooms(' + p + ')"') +
      ' class="w-8 h-8 rounded-lg text-xs flex items-center justify-center ' +
      (active   ? 'bg-sky-600 text-white font-bold' : 'border border-slate-200 text-slate-400 hover:bg-slate-50') +
      (disabled ? ' opacity-40 cursor-not-allowed' : '') + '">' + label + '</button>';
  };
  var html = btn('<i class="fa-solid fa-chevron-left"></i>', page - 1, page <= 1, false);
  for (var p = start; p <= end; p++) html += btn(p, p, false, p === page);
  html += btn('<i class="fa-solid fa-chevron-right"></i>', page + 1, page >= totalPage, false);
  box.innerHTML = html;
}

/* XSS 방지용 이스케이프 */
function esc(str) {
  return str ? str.replace(/&/g,'&amp;').replace(/</g,'&lt;').replace(/>/g,'&gt;').replace(/'/g,'&#39;') : '';
}

/* ══════════════════════════════════════════════════
   등록 폼
══════════════════════════════════════════════════ */

function openCreateForm() {
  closeStatusForm(); // 상태 변경 카드와 동시에 열리지 않도록
  document.getElementById('create-form-card').classList.remove('hidden');
  document.getElementById('create-classNm').focus();
}

function closeCreateForm() {
  document.getElementById('create-form-card').classList.add('hidden');
  document.getElementById('create-classNm').value = '';
  document.getElementById('create-courseSn').value = '';
  document.getElementById('create-enrlStrtYmd').value = '';
  document.getElementById('create-enrlEndYmd').value = '';
  document.getElementById('create-error').classList.add('hidden');
}

function showCreateError(msg) {
  var el = document.getElementById('create-error');
  el.textContent = msg;
  el.classList.remove('hidden');
}

function submitCreate() {
  var classNm  = document.getElementById('create-classNm').value.trim();
  var courseSn = document.getElementById('create-courseSn').value;
  var strtYmd  = document.getElementById('create-enrlStrtYmd').value.replace(/-/g, '');
  var endYmd   = document.getElementById('create-enrlEndYmd').value.replace(/-/g, '');

  if (!classNm)  { showCreateError('클래스룸명을 입력하세요.'); return; }
  if (!courseSn) { showCreateError('강좌를 선택하세요.'); return; }
  if (strtYmd && endYmd && endYmd < strtYmd) { showCreateError('종료일은 시작일 이후여야 합니다.'); return; }

  var submitBtn = document.getElementById('create-submit-btn');
  submitBtn.disabled = true;

  var params = new URLSearchParams();
  params.append('classNm', classNm);
  params.append('courseSn', courseSn);
  if (strtYmd) params.append('enrlStrtYmd', strtYmd);
  if (endYmd)  params.append('enrlEndYmd', endYmd);

  fetch('/classroom/create', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  })
  .then(function(r) { return r.json(); })
  .then(function(data) {
    if (data.success) {
      closeCreateForm();
      alert('등록 요청이 결재 상신되었습니다.\n결재 승인 후 클래스룸이 생성됩니다.');
    } else {
      showCreateError(data.message || '요청 중 오류가 발생했습니다.');
      submitBtn.disabled = false;
    }
  })
  .catch(function() {
    showCreateError('서버 오류가 발생했습니다.');
    submitBtn.disabled = false;
  });
}

/* ══════════════════════════════════════════════════
   상태 변경 폼
══════════════════════════════════════════════════ */

/* 상태 변경 대상 클래스룸 정보를 임시 보관 */
var statusTarget = { classSn: null, selectedStatCd: null };

/**
 * 상태 변경 카드를 열고 대상 정보를 초기화한다.
 * @param {number} classSn       변경할 클래스룸 PK
 * @param {string} classNm       클래스룸명 (타이틀 표시용)
 * @param {string} currentStatCd 현재 상태 코드 (초기 선택 표시용)
 */
function openStatusForm(classSn, classNm, currentStatCd) {
  closeCreateForm(); // 등록 카드와 동시에 열리지 않도록

  statusTarget.classSn = classSn;

  // 타이틀에 클래스룸명 표시
  document.getElementById('status-form-title').innerHTML =
    '<i class="fa-solid fa-pen-to-square mr-1.5"></i>' + esc(classNm) + ' — 상태 변경';

  // 에러 초기화, 버튼 활성화
  document.getElementById('status-error').classList.add('hidden');
  document.getElementById('status-submit-btn').disabled = false;

  // 현재 상태를 선택 상태로 초기화
  selectStatus(currentStatCd);

  document.getElementById('status-form-card').classList.remove('hidden');
  // 카드가 화면에 보이도록 스크롤
  document.getElementById('status-form-card').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}

function closeStatusForm() {
  document.getElementById('status-form-card').classList.add('hidden');
  statusTarget.classSn = null;
  statusTarget.selectedStatCd = null;
}

/**
 * 상태 버튼 선택 처리.
 * 선택된 버튼에 hm-btn-sky 를 적용하고 나머지는 hm-btn-secondary 로 복원.
 */
function selectStatus(code) {
  statusTarget.selectedStatCd = code;
  var codes = ['WAITING', 'RECRUITING', 'ACTIVE', 'CLOSED'];
  codes.forEach(function(c) {
    var btn = document.getElementById('stat-btn-' + c);
    if (!btn) return;
    // 선택된 버튼: sky 색상 / 미선택: 기본 secondary
    if (c === code) {
      btn.classList.remove('hm-btn-secondary');
      btn.classList.add('hm-btn-primary', 'hm-btn-sky');
    } else {
      btn.classList.remove('hm-btn-primary', 'hm-btn-sky');
      btn.classList.add('hm-btn-secondary');
    }
  });
}

function showStatusError(msg) {
  var el = document.getElementById('status-error');
  el.textContent = msg;
  el.classList.remove('hidden');
}

function submitStatus() {
  if (!statusTarget.classSn) return;
  if (!statusTarget.selectedStatCd) {
    showStatusError('변경할 상태를 선택하세요.');
    return;
  }

  var submitBtn = document.getElementById('status-submit-btn');
  submitBtn.disabled = true;

  var params = new URLSearchParams();
  params.append('classStatCd', statusTarget.selectedStatCd);

  // Thymeleaf form 은 GET/POST만 지원하므로 AJAX POST 사용
  fetch('/classroom/' + statusTarget.classSn + '/status', {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params.toString()
  })
  .then(function(r) { return r.json(); })
  .then(function(data) {
    if (data.success) {
      closeStatusForm();
      loadClassrooms(currentPage); // 현재 페이지 유지하면서 목록 갱신
    } else {
      showStatusError(data.message || '상태 변경 중 오류가 발생했습니다.');
      submitBtn.disabled = false;
    }
  })
  .catch(function() {
    showStatusError('서버 오류가 발생했습니다.');
    submitBtn.disabled = false;
  });
}

/* ══════════════════════════════════════════════════
   페이지 초기 로드
══════════════════════════════════════════════════ */
document.addEventListener('DOMContentLoaded', function() { loadClassrooms(1); });
