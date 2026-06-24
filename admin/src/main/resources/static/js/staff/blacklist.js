/* ── 공통 유틸 ── */
function esc(v) {
  if (v == null) return '';
  return String(v).replace(/&/g, '&amp;').replace(/</g, '&lt;')
                  .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}
function fmtDt(dt) {
  if (!dt) return '-';
  if (Array.isArray(dt)) {
    const [y, mo, d] = dt;
    return `${y}-${String(mo).padStart(2,'0')}-${String(d).padStart(2,'0')}`;
  }
  return String(dt).replace('T', ' ').substring(0, 10);
}

const SCREEN_SIZE = 10, BLOCK_SIZE = 5;

/* ── 등급/유형 표시 메타 (코드값 기준) ── */
const LEVEL_META = {
  '01': { badge: 'bg-red-50 text-red-600',     icon: 'fa-circle-xmark', border: 'border-red-200',   avatar: 'bg-red-500' },
  '02': { badge: 'bg-amber-50 text-amber-600', icon: 'fa-eye',          border: 'border-amber-200', avatar: 'bg-amber-400' }
};
const RESOLVED_META = { badge: 'bg-emerald-50 text-emerald-700', icon: 'fa-circle-check', border: 'border-slate-100', avatar: 'bg-slate-300' };
const CATEGORY_ICON  = { '01': 'fa-hand-fist', '02': 'fa-won-sign', '03': 'fa-person-chalkboard', '04': 'fa-circle-dot' };
const CATEGORY_COLOR = { '01': 'bg-red-50 text-red-700', '02': 'bg-amber-50 text-amber-700', '03': 'bg-orange-50 text-orange-700', '04': 'bg-slate-100 text-slate-600' };

function metaOf(row) {
  return row.status === 'resolved' ? RESOLVED_META : (LEVEL_META[row.blklstLvlCd] || LEVEL_META['02']);
}

/* 정지 기간 표시: 해제됨 / 경고(차단없음) / 영구정지 / ~날짜까지 */
function suspendLabel(s) {
  if (s.status === 'resolved') return '해제됨';
  if (s.blklstLvlCd !== '01')  return '경고 · 차단 없음';
  return s.blklstEndDt ? '정지 ~ ' + fmtDt(s.blklstEndDt) + ' 까지' : '영구정지';
}

/* ── 셀렉트(커스텀셀렉트 호환) ── */
function setSelect(id, val) {
  const el = document.getElementById(id);
  if (!el) return;
  if (el.customSelect && el.customSelect.setValue) el.customSelect.setValue(val || '');
  else el.value = val || '';
}
function getSelect(id) { const el = document.getElementById(id); return el ? el.value : ''; }

/* ── 목록 검색 + 서버 페이징 ── */
let blPage = 1, kwTimer = null;
function onKeywordInput() { clearTimeout(kwTimer); kwTimer = setTimeout(() => doSearch(1), 300); }

async function doSearch(page) {
  blPage = page || 1;
  const p = new URLSearchParams();
  const kw = document.getElementById('fKeyword').value.trim();
  const lv = getSelect('fLevel'), ct = getSelect('fCategory'), st = document.getElementById('fStatus').value;
  if (kw) p.set('keyword', kw);
  if (lv) p.set('level', lv);
  if (ct) p.set('category', ct);
  if (st) p.set('status', st);
  p.set('page', blPage); p.set('screenSize', SCREEN_SIZE);
  try {
    const data = await fetch('/admin/blacklist/search?' + p).then(r => r.json());
    renderCards(data.items);
    renderStats(data.items);
    renderPager(data.totalCount, blPage);
  } catch (e) { console.error('주의 학생 조회 실패', e); }
}

