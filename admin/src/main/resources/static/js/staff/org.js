/* ── 탭 전환 ── */
function switchTab(id, btn) {
  document.querySelectorAll('.org-panel').forEach(p => p.classList.add('hidden'));
  document.querySelectorAll('.org-tab').forEach(b => {
    b.classList.remove('active');
    b.classList.add('text-slate-500');
  });
  document.getElementById(id).classList.remove('hidden');
  btn.classList.add('active');
  btn.classList.remove('text-slate-500');
  if (id === 'tab-grade')   loadGradesOnce();
  if (id === 'tab-mapping') loadMntKanbanOnce();
}

/* ── 초기 카운트 + deptMap + 사수배정 직급 옵션 스냅샷 ── */
const deptMap = {};
let allMntGradeOpts = [];
let gradeOrdMap    = {}; /* jbgrNm → sortOrd (직급 rank 조회용) */

/* 스크립트가 body 하단에 위치하므로 DOM 파싱 완료 — 즉시 실행 가능 */
function initMntGradeOpts() {
  const opts = document.querySelectorAll('#mnt-grade-filter option[value]:not([value=""])');
  if (opts.length > 0) {
    allMntGradeOpts = Array.from(opts).map(opt => ({
      value:   opt.value,
      text:    opt.textContent.trim(),
      dept:    (opt.dataset.dept || '').trim(),
      sortOrd: parseInt(opt.dataset.sortOrd || '999', 10)
    }));
    gradeOrdMap = Object.fromEntries(allMntGradeOpts.map(o => [o.value, o.sortOrd]));
  }
}
initMntGradeOpts(); /* 즉시 초기화 */

document.addEventListener('DOMContentLoaded', () => {
  document.getElementById('dept-count').textContent = document.querySelectorAll('#dept-tbody tr').length;
  document.querySelectorAll('#grade-dept-filter option[value]').forEach(opt => {
    if (opt.value) deptMap[opt.value] = opt.textContent.trim();
  });
  if (allMntGradeOpts.length === 0) initMntGradeOpts();
  loadCommonCodes('mnt-stat-filter', '200', '전체 상태');
});

/* ─────────────── 직급 DB 페이징+필터 ─────────────── */
let gradeCurPage = 1;
let gradeLoaded  = false;
const GRADE_SIZE = 10;

function loadGradesOnce() {
  if (!gradeLoaded) { gradeLoaded = true; loadGrades(1); }
}

function loadGrades(page) {
  gradeCurPage = page;
  const deptCd = document.getElementById('grade-dept-filter').value;
  const useYn  = document.getElementById('grade-use-filter').value;
  const url = `/admin/org/grade/list?deptCd=${encodeURIComponent(deptCd)}&useYn=${encodeURIComponent(useYn)}&page=${page}&size=${GRADE_SIZE}`;
  fetch(url).then(r => r.json()).then(data => {
    renderGrades(data.list);
    document.getElementById('grade-count').textContent = data.total;
    renderGradePagination(data.page, data.totalPages);
  });
}

function renderGrades(list) {
  const tbody = document.getElementById('grade-tbody');
  if (!list || list.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="py-8 text-center text-slate-400 text-sm">조회된 직급이 없습니다.</td></tr>';
    return;
  }
  tbody.innerHTML = list.map(g => {
    const deptNm   = deptMap[g.deptCd] || g.deptCd || '-';
    const days     = g.baseAnnLvDays != null ? g.baseAnnLvDays + '일' : '15일';
    const badge    = g.useYn === 'Y'
      ? '<span class="px-2 py-0.5 rounded-full text-xs font-bold bg-emerald-100 text-emerald-700">사용중</span>'
      : '<span class="px-2 py-0.5 rounded-full text-xs font-bold bg-slate-100 text-slate-400">미사용</span>';
    const toggleTxt = g.useYn === 'Y' ? '비활성화' : '활성화';
    const toggleVal = g.useYn === 'Y' ? 'N' : 'Y';
    return `<tr class="hover:bg-slate-50 transition-colors"
                data-cd="${escHtml(g.jbgrCd)}" data-nm="${escHtml(g.jbgrNm)}"
                data-dept="${escHtml(g.deptCd || '')}" data-ord="${g.sortOrd ?? ''}"
                data-days="${g.baseAnnLvDays ?? ''}" data-use="${g.useYn}">
      <td class="py-3 px-4 font-mono text-slate-600">${escHtml(g.jbgrCd)}</td>
      <td class="py-3 px-4 font-semibold text-slate-800">${escHtml(g.jbgrNm)}</td>
      <td class="py-3 px-4 text-slate-500">${escHtml(deptNm)}</td>
      <td class="py-3 px-4 text-slate-500">${g.sortOrd ?? '-'}</td>
      <td class="py-3 px-4 text-slate-500">${days}</td>
      <td class="py-3 px-4">${badge}</td>
      <td class="py-3 px-4">
        <div class="flex items-center gap-2">
          <button onclick="openGradeModal(this.closest('tr'))" class="text-xs text-[#3b82f6] hover:underline font-semibold">수정</button>
          <button data-cd="${escHtml(g.jbgrCd)}" data-toggle="${toggleVal}"
                  onclick="toggleGrade(this.dataset.cd, this.dataset.toggle)"
                  class="text-xs text-slate-400 hover:underline">${toggleTxt}</button>
        </div>
      </td>
    </tr>`;
  }).join('');
}

