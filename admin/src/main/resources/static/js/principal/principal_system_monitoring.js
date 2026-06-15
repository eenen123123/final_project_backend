/* ─── 공통 유틸 ─────────────────────────────────────────────────── */
function esc(v) {
  if (v == null) return '';
  return String(v)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/* Jackson 기본 설정 시 LocalDateTime → 배열 [y,mo,d,h,mi,s], 설정 시 ISO 문자열 */
function formatDt(dt) {
  if (!dt) return '—';
  if (Array.isArray(dt)) {
    const [y, mo, d, h = 0, mi = 0, s = 0] = dt;
    return `${y}-${String(mo).padStart(2,'0')}-${String(d).padStart(2,'0')} `
         + `${String(h).padStart(2,'0')}:${String(mi).padStart(2,'0')}:${String(s).padStart(2,'0')}`;
  }
  return String(dt).replace('T', ' ').substring(0, 19);
}

const SCREEN_SIZE = 10;
const BLOCK_SIZE  = 5;

/* ─── 탭 전환 ──────────────────────────────────────────────────── */
function switchMainTab(tabId, btn) {
  document.querySelectorAll('.main-tab-panel').forEach(p => p.classList.add('hidden'));
  document.querySelectorAll('.main-tab-btn').forEach(b => {
    b.classList.remove('text-violet-600', 'border-violet-500', 'bg-white');
    b.classList.add('text-slate-500', 'border-transparent');
  });
  document.getElementById(tabId).classList.remove('hidden');
  btn.classList.remove('text-slate-500', 'border-transparent');
  btn.classList.add('text-violet-600', 'border-violet-500', 'bg-white');
}

function switchSubTab(btnClass, panelId, btn) {
  const parentPanel = btn.closest('.main-tab-panel');
  parentPanel.querySelectorAll('.' + btnClass).forEach(b => {
    b.classList.remove('text-violet-600', 'bg-violet-50', 'font-bold');
    b.classList.add('text-slate-400');
  });
  const prefix = panelId.replace(/-[^-]+$/, '-');
  parentPanel.querySelectorAll('[id^="' + prefix + '"]').forEach(p => p.classList.add('hidden'));
  document.getElementById(panelId).classList.remove('hidden');
  btn.classList.remove('text-slate-400');
  btn.classList.add('text-violet-600', 'bg-violet-50', 'font-bold');
}

/* ─── 서버사이드 페이지네이션 렌더 ─────────────────────────────── */
function renderPagination(pgId, infoId, totalCount, page, type) {
  const info = document.getElementById(infoId);
  const pg   = document.getElementById(pgId);
  if (!pg) return;

  const totalPage = Math.max(1, Math.ceil(totalCount / SCREEN_SIZE));
  const start = totalCount === 0 ? 0 : (page - 1) * SCREEN_SIZE + 1;
  const end   = Math.min(page * SCREEN_SIZE, totalCount);

  if (info) {
    info.textContent = totalCount === 0
      ? '데이터가 없습니다.'
      : `전체 ${totalCount}건 중 ${start} – ${end} 표시`;
  }

  if (totalPage <= 1) { pg.innerHTML = ''; return; }

  const blockEnd   = Math.ceil(page / BLOCK_SIZE) * BLOCK_SIZE;
  const blockStart = Math.max(blockEnd - BLOCK_SIZE + 1, 1);
  const realEnd    = Math.min(blockEnd, totalPage);

  let html = '';
  if (blockStart > 1)
    html += `<button onclick="goPage('${type}',${blockStart - 1})"
              class="w-8 h-8 rounded-lg text-slate-400 hover:bg-slate-100 text-xs flex items-center justify-center">‹</button>`;
  for (let p = blockStart; p <= realEnd; p++) {
    const cls = p === page ? 'bg-violet-600 text-white font-bold' : 'text-slate-400 hover:bg-slate-100';
    html += `<button onclick="goPage('${type}',${p})"
              class="w-8 h-8 rounded-lg ${cls} text-xs flex items-center justify-center">${p}</button>`;
  }
  if (realEnd < totalPage)
    html += `<button onclick="goPage('${type}',${realEnd + 1})"
              class="w-8 h-8 rounded-lg text-slate-400 hover:bg-slate-100 text-xs flex items-center justify-center">›</button>`;
  pg.innerHTML = html;
}

function goPage(type, page) {
  ({ aa: searchAdminAccess, aud: searchAdminAudit,
     ma: searchMemberAccess, mact: searchMemberActivity,
     err: searchSysError })[type]?.(page);
}

/* ─── 초기화 버튼 ──────────────────────────────────────────────── */
const RESET_IDS = {
  aa:   ['inp-aa-userId', 'inp-aa-fromDt', 'inp-aa-toDt', 'inp-aa-session'],
  aud:  ['inp-aud-keyword', 'inp-aud-method', 'inp-aud-status'],
  ma:   ['inp-ma-userId', 'inp-ma-success', 'inp-ma-fromDt'],
  mact: ['inp-mact-keyword', 'inp-mact-fromDt'],
  err:  ['inp-err-keyword', 'inp-err-fromDt']
};

function resetAndSearch(type) {
  (RESET_IDS[type] || []).forEach(id => {
    const el = document.getElementById(id);

    if (!el) return;
    if (el.customSelect) el.customSelect.setValue('');
    else {
      el.value = '';
      if (el.type === 'date') el.style.color = '#94a3b8';
    }
  });
  goPage(type, 1);
}

/* ─── 1. 관리자 접속 이력 ─────────────────────────────────────── */
async function searchAdminAccess(page = 1) {
  const params = new URLSearchParams({ page, screenSize: SCREEN_SIZE });
  const userId  = document.getElementById('inp-aa-userId').value.trim();
  const fromDt  = document.getElementById('inp-aa-fromDt').value;
  const toDt    = document.getElementById('inp-aa-toDt').value;
  const session = document.getElementById('inp-aa-session').value;
  if (userId)  params.set('userId', userId);
  if (fromDt)  params.set('fromDt', fromDt);
  if (toDt)    params.set('toDt',   toDt);
  if (session) params.set('sessionStatus', session);

  try {
    const data = await fetch('/admin/system/monitoring/admin-access?' + params).then(r => r.json());
    renderAdminAccessTable(data.items);
    renderPagination('pg-admin-access', 'pg-info-admin-access', data.totalCount, page, 'aa');
  } catch (e) { console.error('관리자 접속 이력 조회 실패', e); }
}

function renderAdminAccessTable(items) {
  const tbody = document.getElementById('tbody-admin-access');
  if (!items || items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-sm text-slate-400">조회된 데이터가 없습니다.</td></tr>';
    return;
  }
  tbody.innerHTML = items.map(r => {
    const initial = esc((r.userId || '?').charAt(0).toUpperCase());
    const status = r.logoutDt
      ? `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-slate-100 text-slate-500">
           <span class="w-1.5 h-1.5 rounded-full bg-slate-400"></span> 종료</span>`
      : `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700">
           <span class="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse"></span> 접속 중</span>`;
    return `<tr class="hover:bg-slate-50/50 transition-colors">
      <td class="py-4 px-6 font-mono text-xs text-slate-400">${esc(r.logId)}</td>
      <td class="py-4 px-6">
        <div class="flex items-center gap-2">

          <p class="font-semibold text-slate-800">${esc(r.userId)}</p>
        </div>
      </td>
      <td class="py-4 px-6 font-mono text-xs">${esc(r.loginIp) || '—'}</td>
      <td class="py-4 px-6 text-xs text-slate-500 font-mono">${formatDt(r.loginDt)}</td>
      <td class="py-4 px-6 text-xs text-slate-500 font-mono">${formatDt(r.logoutDt)}</td>
      <td class="py-4 px-6 text-center">${status}</td>
    </tr>`;
  }).join('');
}

/* ─── 2. 관리자 활동 감사 로그 ──────────────────────────────────── */
async function searchAdminAudit(page = 1) {
  const params = new URLSearchParams({ page, screenSize: SCREEN_SIZE });
  const keyword    = document.getElementById('inp-aud-keyword').value.trim();
  const httpMethod = document.getElementById('inp-aud-method').value;
  const statusCode = document.getElementById('inp-aud-status').value;
  if (keyword)    params.set('keyword',    keyword);
  if (httpMethod) params.set('httpMethod', httpMethod);
  if (statusCode) params.set('statusCode', statusCode);

  try {
    const data = await fetch('/admin/system/monitoring/admin-audit?' + params).then(r => r.json());
    renderAdminAuditTable(data.items);
    renderPagination('pg-admin-audit', 'pg-info-admin-audit', data.totalCount, page, 'aud');
  } catch (e) { console.error('관리자 감사 로그 조회 실패', e); }
}

const METHOD_BADGE = {
  GET:    'bg-sky-100 text-sky-700',
  POST:   'bg-amber-100 text-amber-700',
  PUT:    'bg-indigo-100 text-indigo-700',
  DELETE: 'bg-rose-100 text-rose-700'
};

function statusBadgeClass(code) {
  const n = parseInt(code);
  if (n >= 500) return 'bg-rose-100 text-rose-700';
  if (n >= 400) return 'bg-amber-100 text-amber-700';
  return 'bg-emerald-100 text-emerald-700';
}

function renderAdminAuditTable(items) {
  const tbody = document.getElementById('tbody-admin-audit');
  if (!items || items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-10 text-sm text-slate-400">조회된 데이터가 없습니다.</td></tr>';
    return;
  }
  tbody.innerHTML = items.map(r => {
    const mCls = METHOD_BADGE[r.httpMethod] || 'bg-slate-100 text-slate-600';
    const sCls = statusBadgeClass(r.statusCode);
    return `<tr class="hover:bg-slate-50/50 transition-colors">
      <td class="py-4 px-5 font-mono text-xs text-slate-400">${esc(r.auditId)}</td>
      <td class="py-4 px-5 font-semibold text-slate-800">${esc(r.adminId)}</td>
      <td class="py-4 px-5 text-center">
        <span class="px-2 py-0.5 rounded-md text-[10px] font-bold ${mCls}">${esc(r.httpMethod)}</span>
        </td>
        <td class="py-4 px-5 text-xs font-mono text-slate-600 whitespace-nowrap">${esc(r.requestUri)}</td>
        <td class="py-4 px-5 text-center">
          <span class="px-2 py-0.5 rounded-md text-[10px] font-bold ${sCls}">${esc(r.statusCode)}</span>
          </td>
      <td class="py-4 px-5 font-mono text-xs">${esc(r.memberIp) || '—'}</td>
      <td class="py-4 px-5 text-xs text-slate-500 font-mono">${esc(r.createdAt) || '—'}</td>
      <td class="py-4 px-5 text-center">
        <button class="text-slate-400 hover:text-violet-600 p-1.5 transition-colors"
                title="요청 파라미터 상세"
                data-audit-id="${esc(r.auditId)}"
                data-row="${esc(JSON.stringify(r))}"
                onclick="openAuditDetailModal(this.dataset.auditId, this.dataset.row)">
          <i class="fa-solid fa-file-lines text-base"></i>
        </button>
      </td>
    </tr>`;
  }).join('');
}

/* ─── 3. 사용자 접속 이력 ────────────────────────────────────────── */
async function searchMemberAccess(page = 1) {
  const params = new URLSearchParams({ page, screenSize: SCREEN_SIZE });
  const userId         = document.getElementById('inp-ma-userId').value.trim();
  const loginSuccessYn = document.getElementById('inp-ma-success').value;
  const fromDt         = document.getElementById('inp-ma-fromDt').value;
  if (userId)         params.set('userId',         userId);
  if (loginSuccessYn) params.set('loginSuccessYn', loginSuccessYn);
  if (fromDt)         params.set('fromDt',         fromDt);

  try {
    const data = await fetch('/admin/system/monitoring/member-access?' + params).then(r => r.json());
    renderMemberAccessTable(data.items);
    renderPagination('pg-member-access', 'pg-info-member-access', data.totalCount, page, 'ma');
  } catch (e) { console.error('사용자 접속 이력 조회 실패', e); }
}

function renderMemberAccessTable(items) {
  const tbody = document.getElementById('tbody-member-access');
  if (!items || items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-10 text-sm text-slate-400">조회된 데이터가 없습니다.</td></tr>';
    return;
  }
  tbody.innerHTML = items.map(r => {
    const isSuccess = r.loginSuccessYn === 'Y';
    const badge = isSuccess
      ? `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700"><i class="fa-solid fa-check text-[10px]"></i> 성공</span>`
      : `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold bg-rose-50 text-rose-600"><i class="fa-solid fa-xmark text-[10px]"></i> 실패</span>`;
    const failCell = isSuccess
      ? '<td class="py-4 px-6 text-xs text-slate-400">—</td>'
      : `<td class="py-4 px-6 text-xs text-rose-600 font-medium">${esc(r.failRsn) || '—'}</td>`;
    return `<tr class="hover:bg-slate-50/50 transition-colors">
      <td class="py-4 px-6 font-mono text-xs text-slate-400">${esc(r.logId)}</td>
      <td class="py-4 px-6 font-mono text-xs">${esc(r.inputUserId) || '—'}</td>
      <td class="py-4 px-6 font-semibold text-slate-800">${esc(r.userId) || '—'}</td>
      <td class="py-4 px-6 text-center">${badge}</td>
      ${failCell}
      <td class="py-4 px-6 text-xs font-mono text-slate-500">${formatDt(r.loginDt)}</td>
      <td class="py-4 px-6 font-mono text-xs">${esc(r.loginIp) || '—'}</td>
    </tr>`;
  }).join('');
}

/* ─── 4. 사용자 활동 이력 ────────────────────────────────────────── */
async function searchMemberActivity(page = 1) {
  const params = new URLSearchParams({ page, screenSize: SCREEN_SIZE });
  const keyword = document.getElementById('inp-mact-keyword').value.trim();
  const fromDt  = document.getElementById('inp-mact-fromDt').value;
  if (keyword) params.set('keyword', keyword);
  if (fromDt)  params.set('fromDt',  fromDt);

  try {
    const data = await fetch('/admin/system/monitoring/member-activity?' + params).then(r => r.json());
    renderMemberActivityTable(data.items);
    renderPagination('pg-member-activity', 'pg-info-member-activity', data.totalCount, page, 'mact');
  } catch (e) { console.error('사용자 활동 이력 조회 실패', e); }
}

const ACTIVITY_STYLE = {
  '강의 재생':    { cls: 'bg-violet-50 text-violet-700', icon: 'fa-play' },
  '과제 제출':    { cls: 'bg-sky-50 text-sky-700',       icon: 'fa-file-arrow-up' },
  '게시글 조회':  { cls: 'bg-slate-100 text-slate-600',  icon: 'fa-eye' },
  '댓글 작성':    { cls: 'bg-teal-50 text-teal-700',     icon: 'fa-comment' },
  '자료 다운로드': { cls: 'bg-amber-50 text-amber-700', icon: 'fa-download' }
};

function renderMemberActivityTable(items) {
  const tbody = document.getElementById('tbody-member-activity');
  if (!items || items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="9" class="text-center py-10 text-sm text-slate-400">조회된 데이터가 없습니다.</td></tr>';
    return;
  }
  tbody.innerHTML = items.map(r => {
    // activityType은 "METHOD URI" 형태 → 첫 공백 기준으로 분리
    const at = r.activityType || '';
    const sp = at.indexOf(' ');
    const method = sp > 0 ? at.substring(0, sp) : '';
    const uri    = sp > 0 ? at.substring(sp + 1) : at;
    const mCls = METHOD_BADGE[method] || 'bg-slate-100 text-slate-600';
    const sCls = statusBadgeClass(r.statusCode);
    return `<tr class="hover:bg-slate-50/50 transition-colors">
      <td class="py-4 px-5 font-mono text-xs text-slate-400">${esc(r.activityId)}</td>
      <td class="py-4 px-5 font-semibold text-slate-800">${esc(r.userId)}</td>
      <td class="py-4 px-5 text-center">
        <span class="px-2 py-0.5 rounded-md text-[10px] font-bold ${mCls}">${esc(method) || '—'}</span>
      </td>
      <td class="py-4 px-5 text-xs font-mono text-slate-600 whitespace-nowrap">${esc(uri) || '—'}</td>
      <td class="py-4 px-5 font-mono text-xs text-slate-500">${esc(r.targetId) || '—'}</td>
      <td class="py-4 px-5 text-center">
        <span class="px-2 py-0.5 rounded-md text-[10px] font-bold ${sCls}">${esc(r.statusCode) || '—'}</span>
      </td>
      <td class="py-4 px-5 font-mono text-xs">${esc(r.activityIp) || '—'}</td>
      <td class="py-4 px-5 text-xs text-slate-500 font-mono">${esc(r.createdAt) || '—'}</td>
      <td class="py-4 px-5 text-center">
        <button class="text-slate-400 hover:text-violet-600 p-1.5 transition-colors"
                title="활동 상세"
                data-row="${esc(JSON.stringify(r))}"
                onclick="openMemberActivityDetailModal(this.dataset.row)">
          <i class="fa-solid fa-file-lines text-base"></i>
        </button>
      </td>
    </tr>`;
  }).join('');
}

/* ─── 5. 시스템 에러 로그 ───────────────────────────────────────── */
async function searchSysError(page = 1) {
  const params = new URLSearchParams({ page, screenSize: SCREEN_SIZE });
  const keyword = document.getElementById('inp-err-keyword').value.trim();
  const fromDt  = document.getElementById('inp-err-fromDt').value;
  if (keyword) params.set('keyword', keyword);
  if (fromDt)  params.set('fromDt',  fromDt);

  try {
    const data = await fetch('/admin/system/monitoring/sys-error?' + params).then(r => r.json());
    renderSysErrorTable(data.items);
    renderPagination('pg-error', 'pg-info-error', data.totalCount, page, 'err');
  } catch (e) { console.error('시스템 에러 조회 실패', e); }
}

function errCodeBadge(code) {
  if (!code) return 'bg-slate-200 text-slate-600';
  if (code.includes('Null'))   return 'bg-rose-100 text-rose-700';
  if (code.includes('SQL'))    return 'bg-amber-100 text-amber-700';
  if (code.includes('IO'))     return 'bg-slate-200 text-slate-600';
  if (code.includes('Access') || code.includes('Auth')) return 'bg-orange-100 text-orange-700';
  return 'bg-slate-100 text-slate-700';
}

/* 구분(회원/관리자/익명) 뱃지 - 식별자 또는 IP를 함께 표시 */
function traceBadge(r) {

  if (r.traceType === 'MEMBER')
    return `<span class="px-2 py-0.5 rounded-md text-[10px] font-bold bg-sky-100 text-sky-700">회원</span>
      <span class="ml-1 font-mono text-xs text-slate-600">${esc(r.traceUserId) || ''}</span>`;
  if (r.traceType === 'ADMIN')
    return `<span class="px-2 py-0.5 rounded-md text-[10px] font-bold bg-indigo-100 text-indigo-700">관리자</span>
      <span class="ml-1 font-mono text-xs text-slate-600">${esc(r.traceUserId) || ''}</span>`;
  return `<span class="px-2 py-0.5 rounded-md text-[10px] font-bold bg-slate-200 text-slate-500">익명</span>
      <span class="ml-1 font-mono text-xs text-slate-400">${esc(r.requestIp) || '-'}</span>`;
}

function renderSysErrorTable(items) {
  const tbody = document.getElementById('tbody-error');
  if (!items || items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-10 text-sm text-slate-400">조회된 데이터가 없습니다.</td></tr>';
    return;
  }
  tbody.innerHTML = items.map(r => {
    const rawMsg = r.errorMessage || '(없음)';
    // traceType은 DB 조회 시 회원활동/관리자감사 테이블과 조인하여 내려온 값.
    // 추적 가능(회원/관리자)한 행에만 추적 버튼을 노출하고, 익명 행은 버튼 없음.
    const traceable = r.traceType === 'MEMBER' || r.traceType === 'ADMIN';
    const traceBtn = traceable
      ? `<button class="text-violet-600 hover:text-violet-800 hover:bg-violet-50 p-1.5 rounded-md transition-colors" title="사용자 활동 추적"
                 data-trace-id="${esc(r.traceId)}"
                 data-uri="${esc(r.requestUri)}"
                 data-code="${esc(r.errorCode)}"
                 onclick="openTraceModal(this.dataset.traceId, this.dataset.uri, this.dataset.code)">
           <i class="fa-solid fa-magnifying-glass-chart text-base"></i>
         </button>`
      : '';
    return `<tr class="hover:bg-slate-50/50 transition-colors">
      <td class="py-4 px-6 font-mono text-xs text-slate-400 truncate">${esc(r.errorId)}</td>
      <td class="py-4 px-6 truncate" title="${esc(r.traceId)}">
        <span class="font-mono text-xs text-violet-600 bg-violet-50 px-2 py-0.5 rounded-md">${esc(r.traceId) || '—'}</span>
      </td>
      <td class="py-4 px-2 truncate" title="${esc(r.traceUserId) || esc(r.requestIp) || ''}">${traceBadge(r)}</td>
      <td class="py-4 px-2 truncate" title="${esc(r.errorCode)}">
        <span class="px-2 py-0.5 rounded-md text-[10px] font-bold ${errCodeBadge(r.errorCode)}">${esc(r.errorCode) || '—'}</span>
      </td>
      <td class="py-4 px-6 text-xs font-mono text-slate-600 truncate" title="${esc(r.requestUri)}">${esc(r.requestUri) || '—'}</td>
      <td class="py-4 px-6 text-xs font-mono text-slate-500 truncate">${esc(r.createdAt) || '—'}</td>
      <td class="py-4 px-6 text-center whitespace-nowrap">
        ${traceBtn}
        <button class="text-slate-400 hover:text-rose-600 p-1.5 transition-colors"
                title="에러 메시지 상세"
                data-error-id="${esc(r.errorId)}"
                data-error-code="${esc(r.errorCode)}"
                data-error-msg="${esc(rawMsg)}"
                onclick="openErrorDetailModal(this.dataset.errorId, this.dataset.errorCode, this.dataset.errorMsg)">
          <i class="fa-solid fa-circle-exclamation text-base"></i>
        </button>
      </td>
    </tr>`;
  }).join('');
}

/* ─── 모달 ──────────────────────────────────────────────────────── */
function openAuditDetailModal(auditId, rowJson) {
  document.getElementById('auditDetailTitle').textContent = `감사 로그 #${auditId}`;
  try {
    const parsed = JSON.parse(rowJson);
    document.getElementById('auditDetailContent').textContent = JSON.stringify(parsed, null, 2);
  } catch (e) {
    document.getElementById('auditDetailContent').textContent = rowJson || '(없음)';
  }
  const m = document.getElementById('auditDetailModal');
  m.classList.remove('hidden'); m.classList.add('flex');
}
function closeAuditDetailModal() {
  const m = document.getElementById('auditDetailModal');
  m.classList.add('hidden'); m.classList.remove('flex');
}
document.getElementById('auditDetailModal').addEventListener('click', function(e) {
  if (e.target === this) closeAuditDetailModal();
});

function openMemberActivityDetailModal(rowJson) {
  let r;
  try { r = JSON.parse(rowJson); } catch (e) { r = {}; }
  const at = r.activityType || '';
  const sp = at.indexOf(' ');
  const method = sp > 0 ? at.substring(0, sp) : '';
  const uri    = sp > 0 ? at.substring(sp + 1) : at;
  const fields = [
    ['활동 번호', r.activityId],
    ['Trace ID', r.traceId],
    ['사용자 ID', r.userId],
    ['메서드', method],
    ['요청 URI', uri],
    ['대상 번호', r.targetId],
    ['상태 코드', r.statusCode],
    ['발생 IP', r.activityIp],
    ['활동 일시', r.createdAt],
  ];
  document.getElementById('mactDetailContent').innerHTML = fields.map(([k, v]) =>
    `<div class="flex gap-3 py-2 border-b border-slate-50">
       <span class="w-24 shrink-0 text-xs font-bold text-slate-400">${k}</span>
       <span class="font-mono text-xs text-slate-700 break-all">${esc(v) || '—'}</span>
     </div>`).join('');
  const m = document.getElementById('mactDetailModal');
  m.classList.remove('hidden'); m.classList.add('flex');
}
function closeMemberActivityDetailModal() {
  const m = document.getElementById('mactDetailModal');
  m.classList.add('hidden'); m.classList.remove('flex');
}
document.getElementById('mactDetailModal').addEventListener('click', function(e) {
  if (e.target === this) closeMemberActivityDetailModal();
});

function openErrorDetailModal(errorId, errorCode, msg) {
  document.getElementById('errorDetailTitle').textContent = `에러 #${errorId} — ${errorCode || ''}`;
  document.getElementById('errorDetailContent').textContent = msg || '(없음)';
  const m = document.getElementById('errorDetailModal');
  m.classList.remove('hidden'); m.classList.add('flex');
}
function closeErrorDetailModal() {
  const m = document.getElementById('errorDetailModal');
  m.classList.add('hidden'); m.classList.remove('flex');
}
document.getElementById('errorDetailModal').addEventListener('click', function(e) {
  if (e.target === this) closeErrorDetailModal();
});

/* ─── 에러 추적 모달 ────────────────────────────────────────────── */
async function openTraceModal(traceId, requestUri, errorCode) {
  const titleEl = document.getElementById('traceTitle');
  const subEl   = document.getElementById('traceSubtitle');
  const bodyEl  = document.getElementById('traceContent');
  const ctx = { traceId, requestUri, errorCode };
  titleEl.textContent = '에러 추적';
  subEl.textContent = `traceId: ${traceId}`;
  bodyEl.innerHTML = '<p class="text-center py-10 text-sm text-slate-400">조회 중...</p>';

  const m = document.getElementById('traceModal');
  m.classList.remove('hidden'); m.classList.add('flex');

  try {
    const data = await fetch('/admin/system/monitoring/trace?traceId=' + encodeURIComponent(traceId)).then(r => r.json());
    renderTrace(titleEl, bodyEl, data, ctx);
  } catch (e) {
    bodyEl.innerHTML = '<p class="text-center py-10 text-sm text-rose-500">추적 조회 실패</p>';
  }
}

/* 이 에러가 발생한 요청을 모달 상단에 고정 표시 */
function errorContextBanner(ctx) {
  return `<div class="mb-4 rounded-lg bg-rose-50 border border-rose-100 px-4 py-3">
    <p class="text-xs text-rose-400 mb-0.5">이 에러가 발생한 요청</p>
    <p class="font-mono text-sm text-rose-700 break-all">${esc(ctx.errorCode) || ''} · ${esc(ctx.requestUri) || '—'}</p>
  </div>`;
}

function renderTrace(titleEl, bodyEl, data, ctx) {
  const banner = errorContextBanner(ctx);
  if (data.type === 'MEMBER' || data.type === 'ADMIN') {
    const isMember = data.type === 'MEMBER';
    titleEl.innerHTML = `${isMember ? '회원' : '관리자'} <span class="font-mono text-violet-600">${esc(data.userId)}</span> 최근 활동`;
    const list = data.activities || [];
    if (list.length === 0) {
      bodyEl.innerHTML = banner + '<p class="text-center py-10 text-sm text-slate-400">활동 이력이 없습니다.</p>';
      return;
    }
    const headers = isMember ? ['메서드', 'URI', '대상', '상태', 'IP', '일시'] : ['메서드', 'URI', '상태', 'IP', '일시'];
    const rows = list.map(a => isMember ? memberRow(a, ctx.traceId) : adminRow(a, ctx.traceId)).join('');
    bodyEl.innerHTML = banner + traceTable(headers, rows)
      + '<p class="text-[11px] text-slate-400 mt-2">붉게 표시된 행이 이 에러가 발생한 요청입니다.</p>';
  } else {
    titleEl.innerHTML = `익명 · IP <span class="font-mono text-slate-600">${esc(data.requestIp) || '—'}</span>`;
    const list = data.sameIpErrors || [];
    if (list.length === 0) {
      bodyEl.innerHTML = banner + '<p class="text-center py-10 text-sm text-slate-400">같은 IP의 에러가 없습니다.</p>';
      return;
    }
    const rows = list.map(e => {
      const hit = e.traceId && e.traceId === ctx.traceId;
      return `<tr class="border-b border-slate-50 ${hit ? 'bg-rose-50' : ''}">
        <td class="py-2.5 px-4 font-mono text-xs">${esc(e.errorCode) || '—'}</td>
        <td class="py-2.5 px-4 font-mono text-xs text-slate-600 break-all">${esc(e.requestUri) || '—'}</td>
        <td class="py-2.5 px-4 font-mono text-xs text-slate-500">${esc(e.createdAt) || '—'}</td>
      </tr>`;
    }).join('');
    bodyEl.innerHTML = banner + traceTable(['에러 코드', 'URI', '일시'], rows);
  }
}

function traceTable(headers, rows) {
  const ths = headers.map(h => `<th class="py-2.5 px-4 text-left font-bold text-slate-500">${h}</th>`).join('');
  return `<table class="w-full text-sm border-collapse">
    <thead><tr class="bg-slate-50 text-xs">${ths}</tr></thead>
    <tbody>${rows}</tbody></table>`;
}

function memberRow(a, traceId) {
  const hit = a.traceId && a.traceId === traceId;
  const mark = hit ? ' <span class="ml-1 text-[10px] font-bold text-rose-600"></span>' : '';
  // activityType은 "METHOD URI" 형태이므로 첫 공백 기준으로 분리해 관리자 표와 통일
  const at = a.activityType || '';
  const sp = at.indexOf(' ');
  const method = sp > 0 ? at.substring(0, sp) : '';
  const uri = sp > 0 ? at.substring(sp + 1) : at;
  return `<tr class="border-b border-slate-50 ${hit ? 'bg-rose-50' : ''}">
    <td class="py-2.5 px-4 font-mono text-xs">${esc(method) || '—'}</td>
    <td class="py-2.5 px-4 font-mono text-xs text-slate-600 break-all">${esc(uri) || '—'}${mark}</td>
    <td class="py-2.5 px-4 font-mono text-xs text-slate-600">${esc(a.targetId) || '—'}</td>
    <td class="py-2.5 px-4 font-mono text-xs">${esc(a.statusCode) || '—'}</td>
    <td class="py-2.5 px-4 font-mono text-xs text-slate-500">${esc(a.activityIp) || '—'}</td>
    <td class="py-2.5 px-4 font-mono text-xs text-slate-500">${esc(a.createdAt) || '—'}</td>
  </tr>`;
}

function adminRow(a, traceId) {
  const hit = a.traceId && a.traceId === traceId;
  const mark = hit ? ' <span class="ml-1 text-[10px] font-bold text-rose-600"></span>' : '';
  return `<tr class="border-b border-slate-50 ${hit ? 'bg-rose-50' : ''}">
    <td class="py-2.5 px-4 font-mono text-xs">${esc(a.httpMethod) || '—'}</td>
    <td class="py-2.5 px-4 font-mono text-xs text-slate-600 break-all">${esc(a.requestUri) || '—'}${mark}</td>
    <td class="py-2.5 px-4 font-mono text-xs">${esc(a.statusCode) || '—'}</td>
    <td class="py-2.5 px-4 font-mono text-xs text-slate-500">${esc(a.memberIp) || '—'}</td>
    <td class="py-2.5 px-4 font-mono text-xs text-slate-500">${esc(a.createdAt) || '—'}</td>
  </tr>`;
}

function closeTraceModal() {
  const m = document.getElementById('traceModal');
  m.classList.add('hidden'); m.classList.remove('flex');
}
document.getElementById('traceModal').addEventListener('click', function(e) {
  if (e.target === this) closeTraceModal();
});

/* ─── 초기 로드 ─────────────────────────────────────────────────── */
(function init() {
  /* 대탭 첫 번째 활성 */
  const firstMain = document.querySelector('.main-tab-btn[data-main-tab="main-admin"]');
  if (firstMain) switchMainTab('main-admin', firstMain);

  /* 소탭 첫 번째 활성 */
  const firstAdminSub = document.querySelector('.admin-sub-btn[data-sub-tab="sub-admin-access"]');
  if (firstAdminSub) switchSubTab('admin-sub-btn', 'sub-admin-access', firstAdminSub);

  const firstMemberSub = document.querySelector('.member-sub-btn[data-sub-tab="sub-member-access"]');
  if (firstMemberSub) switchSubTab('member-sub-btn', 'sub-member-access', firstMemberSub);

  /* 모든 탭 초기 데이터 로드 */
  searchAdminAccess(1);
  searchAdminAudit(1);
  searchMemberAccess(1);
  searchMemberActivity(1);
  searchSysError(1);

  /* 필터 자동 검색 - 텍스트 debounce 300ms, select/date 즉시 */
  const SEARCH_FN = {
    aa: searchAdminAccess,
    aud: searchAdminAudit,
    ma: searchMemberAccess,
    mact: searchMemberActivity,
    err: searchSysError
  };
  function debounce(fn, ms = 300) {
    let t;
    return () => { clearTimeout(t);
      t = setTimeout(() => fn(1), ms);
    };
  }
  Object.entries(RESET_IDS).forEach(([type, ids]) => {
    ids.forEach(id => {
      const el = document.getElementById(id);
      if (!el) return;
      const isText = el.tagName !== 'SELECT' && el.type !== 'date';
      el.addEventListener(isText ? 'input' : 'change',
      isText ? debounce(SEARCH_FN[type]) : () => SEARCH_FN[type](1));
    });
  });
  /* 날짜 input 빈 상태 글자색 보정 */
  document.querySelectorAll('input[type="date"].hm-input').forEach(el => {
    const syncColor = () => { el.style.color = el.value ? '#1e293b' : '#94a3b8'; };
    syncColor();
    el.addEventListener('change', syncColor);
  });
})();