function renderPager(total, page) {
  const info = document.getElementById('blPageInfo');
  const box  = document.getElementById('blPagination');
  const totalPages = Math.max(1, Math.ceil(total / SCREEN_SIZE));
  const start = total === 0 ? 0 : (page - 1) * SCREEN_SIZE + 1;
  const end   = Math.min(page * SCREEN_SIZE, total);
  info.textContent = total === 0 ? '' : `총 ${total}명 중 ${start}-${end}`;
  if (totalPages <= 1) { box.innerHTML = ''; return; }
  const bStart = Math.floor((page - 1) / BLOCK_SIZE) * BLOCK_SIZE + 1;
  const bEnd   = Math.min(bStart + BLOCK_SIZE - 1, totalPages);
  let html = `<button class="emp-page-btn" ${page <= 1 ? 'disabled' : ''} onclick="doSearch(${page - 1})"><i class="fa-solid fa-angle-left"></i></button>`;
  for (let pg = bStart; pg <= bEnd; pg++)
    html += `<button class="emp-page-btn ${pg === page ? 'active' : ''}" onclick="doSearch(${pg})">${pg}</button>`;
  html += `<button class="emp-page-btn" ${page >= totalPages ? 'disabled' : ''} onclick="doSearch(${page + 1})"><i class="fa-solid fa-angle-right"></i></button>`;
  box.innerHTML = html;
}

function renderCards(items) {
  const c = document.getElementById('blacklistCards');
  if (!items || items.length === 0) {
    c.innerHTML = `<div class="bg-white rounded-2xl border border-slate-100 shadow-sm py-16 text-center text-slate-300">
      <i class="fa-solid fa-triangle-exclamation text-4xl mb-3 block"></i>
      <p class="text-sm">조회된 주의 학생이 없습니다.</p></div>`;
    return;
  }
  c.innerHTML = items.map(s => {
    const m = metaOf(s);
    const resolved = s.status === 'resolved';
    const dim = resolved ? 'opacity-60' : '';
    const catColor = CATEGORY_COLOR[s.blklstCtgrCd] || CATEGORY_COLOR['04'];
    const catIcon  = CATEGORY_ICON[s.blklstCtgrCd]  || CATEGORY_ICON['04'];
    return `
      <div class="bg-white rounded-2xl border ${m.border} shadow-sm p-5 hover:shadow-md transition-shadow cursor-pointer ${dim}"
           onclick="openDetail('${esc(s.stdUserId)}')">
        <div class="flex items-start justify-between gap-4">
          <div class="flex items-start gap-4 flex-1 min-w-0">
            <div class="w-11 h-11 rounded-xl flex items-center justify-center text-white font-bold text-sm shrink-0 mt-0.5 ${m.avatar}">
              ${esc((s.userName || '?').charAt(0))}
            </div>
            <div class="flex-1 min-w-0">
              <div class="flex items-center gap-2 flex-wrap">
                <p class="font-bold text-slate-800">${esc(s.userName) || '-'}</p>
                <span class="text-xs text-slate-400">${esc(s.classNm) || '클래스룸 미배정'}</span>
              </div>
              <div class="flex items-center gap-2 mt-1.5 flex-wrap">
                <span class="inline-flex items-center gap-1 text-xs font-semibold px-2 py-0.5 rounded-full ${m.badge}">
                  <i class="fa-solid ${m.icon} text-[10px]"></i>${resolved ? '해제' : (esc(s.blklstLvlNm) || '-')}
                </span>
                <span class="inline-flex items-center gap-1 text-xs font-medium px-2 py-0.5 rounded-full ${catColor}">
                  <i class="fa-solid ${catIcon} text-[10px]"></i>${esc(s.blklstCtgrNm) || '-'}
                </span>
              </div>
              <p class="text-xs text-slate-500 mt-2 line-clamp-1">${esc(s.blklstRsnCn) || ''}</p>
            </div>
          </div>
          <div class="text-right shrink-0 space-y-1.5">
            <p class="text-xs text-slate-400 font-mono">${fmtDt(s.regDt || s.blklstStrtDt)}</p>
            <p class="text-xs font-medium ${s.status !== 'resolved' && s.blklstLvlCd === '01' ? 'text-red-500' : 'text-slate-400'}">${suspendLabel(s)}</p>
          </div>
        </div>
        ${s.blklstActnCn ? `
          <div class="mt-3.5 pt-3.5 border-t border-slate-50 flex items-center gap-2">
            <i class="fa-solid fa-clipboard-check text-emerald-400 text-xs shrink-0"></i>
            <p class="text-xs text-slate-500 truncate">${esc(s.blklstActnCn)}</p>
          </div>` : ''}
        <div class="mt-3.5 pt-3.5 border-t border-slate-50 flex items-center justify-between">
          <div class="flex items-center gap-1 text-xs text-slate-400">
            <i class="fa-solid fa-clock-rotate-left text-[10px]"></i><span>${s.logCount || 0}건의 기록</span>
          </div>
          <div class="flex items-center gap-1">
            <button onclick="event.stopPropagation(); openBlacklistModal('${esc(s.stdUserId)}')"
              class="text-xs text-slate-400 hover:text-blue-600 px-2.5 py-1 rounded-lg hover:bg-blue-50 transition-colors flex items-center gap-1">
              <i class="fa-regular fa-pen-to-square"></i> 수정
            </button>
            ${!resolved ? `
              <button onclick="event.stopPropagation(); resolveEntry('${esc(s.stdUserId)}')"
                class="text-xs text-slate-400 hover:text-emerald-600 px-2.5 py-1 rounded-lg hover:bg-emerald-50 transition-colors flex items-center gap-1">
                <i class="fa-solid fa-circle-check"></i> 해제
              </button>` : ''}
          </div>
        </div>
      </div>`;
  }).join('');
}