function renderGradePagination(cur, total) {
  const el = document.getElementById('grade-pagination');
  if (total <= 1) { el.innerHTML = ''; return; }
  let html = '';
  for (let i = 1; i <= total; i++) {
    const cls = i === cur ? 'active' : '';
    html += `<button onclick="loadGrades(${i})" class="emp-page-btn ${cls}">${i}</button>`;
  }
  el.innerHTML = html;
}


/* ─────────────── 부서 CRUD ─────────────── */
function openDeptModal(row) {
  const isEdit = !!row;
  document.getElementById('dept-modal-title').innerHTML =
    `<i class="fa-solid fa-building mr-2 text-blue-500"></i>${isEdit ? '부서 수정' : '부서 추가'}`;
  document.getElementById('dept-edit-cd').value = isEdit ? row.dataset.cd : '';
  document.getElementById('dept-cd').value       = isEdit ? row.dataset.cd : '';
  document.getElementById('dept-cd').readOnly    = isEdit;
  document.getElementById('dept-nm').value       = isEdit ? row.dataset.nm : '';
  document.getElementById('dept-prnt').value     = isEdit ? (row.dataset.prnt || '') : '';
  document.getElementById('dept-tel').value      = isEdit ? (row.dataset.tel || '') : '';
  document.getElementById('modal-dept').classList.remove('hidden');
}
function closeDeptModal() { document.getElementById('modal-dept').classList.add('hidden'); }