/* ── 유형별 현황 (현재 목록 기준, 적용 중만) ── */
function renderStats(items) {
  const counts = {};
  (items || []).filter(s => s.status !== 'resolved').forEach(s => {
    const nm = s.blklstCtgrNm || '기타';
    counts[nm] = (counts[nm] || 0) + 1;
  });
  const total = Object.values(counts).reduce((a, b) => a + b, 0) || 1;
  const box = document.getElementById('categoryStats');
  const entries = Object.entries(counts);
  if (entries.length === 0) { box.innerHTML = '<p class="text-xs text-slate-400">표시할 데이터가 없습니다.</p>'; return; }
  box.innerHTML = entries.map(([cat, cnt]) => {
    const pct = Math.round((cnt / total) * 100);
    return `
      <div>
        <div class="flex justify-between text-xs mb-1">
          <span class="font-medium text-slate-600">${esc(cat)}</span>
          <span class="font-semibold text-slate-700">${cnt}명 <span class="text-slate-400 font-normal">(${pct}%)</span></span>
        </div>
        <div class="h-2 bg-slate-100 rounded-full overflow-hidden">
          <div class="h-full rounded-full bg-blue-400" style="width:${pct}%"></div>
        </div>
      </div>`;
  }).join('');
}

/* ── 요약 카드 ── */
async function loadSummary() {
  try {
    const s = await fetch('/admin/blacklist/summary').then(r => r.json());
    document.getElementById('sumTotal').textContent    = (s.TOTAL_CNT     || 0) + '명';
    document.getElementById('sumHighRisk').textContent = (s.HIGH_RISK_CNT || 0) + '명';
    document.getElementById('sumObserve').textContent  = (s.OBSERVE_CNT   || 0) + '명';
    document.getElementById('sumResolved').textContent = (s.RESOLVED_CNT  || 0) + '명';
  } catch (e) { console.error('요약 조회 실패', e); }
}

/* ── 학생 검색 picker (등록 시 STD_USER_ID 선택) ── */
const pickTimer = {};
function pickIds(prefix) {
  return prefix === 'bl'
    ? { input: 'blName', list: 'blStudentResults', hid: 'blStudentId' }
    : { input: 'quickStudent', list: 'quickStudentResults', hid: 'quickStudentId' };
}
function searchStudentPicker(prefix) {
  const ids = pickIds(prefix);
  document.getElementById(ids.hid).value = ''; // 입력 변경 시 선택 무효화
  const kw = document.getElementById(ids.input).value.trim();
  clearTimeout(pickTimer[prefix]);
  pickTimer[prefix] = setTimeout(async () => {
    const list = document.getElementById(ids.list);
    try {
      const p = new URLSearchParams({ page: 1, screenSize: 8 });
      if (kw) p.set('keyword', kw);
      const data = await fetch('/admin/employees/students/search?' + p).then(r => r.json());
      const arr = data.items || [];
      list.innerHTML = arr.length === 0
        ? '<div class="px-3 py-2 text-xs text-slate-400">검색 결과 없음</div>'
        : arr.map(it => `
            <button type="button" class="w-full text-left px-3 py-2 text-sm hover:bg-blue-50 flex items-center gap-2"
              onclick="pickStudent('${prefix}','${esc(it.userId)}','${esc(it.userName)}')">
              <span class="font-semibold text-slate-700">${esc(it.userName)}</span>
              <span class="text-xs text-slate-400 font-mono">${esc(it.userId)}</span>
            </button>`).join('');
      list.classList.remove('hidden');
    } catch (e) { console.error('학생 검색 실패', e); }
  }, 250);
}
function pickStudent(prefix, userId, userName) {
  const ids = pickIds(prefix);
  document.getElementById(ids.input).value = userName;
  document.getElementById(ids.hid).value = userId;
  document.getElementById(ids.list).classList.add('hidden');
  // 등록 모달이면 선택한 학생의 수강중 클래스룸을 조회해 표시
  if (prefix === 'bl') {
    const grade = document.getElementById('blGrade');
    grade.value = '조회 중...';
    fetch('/admin/blacklist/classroom?userId=' + encodeURIComponent(userId))
      .then(r => r.json())
      .then(d => { grade.value = d.classNm || '클래스룸 미배정'; })
      .catch(() => { grade.value = '-'; });
  }
}
/* 바깥 클릭 시 결과 닫기 */
document.addEventListener('click', e => {
  ['blStudentResults', 'quickStudentResults'].forEach(id => {
    const list = document.getElementById(id);
    if (list && !list.classList.contains('hidden') && !list.parentElement.contains(e.target))
      list.classList.add('hidden');
  });
});

/* ── 상세 모달 ── */
const EVT_LABEL = { WARN: '경고', SUSP: '기간정지', PERM: '영구정지', MOD: '수정', REL: '해제' };