function saveDept() {
  const editCd = document.getElementById('dept-edit-cd').value;
  const isEdit = !!editCd;
  const body = {
    deptCd:     document.getElementById('dept-cd').value.trim(),
    deptNm:     document.getElementById('dept-nm').value.trim(),
    prntDeptCd: document.getElementById('dept-prnt').value.trim() || null,
    intlTelNo:  document.getElementById('dept-tel').value.trim() || null,
  };
  if (!body.deptNm) return alert('부서명을 입력하세요.');
  if (!isEdit && !body.deptCd) return alert('부서 코드를 입력하세요.');

  const url    = isEdit ? `/admin/org/dept/${editCd}` : '/admin/org/dept';
  const method = isEdit ? 'PUT' : 'POST';
  fetch(url, { method, headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
    .then(r => r.json()).then(d => {
      if (d.result === 'success') { closeDeptModal(); location.reload(); }
      else alert('오류: ' + (d.message || '처리 실패'));
    });
}

function toggleDept(deptCd, newUseYn) {
  if (!confirm(`해당 부서를 ${newUseYn === 'Y' ? '활성화' : '비활성화'}하시겠습니까?`)) return;
  fetch(`/admin/org/dept/${deptCd}/toggle`, {
    method: 'PUT', headers: {'Content-Type':'application/json'},
    body: JSON.stringify({ useYn: newUseYn })
  }).then(r => r.json()).then(d => {
    if (d.result === 'success') location.reload();
    else alert('오류: ' + d.message);
  });
}

/* ─────────────── 직급 CRUD ─────────────── */
function openGradeModal(row) {
  const isEdit = !!row;
  document.getElementById('grade-modal-title').innerHTML =
    `<i class="fa-solid fa-layer-group mr-2 text-blue-500"></i>${isEdit ? '직급 수정' : '직급 추가'}`;
  document.getElementById('grade-edit-cd').value = isEdit ? row.dataset.cd : '';
  document.getElementById('grade-cd').value       = isEdit ? row.dataset.cd : '';
  document.getElementById('grade-cd').readOnly    = isEdit;
  document.getElementById('grade-nm').value       = isEdit ? row.dataset.nm : '';
  document.getElementById('grade-dept').value     = isEdit ? (row.dataset.dept || '') : '';
  document.getElementById('grade-ord').value      = isEdit ? (row.dataset.ord || '') : '';
  document.getElementById('grade-days').value     = isEdit ? (row.dataset.days || '') : '';
  document.getElementById('modal-grade').classList.remove('hidden');
}
function closeGradeModal() { document.getElementById('modal-grade').classList.add('hidden'); }

function saveGrade() {
  const editCd = document.getElementById('grade-edit-cd').value;
  const isEdit = !!editCd;
  const body = {
    jbgrCd:        document.getElementById('grade-cd').value.trim(),
    jbgrNm:        document.getElementById('grade-nm').value.trim(),
    deptCd:        document.getElementById('grade-dept').value.trim() || null,
    sortOrd:       parseInt(document.getElementById('grade-ord').value) || null,
    baseAnnLvDays: parseInt(document.getElementById('grade-days').value) || 15,
  };
  if (!body.jbgrNm) return alert('직급명을 입력하세요.');
  if (!isEdit && !body.jbgrCd) return alert('직급 코드를 입력하세요.');

  const url    = isEdit ? `/admin/org/grade/${editCd}` : '/admin/org/grade';
  const method = isEdit ? 'PUT' : 'POST';
  fetch(url, { method, headers: {'Content-Type':'application/json'}, body: JSON.stringify(body) })
    .then(r => r.json()).then(d => {
      if (d.result === 'success') {
        closeGradeModal();
        loadGrades(isEdit ? gradeCurPage : 1);
      } else alert('오류: ' + (d.message || '처리 실패'));
    });
}

function toggleGrade(jbgrCd, newUseYn) {
  if (!confirm(`해당 직급을 ${newUseYn === 'Y' ? '활성화' : '비활성화'}하시겠습니까?`)) return;
  fetch(`/admin/org/grade/${jbgrCd}/toggle`, {
    method: 'PUT', headers: {'Content-Type':'application/json'},
    body: JSON.stringify({ useYn: newUseYn })
  }).then(r => r.json()).then(d => {
    if (d.result === 'success') loadGrades(gradeCurPage);
    else alert('오류: ' + d.message);
  });
}

/* ═══════════════════════════════════════════
   사수 배정 — 좌(미배정+필터) / 우(팀 현황)
═══════════════════════════════════════════ */
let mntDeptCd       = '';
let mntViewMode     = 'kanban'; /* 'kanban' | 'tree' */
let mntKanbanLoaded = false;
let allEmps         = [];
let extraSups       = []; /* "팀 추가"로 만든 임시 사수 열 */
let dragUserId      = null;
let dragCurMnt      = null;
let pendingChanges  = new Map(); /* userId → newMntUserId (일괄 등록 대기 목록) */

/* 트리 뷰 사용 부서 (행정팀 D100, PD팀 D200) */
const TREE_VIEW_DEPTS = new Set(['D100', 'D200']);

function selectMntDept(btn) {
  document.querySelectorAll('.mnt-dept-btn').forEach(b => b.classList.remove('active'));
  btn.classList.add('active');
  mntDeptCd  = (btn.dataset.dept || '').trim();
  mntViewMode = TREE_VIEW_DEPTS.has(mntDeptCd) ? 'tree' : 'kanban';
  extraSups  = [];
  filterMntGradeByDept(mntDeptCd);
  loadMntKanban();
}

/* 부서 선택 시 직급 옵션 재구성 */
function filterMntGradeByDept(deptCd) {
  if (allMntGradeOpts.length === 0) initMntGradeOpts();
  const trimmed = (deptCd || '').trim();
  const sel = document.getElementById('mnt-grade-filter');
  Array.from(sel.options).forEach(opt => {
    if (!opt.value) return;
    opt.hidden = trimmed ? (opt.dataset.dept || '').trim() !== trimmed : false;
  });
  sel.value = '';
  if (sel.customSelect) sel.customSelect.refresh();
  renderUnassigned();
}

function loadMntKanbanOnce() {
  if (!mntKanbanLoaded) { mntKanbanLoaded = true; loadMntKanban(); }
}

function loadMntKanban() {
  document.getElementById('mnt-unassigned-list').innerHTML =
    '<p class="text-xs text-slate-300 text-center py-8">불러오는 중...</p>';
  document.getElementById('mnt-teams').innerHTML =
    '<div class="text-slate-300 text-sm self-center px-4">불러오는 중...</div>';
  fetch(`/admin/org/mapping/employees?deptCd=${encodeURIComponent(mntDeptCd)}`)
    .then(r => {
      if (!r.ok) throw new Error(`서버 오류 ${r.status}`);
      return r.json();
    })
    .then(emps => {
      allEmps = emps;
      /* 대기 중인 변경 사항을 서버 데이터 위에 다시 적용 */
      pendingChanges.forEach((mntId, uid) => {
        const emp = allEmps.find(e => e.userId === uid);
        if (emp) emp.mntUserId = mntId;
      });
      renderUnassigned();
      renderTeams();
    })
    .catch(err => {
      document.getElementById('mnt-unassigned-list').innerHTML =
        `<p class="text-xs text-red-400 text-center py-8">데이터 로드 실패<br>${err.message}</p>`;
      document.getElementById('mnt-teams').innerHTML =
        `<p class="text-sm text-red-400 self-center px-4">데이터 로드 실패: ${err.message}</p>`;
    });
}

/* ── 왼쪽: 미배정 목록 렌더 (필터 적용) ── */
function renderUnassigned() {
  const kw    = (document.getElementById('mnt-search').value || '').trim().toLowerCase();
  const grade = document.getElementById('mnt-grade-filter').value;
  const stat  = document.getElementById('mnt-stat-filter').value;

  const list = allEmps.filter(e => {
    if (e.mntUserId) return false;
    if (kw    && !((e.userName||'').toLowerCase().includes(kw) || (e.userId||'').toLowerCase().includes(kw))) return false;
    if (grade && e.jbgrNm !== grade) return false;
    if (stat  && e.emplStatCd !== stat) return false;
    return true;
  });

  document.getElementById('mnt-unassigned-count').textContent = `${list.length}명`;
  const container = document.getElementById('mnt-unassigned-list');
  const savedScrollTop = container.scrollTop;
  if (!list.length) {
    container.innerHTML = '<p class="text-xs text-slate-400 text-center py-10">조건에 맞는<br>미배정 직원이 없습니다.</p>';
    return;
  }
  container.innerHTML = list.map(e => renderEmpCard(e)).join('');
  container.scrollTop = savedScrollTop;
}

/* ── 오른쪽: 뷰 모드에 따라 칸반 or 트리로 분기 ── */
function renderTeams() {
  const teamsEl  = document.getElementById('mnt-teams');
  const wrapEl   = teamsEl.parentElement;

  /* 렌더 전 스크롤 위치 저장 */
  const treeWrap      = document.getElementById('org-tree-wrap');
  const savedTreeLeft = treeWrap ? treeWrap.scrollLeft : null;
  const savedTreeTop  = treeWrap ? treeWrap.scrollTop  : null;
  const savedWrapLeft = wrapEl.scrollLeft;

  const empMap = {};
  allEmps.forEach(e => { empMap[e.userId] = e; });

  const dbLeaders = allEmps.filter(e => e.mntUserId && e.mntUserId === e.userId).map(e => e.userId);
  const supIds    = [...new Set([...dbLeaders, ...extraSups])];
  supIds.sort((a, b) => ((empMap[a]?.userName)||a).localeCompare((empMap[b]?.userName)||b));

  if (mntViewMode === 'tree') {
    /* 트리 뷰: 스크롤 방향 양방향 */
    wrapEl.className  = 'flex-1 overflow-auto';
    teamsEl.className = 'h-full';
    teamsEl.removeAttribute('style');
    renderOrgTree(supIds, empMap, savedTreeLeft, savedTreeTop);
  } else {
    /* 칸반 뷰: 가로 스크롤 */
    wrapEl.className  = 'flex-1 overflow-x-auto overflow-y-hidden';
    teamsEl.className = 'flex gap-3 h-full items-start';
    teamsEl.style.minWidth = 'max-content';
    renderKanban(supIds, empMap);
    /* 칸반 스크롤 복원 */
    requestAnimationFrame(() => { wrapEl.scrollLeft = savedWrapLeft; });
  }
}

/* ── 칸반 뷰 (강사팀 / 전체) ── */
function renderKanban(supIds, empMap) {
  const teamsEl = document.getElementById('mnt-teams');

  if (!supIds.length) {
    teamsEl.innerHTML = `
      <div class="mnt-col flex-shrink-0 rounded-xl border-2 border-dashed border-slate-200 bg-slate-50
                   flex flex-col items-center justify-center cursor-pointer
                   hover:border-blue-300 hover:bg-blue-50 transition-colors"
           style="width:210px; min-height:240px"
           onclick="openAddTeamModal()"
           ondragover="event.preventDefault()"
           ondrop="onDropToNewTeam(event)">
        <i class="fa-solid fa-plus text-2xl text-slate-300 mb-2"></i>
        <p class="text-xs font-semibold text-slate-400">팀 추가</p>
        <p class="text-xs text-slate-300 mt-1 text-center px-4 leading-relaxed">사수를 선택하거나<br>카드를 여기에 놓으세요</p>
      </div>`;
    return;
  }

  teamsEl.innerHTML = supIds.map(sid => {
    const sup     = empMap[sid] || { userId: sid, userName: sid };
    const members = getTeamDescendants(sid);
    const avatar  = sup.userProfile && sup.userProfile.startsWith('http')
      ? `<img src="${escHtml(sup.userProfile)}" class="w-9 h-9 rounded-lg object-cover flex-shrink-0">`
      : `<div class="w-9 h-9 rounded-lg bg-blue-100 flex items-center justify-center text-sm font-bold text-blue-600 flex-shrink-0">${escHtml((sup.userName||'?').charAt(0))}</div>`;
    return `
      <div class="mnt-col flex-shrink-0 rounded-xl border border-slate-200 bg-white overflow-hidden"
           style="width:210px" data-sup-id="${escHtml(sid)}"
           ondragover="event.preventDefault()"
           ondrop="onDropToTeam(event,this)">
        <div class="bg-blue-50 border-b border-blue-100 px-3 py-3 flex items-center gap-2.5">
          ${avatar}
          <div class="min-w-0 flex-1">
            <p class="font-bold text-sm text-slate-800 truncate">${escHtml(sup.userName||sid)}</p>
            <p class="text-xs text-slate-400 truncate">${escHtml(sup.jbgrNm||'')}${sup.deptNm?' · '+escHtml(sup.deptNm):''}</p>
          </div>
          <span class="text-xs bg-blue-100 text-blue-600 font-bold px-1.5 py-0.5 rounded-md flex-shrink-0">${members.length}명</span>
          <button onclick="event.stopPropagation(); dissolveTeam('${escHtml(sid)}')"
                  title="팀 전체 해제"
                  class="flex-shrink-0 w-5 h-5 rounded-full bg-slate-200 hover:bg-red-100 hover:text-red-500
                         text-slate-400 text-xs flex items-center justify-center transition-colors ml-1">✕</button>
        </div>
        <div class="mnt-col-body p-2 flex flex-col gap-1.5" style="min-height:120px">
          ${members.map(e => renderEmpCard(e)).join('') ||
            '<p class="text-xs text-slate-300 text-center py-5">팀원 없음<br><span style="font-size:.7rem">카드를 드래그해서 놓으세요</span></p>'}
        </div>
      </div>`;
  }).join('') + `
    <div onclick="openAddTeamModal()"
         class="mnt-col flex-shrink-0 rounded-xl border-2 border-dashed border-slate-200 bg-slate-50
                flex flex-col items-center justify-center cursor-pointer
                hover:border-blue-300 hover:bg-blue-50 transition-colors"
         style="width:64px; height:80px; align-self:flex-start"
         ondragover="event.preventDefault()"
         ondrop="onDropToNewTeam(event)">
      <i class="fa-solid fa-plus text-lg text-slate-300"></i>
    </div>`;
}

/* ── 트리 뷰 (행정팀 / PD팀) ── */
function renderOrgTree(supIds, empMap, savedScrollLeft, savedScrollTop) {
  const teamsEl = document.getElementById('mnt-teams');

  if (!supIds.length) {
    teamsEl.innerHTML = `
      <div class="flex flex-col items-center justify-center h-full gap-3 text-slate-300">
        <div class="w-20 h-20 rounded-full border-2 border-dashed border-slate-200 bg-slate-50
                     flex flex-col items-center justify-center cursor-pointer
                     hover:border-blue-300 hover:bg-blue-50 transition-colors"
             onclick="openAddTeamModal()"
             ondragover="event.preventDefault()"
             ondrop="onDropToNewTeam(event)">
          <i class="fa-solid fa-plus text-xl text-slate-300"></i>
        </div>
        <p class="text-xs text-slate-400">팀장을 드래그하거나 클릭하여 추가</p>
      </div>`;
    return;
  }

  teamsEl.innerHTML = `
    <div class="org-tree-wrap" id="org-tree-wrap">
      ${supIds.map(sid => `
        <div class="org-tree">
          <ul>${buildTreeHtml(sid, empMap)}</ul>
        </div>
      `).join('')}
    </div>`;

  /* 스크롤 복원: 이전 위치가 있으면 복원, 최초 로드면 중앙 정렬 */
  requestAnimationFrame(() => {
    const wrap = document.getElementById('org-tree-wrap');
    if (!wrap) return;
    if (savedScrollLeft !== null && savedScrollLeft !== undefined) {
      wrap.scrollLeft = savedScrollLeft;
      wrap.scrollTop  = savedScrollTop ?? 0;
    } else {
      wrap.scrollLeft = (wrap.scrollWidth - wrap.clientWidth) / 2;
    }
  });
}

/* 재귀 트리 노드 HTML 생성 */
function buildTreeHtml(userId, empMap) {
  const emp = empMap[userId];
  if (!emp) return '';
  const isRoot   = emp.mntUserId === emp.userId;
  const children = allEmps.filter(e => e.mntUserId === userId && e.userId !== userId);

  const avatar = emp.userProfile && emp.userProfile.startsWith('http')
    ? `<img src="${escHtml(emp.userProfile)}" class="w-8 h-8 rounded-full object-cover mx-auto mb-1">`
    : `<div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-sm font-bold text-blue-600 mx-auto mb-1">${escHtml((emp.userName||'?').charAt(0))}</div>`;

  const dissolveBtn = isRoot
    ? `<button onclick="event.stopPropagation(); dissolveTeam('${escHtml(userId)}')"
               title="팀 전체 해제"
               class="absolute -top-1.5 -right-1.5 w-4 h-4 rounded-full bg-slate-200 hover:bg-red-100
                      hover:text-red-500 text-slate-400 text-xs flex items-center justify-center
                      transition-colors leading-none">✕</button>`
    : '';

  const nodeHtml = `
    <div class="tree-node ${isRoot ? 'tree-node-root' : ''} relative"
         data-uid="${escHtml(userId)}"
         data-mnt="${escHtml(emp.mntUserId || '')}"
         ${!isRoot ? 'draggable="true"' : ''}
         style="${!isRoot ? 'cursor:grab;' : ''}"
         ondragover="event.preventDefault()"
         ondrop="onDropToTreeNode(event, this)">
      ${dissolveBtn}
      ${avatar}
      <p class="text-xs font-bold text-slate-800 truncate" style="max-width:84px">${escHtml(emp.userName||userId)}</p>
      <p class="text-xs text-slate-400 truncate" style="max-width:84px">${escHtml(emp.jbgrNm||'')}</p>
    </div>`;

  if (!children.length) return `<li>${nodeHtml}</li>`;

  return `<li>
    ${nodeHtml}
    <ul>${children.map(c => buildTreeHtml(c.userId, empMap)).join('')}</ul>
  </li>`;
}

/* 트리 노드에 드롭 → 해당 노드가 직접 사수 */
function onDropToTreeNode(event, nodeEl) {
  event.preventDefault();
  event.stopPropagation();
  nodeEl.classList.remove('drag-over');
  if (!dragUserId) return;
  const targetId = nodeEl.dataset.uid;
  if (!targetId || dragUserId === targetId || dragCurMnt === targetId) return;
  /* 자신의 하위 직원에게 드롭 시 순환 참조 방지 */
  const descendants = getTeamDescendants(dragUserId);
  if (descendants.some(e => e.userId === targetId)) return;
  saveMntMapping(dragUserId, targetId);
}

/* ── 공통 직원 카드 ── */
function renderEmpCard(emp) {
  const avatar = emp.userProfile && emp.userProfile.startsWith('http')
    ? `<img src="${escHtml(emp.userProfile)}" class="w-7 h-7 rounded-md object-cover flex-shrink-0">`
    : `<div class="w-7 h-7 rounded-md bg-blue-100 flex items-center justify-center text-xs font-bold text-blue-600 flex-shrink-0">${escHtml((emp.userName||'?').charAt(0))}</div>`;
  const statCls = emp.emplStatCd==='01' ? 'text-emerald-600' : emp.emplStatCd==='02' ? 'text-amber-500' : 'text-red-400';
  const statTxt = emp.emplStatCd==='01' ? '재직' : emp.emplStatCd==='02' ? '휴직' : '퇴사';
  return `
    <div class="emp-card bg-white border border-slate-100 hover:border-blue-200 hover:bg-blue-50
                rounded-lg p-2 flex items-center gap-2 cursor-grab select-none transition-colors shadow-sm"
         draggable="true" data-uid="${escHtml(emp.userId)}" data-mnt="${escHtml(emp.mntUserId||'')}">
      ${avatar}
      <div class="min-w-0 flex-1">
        <p class="text-xs font-semibold text-slate-800 truncate">${escHtml(emp.userName||'-')}</p>
        <p class="text-xs text-slate-400 truncate">${escHtml(emp.jbgrNm||'-')}</p>
      </div>
      <span class="text-xs ${statCls} flex-shrink-0">${statTxt}</span>
    </div>`;
}

/* ── 팀 추가 모달 ── */
function openAddTeamModal() {
  const existingSups = new Set([
    ...allEmps.filter(e => e.mntUserId).map(e => e.mntUserId),
    ...extraSups
  ]);
  const candidates = allEmps.filter(e => !existingSups.has(e.userId));
  if (!candidates.length) { alert('추가 가능한 사수 후보가 없습니다.'); return; }
  const sel = document.getElementById('add-team-select');
  sel.innerHTML = candidates
    .map(e => `<option value="${escHtml(e.userId)}">${escHtml(e.userName)} (${escHtml(e.jbgrNm || e.userId)})</option>`)
    .join('');
  if (sel.customSelect) sel.customSelect.refresh();
  document.getElementById('modal-add-team').classList.remove('hidden');
}
function closeAddTeamModal() { document.getElementById('modal-add-team').classList.add('hidden'); }
function confirmAddTeam() {
  const sel = document.getElementById('add-team-select');
  const sid = sel.customSelect ? sel.customSelect.getValue() : sel.value;
  if (!sid) return;
  closeAddTeamModal();
  if (!extraSups.includes(sid)) extraSups.push(sid);
  renderTeams();
}

/* ── 팀 계층 헬퍼 ── */
/* 팀 루트(leaderId)의 모든 하위 직원을 BFS로 수집 */
function getTeamDescendants(leaderId) {
  const result  = [];
  const queue   = [leaderId];
  const visited = new Set();
  while (queue.length) {
    const pid = queue.shift();
    if (visited.has(pid)) continue;
    visited.add(pid);
    allEmps.forEach(e => {
      if (e.mntUserId === pid && e.userId !== pid) {
        result.push(e);
        queue.push(e.userId);
      }
    });
  }
  return result;
}

/* 드래그한 직원의 직접 사수 자동 계산 — 팀 내 직급 rank 바로 1단계 위 */
function findDirectSup(draggedId, teamHeaderId) {
  const dragged = allEmps.find(e => e.userId === draggedId);
  if (!dragged) return teamHeaderId;
  const draggedOrd = gradeOrdMap[dragged.jbgrNm] ?? 999;

  const header  = allEmps.find(e => e.userId === teamHeaderId);
  const teamAll = [header, ...getTeamDescendants(teamHeaderId)]
                    .filter(e => e && e.userId !== draggedId);

  /* 나보다 높은 직급(sortOrd 작은 값) 중 가장 가까운 사람 */
  const higher  = teamAll.filter(e => (gradeOrdMap[e.jbgrNm] ?? 999) < draggedOrd);
  if (!higher.length) return teamHeaderId;
  higher.sort((a, b) => (gradeOrdMap[b.jbgrNm] ?? 999) - (gradeOrdMap[a.jbgrNm] ?? 999));
  return higher[0].userId;
}

/* ── 팀 전체 해제 (X 버튼) ── */
function dissolveTeam(supId) {
  if (!confirm('이 팀을 전체 해제하시겠습니까?\n모든 팀원이 미배정 상태로 돌아갑니다.')) return;
  const members = getTeamDescendants(supId);
  const targets = [supId, ...members.map(e => e.userId)];
  extraSups = extraSups.filter(id => id !== supId);
  targets.forEach(uid => applyMntChange(uid, null));
  renderUnassigned();
  renderTeams();
  updatePendingBar();
}

/* ── "+" 영역 드롭 → 드래그한 직원을 팀장으로 등록 (mntUserId = 자기 자신) ── */
function onDropToNewTeam(event) {
  event.preventDefault();
  event.stopPropagation();
  if (!dragUserId) return;
  const allLeaderIds = new Set([
    ...allEmps.filter(e => e.mntUserId === e.userId).map(e => e.userId),
    ...extraSups
  ]);
  if (allLeaderIds.has(dragUserId)) return;
  extraSups.push(dragUserId);
  renderTeams();
  renderUnassigned();
  saveMntMapping(dragUserId, dragUserId); /* 자기 자신을 사수로 = 팀장 표시 */
}

/* ── 드롭 핸들러 ── */
function onDropToTeam(event, colEl) {
  event.preventDefault();
  event.stopPropagation();
  colEl.classList.remove('drag-over');
  if (!dragUserId) return;
  const teamHeaderId = colEl.dataset.supId;
  if (!teamHeaderId || dragUserId === teamHeaderId) return;
  /* 직급 rank 기준으로 바로 1단계 위 사수를 자동 계산 */
  const directSupId = findDirectSup(dragUserId, teamHeaderId);
  if (dragCurMnt === directSupId) return; /* 이미 같은 사수면 무시 */
  saveMntMapping(dragUserId, directSupId);
}

function onDropToUnassigned(event) {
  event.preventDefault();
  if (!dragUserId || !dragCurMnt) return; /* 이미 미배정 */

  const descendants = getTeamDescendants(dragUserId);
  const targets = [dragUserId, ...descendants.map(e => e.userId)];
  targets.forEach(uid => applyMntChange(uid, null));
  dragUserId = null;
  renderUnassigned();
  renderTeams();
  updatePendingBar();
}

function saveMntMapping(userId, newMntUserId) {
  applyMntChange(userId, newMntUserId);
  if (newMntUserId === userId) extraSups = extraSups.filter(id => id !== userId);
  dragUserId = null;
  renderUnassigned();
  renderTeams();
  updatePendingBar();
}

function applyMntChange(userId, newMntUserId) {
  const emp = allEmps.find(e => e.userId === userId);
  if (emp) emp.mntUserId = newMntUserId;
  pendingChanges.set(userId, newMntUserId);
}

function updatePendingBar() {
  const bar = document.getElementById('mnt-save-bar');
  const ct  = document.getElementById('mnt-pending-count');
  if (!bar) return;
  if (pendingChanges.size > 0) {
    bar.classList.remove('hidden');
    if (ct) ct.textContent = pendingChanges.size;
  } else {
    bar.classList.add('hidden');
  }
}

async function flushPendingChanges() {
  if (!pendingChanges.size) return;
  const btn = document.getElementById('mnt-flush-btn');
  if (btn) btn.disabled = true;
  const payload = [...pendingChanges.entries()].map(([userId, mntUserId]) => ({ userId, mntUserId: mntUserId ?? '' }));
  try {
    const r = await fetch('/admin/org/mapping/batch', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify(payload)
    });
    if (!r.ok) throw new Error(`서버 오류 ${r.status}`);
    const d = await r.json();
    if (d.result === 'success') {
      pendingChanges.clear();
      updatePendingBar();
      showHermesToast('결재에 등록되었습니다.');
    } else {
      showHermesToast('등록 실패: ' + (d.message || '알 수 없는 오류'), 'error');
    }
  } catch (err) {
    showHermesToast('저장 중 오류가 발생했습니다: ' + err.message, 'error');
  } finally {
    if (btn) btn.disabled = false;
  }
}

function cancelPendingChanges() {
  pendingChanges.clear();
  mntKanbanLoaded = false;
  loadMntKanban();
  updatePendingBar();
}

/* ── DnD 이벤트 위임 ── */
document.addEventListener('dragstart', e => {
  const card = e.target.closest('.emp-card') || e.target.closest('.tree-node[draggable]');
  if (!card) return;
  dragUserId = card.dataset.uid;
  dragCurMnt = card.dataset.mnt;
  e.dataTransfer.effectAllowed = 'move';
  e.dataTransfer.setData('text/plain', dragUserId); /* Firefox drop 이벤트 발화 필수 */
  requestAnimationFrame(() => { card.style.opacity = '0.45'; });
});
document.addEventListener('dragend', e => {
  const card = e.target.closest('.emp-card') || e.target.closest('.tree-node[draggable]');
  if (card) card.style.opacity = '';
  dragUserId = null;
  document.querySelectorAll('.mnt-col.drag-over, .tree-node.drag-over, #mnt-unassigned-list.drag-over')
    .forEach(el => el.classList.remove('drag-over'));
});
document.addEventListener('dragenter', e => {
  if (!dragUserId) return;
  const col  = e.target.closest('.mnt-col');
  if (col) col.classList.add('drag-over');
  const node = e.target.closest('.tree-node');
  if (node) node.classList.add('drag-over');
  const ua = e.target.closest('#mnt-unassigned-list');
  if (ua && dragCurMnt) ua.classList.add('drag-over');
});
document.addEventListener('dragleave', e => {
  const col = e.target.closest('.mnt-col');
  if (col && !col.contains(e.relatedTarget)) col.classList.remove('drag-over');
  const node = e.target.closest('.tree-node');
  if (node && !node.contains(e.relatedTarget)) node.classList.remove('drag-over');
  const ua = e.target.closest('#mnt-unassigned-list');
  if (ua && !ua.contains(e.relatedTarget)) ua.classList.remove('drag-over');
});