/* 등록 응답(페널티) → 안내 메시지 */
function penaltyMsg(p) {
  const n = p.offenseCount || 0;
  if (!p.blocked)   return `${n}회 위반 — 경고 등록 (로그인 제한 없음)`;
  if (p.permanent)  return `${n}회 위반 — 영구정지 (로그인 차단)`;
  return `${n}회 위반 — ${p.impsDays}일 정지 (로그인 차단)`;
}
async function openDetail(stdUserId) {
  try {
    const { info, logs } = await fetch('/admin/blacklist/detail?stdUserId=' + encodeURIComponent(stdUserId)).then(r => r.json());
    const resolved = info.status === 'resolved';
    const m = metaOf(info);
    document.getElementById('blDetailContent').innerHTML = `
      <div class="flex items-center gap-4">
        <div class="w-14 h-14 rounded-2xl flex items-center justify-center text-white font-bold text-xl ${m.avatar}">
          ${esc((info.userName || '?').charAt(0))}
        </div>
        <div>
          <div class="flex items-center gap-2 flex-wrap">
            <p class="text-lg font-bold text-slate-800">${esc(info.userName) || '-'}</p>
            <span class="inline-flex items-center gap-1 text-xs font-semibold px-2.5 py-1 rounded-full ${m.badge}">
              <i class="fa-solid ${m.icon} text-[10px]"></i>${resolved ? '해제' : (esc(info.blklstLvlNm) || '-')}
            </span>
          </div>
          <p class="text-sm text-slate-400 mt-0.5">${esc(info.classNm) || '클래스룸 미배정'}</p>
        </div>
      </div>
      <div class="grid grid-cols-2 gap-3 text-xs">
        <div class="bg-slate-50 rounded-xl p-3"><p class="text-slate-400 mb-1">유형</p><p class="font-semibold text-slate-700">${esc(info.blklstCtgrNm) || '-'}</p></div>
        <div class="bg-slate-50 rounded-xl p-3"><p class="text-slate-400 mb-1">정지 기간</p><p class="font-semibold ${info.status !== 'resolved' && info.blklstLvlCd === '01' ? 'text-red-600' : 'text-slate-700'}">${suspendLabel(info)}</p></div>
        <div class="bg-slate-50 rounded-xl p-3"><p class="text-slate-400 mb-1">등록자</p><p class="font-semibold text-slate-700">${esc(info.regUserName) || esc(info.regUserId) || '-'}</p></div>
        <div class="bg-slate-50 rounded-xl p-3"><p class="text-slate-400 mb-1">등록일</p><p class="font-semibold text-slate-700 font-mono">${fmtDt(info.regDt || info.blklstStrtDt)}</p></div>
      </div>
      <div class="bg-rose-50 border border-rose-100 rounded-xl p-4">
        <p class="text-xs font-bold text-rose-700 mb-1.5">등록 사유</p>
        <p class="text-sm text-rose-800 leading-relaxed">${esc(info.blklstRsnCn) || '-'}</p>
      </div>
      ${info.blklstActnCn ? `
        <div class="bg-emerald-50 border border-emerald-100 rounded-xl p-4">
          <p class="text-xs font-bold text-emerald-700 mb-1.5">조치 내용</p>
          <p class="text-sm text-emerald-800 leading-relaxed">${esc(info.blklstActnCn)}</p>
        </div>` : ''}
      <div>
        <p class="text-xs font-bold text-slate-500 mb-2">변경 이력</p>
        <div class="space-y-2">
          ${(logs || []).map(l => `
            <div class="flex items-start gap-2.5">
              <div class="w-5 h-5 rounded-full bg-blue-100 text-blue-500 flex items-center justify-center text-[9px] shrink-0 mt-0.5">
                <i class="fa-solid fa-clock"></i>
              </div>
              <p class="text-xs text-slate-500">
                <span class="font-mono text-slate-400">${fmtDt(l.blklstRegDt)}</span>
                <span class="font-semibold text-slate-600 ml-1">${EVT_LABEL[l.blklstEvtCd] || esc(l.blklstEvtCd) || ''}</span>
                ${l.blklstEvtCd === 'SUSP' && l.blklstImpsDaysCnt ? '· ' + l.blklstImpsDaysCnt + '일' : ''}
                ${l.rgtrUserName ? '· ' + esc(l.rgtrUserName) : ''}
                ${l.blklstRsnCn ? '— ' + esc(l.blklstRsnCn) : ''}
              </p>
            </div>`).join('') || '<p class="text-xs text-slate-400">이력이 없습니다.</p>'}
        </div>
      </div>`;
    document.getElementById('blDetailEditBtn').onclick = () => { closeDetailModal(); openBlacklistModal(info.stdUserId); };
    document.getElementById('blDetailModal').classList.remove('hidden');
  } catch (e) { console.error('상세 조회 실패', e); }
}
function closeDetailModal() { document.getElementById('blDetailModal').classList.add('hidden'); }

/* ── 등록/수정 모달 ── */
let editingId = null;
async function openBlacklistModal(stdUserId) {
  editingId = stdUserId || null;
  document.getElementById('blStudentResults').classList.add('hidden');
  document.getElementById('blForcePerm').checked = false;
  const nameInput = document.getElementById('blName');
  if (stdUserId) {
    document.getElementById('blModalTitle').textContent = '주의 학생 수정';
    nameInput.readOnly = true;
    try {
      const { info } = await fetch('/admin/blacklist/detail?stdUserId=' + encodeURIComponent(stdUserId)).then(r => r.json());
      nameInput.value = info.userName || '';
      document.getElementById('blStudentId').value = info.stdUserId;
      document.getElementById('blGrade').value = info.classNm || '-';
      setSelect('blCategory', info.blklstCtgrCd);
      document.getElementById('blReason').value = info.blklstRsnCn || '';
      document.getElementById('blAction').value = info.blklstActnCn || '';
    } catch (e) { console.error(e); }
  } else {
    document.getElementById('blModalTitle').textContent = '주의 학생 등록';
    nameInput.readOnly = false;
    nameInput.value = '';
    document.getElementById('blStudentId').value = '';
    document.getElementById('blGrade').value = '';
    setSelect('blCategory', '');
    document.getElementById('blReason').value = '';
    document.getElementById('blAction').value = '';
  }
  document.getElementById('blModal').classList.remove('hidden');
}
function closeBlacklistModal() {
  document.getElementById('blModal').classList.add('hidden');
  editingId = null;
}

async function saveBlacklist() {
  const stdUserId = document.getElementById('blStudentId').value;
  const category = getSelect('blCategory');
  const reason = document.getElementById('blReason').value.trim();
  const action = document.getElementById('blAction').value.trim();
  if (!stdUserId) { showHermesToast('학생을 검색하여 선택해 주세요.', 'error'); return; }
  if (!category)  { showHermesToast('유형을 선택해 주세요.', 'error'); return; }
  if (!reason)    { showHermesToast('등록 사유를 입력해 주세요.', 'error'); return; }
  const body = {
    stdUserId,
    blklstCtgrCd: category,
    blklstRsnCn: reason, blklstActnCn: action,
    forcePermanent: document.getElementById('blForcePerm').checked
  };
  try {
    const url    = editingId ? '/admin/blacklist/' + encodeURIComponent(editingId) : '/admin/blacklist';
    const method = editingId ? 'PUT' : 'POST';
    const res = await fetch(url, { method, headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    if (!res.ok) throw new Error(res.status);
    closeBlacklistModal();
    if (editingId) showHermesToast('수정되었습니다.', 'success');
    else           showHermesToast(penaltyMsg(await res.json()), 'success');
    refresh();
  } catch (e) { console.error(e); showHermesToast('저장에 실패했습니다.', 'error'); }
}

/* ── 빠른 등록 ── */
async function saveQuick() {
  const stdUserId = document.getElementById('quickStudentId').value;
  const category = getSelect('quickCategory');
  const reason = document.getElementById('quickReason').value.trim();
  if (!stdUserId) { showHermesToast('학생을 검색하여 선택해 주세요.', 'error'); return; }
  if (!category)  { showHermesToast('유형을 선택해 주세요.', 'error'); return; }
  if (!reason)    { showHermesToast('등록 사유를 입력해 주세요.', 'error'); return; }
  const body = { stdUserId, blklstCtgrCd: category, blklstRsnCn: reason };
  try {
    const res = await fetch('/admin/blacklist', { method: 'POST', headers: { 'Content-Type': 'application/json' }, body: JSON.stringify(body) });
    if (!res.ok) throw new Error(res.status);
    const p = await res.json();
    document.getElementById('quickStudent').value = '';
    document.getElementById('quickStudentId').value = '';
    document.getElementById('quickReason').value = '';
    setSelect('quickCategory', '');
    showHermesToast(penaltyMsg(p), 'success');
    refresh();
  } catch (e) { console.error(e); showHermesToast('저장에 실패했습니다.', 'error'); }
}

/* ── 해제 처리 ── */
async function resolveEntry(stdUserId) {
  try {
    const res = await fetch('/admin/blacklist/' + encodeURIComponent(stdUserId) + '/resolve', { method: 'PUT' });
    if (!res.ok) throw new Error(res.status);
    showHermesToast('주의 목록에서 해제되었습니다.', 'success');
    refresh();
  } catch (e) { console.error(e); showHermesToast('해제에 실패했습니다.', 'error'); }
}

/* ── 갱신 ── */
function refresh() { loadSummary(); doSearch(blPage); }

/* ── 초기화 ── */
window.addEventListener('load', function () {
  if (window.initDeferredSelects) {
    Promise.resolve(initDeferredSelects()).then(() => { loadSummary(); doSearch(1); });
  } else {
    loadSummary(); doSearch(1);
  }
});
