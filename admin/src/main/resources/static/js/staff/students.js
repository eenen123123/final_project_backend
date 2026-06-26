var selectedEmpId = null;
var detailEditMode = false;

/* ─── 탭 전환 ─── */
function switchEmpTab(tabId, btn) {
  document.querySelectorAll(".emp-tab-btn").forEach((b) => b.classList.remove("active"));
  document.querySelectorAll(".emp-tab-panel").forEach((p) => p.classList.remove("active"));
  btn.classList.add("active");
  document.getElementById(tabId).classList.add("active");
}

/* ─── 페이지 깜빡임 없는 탭 이동 ─── */
async function navigateToEmployees(btn) {
  btn.disabled = true;
  try {
    const res = await fetch('/admin/employees');
    if (!res.ok) { location.href = '/admin/employees'; return; }

    const html = await res.text();
    const doc  = new DOMParser().parseFromString(html, 'text/html');
    const newMain  = doc.querySelector('main');
    const currMain = document.querySelector('main');
    if (!newMain || !currMain) { location.href = '/admin/employees'; return; }

    // 신규 CSS 로드
    doc.querySelectorAll('link[rel="stylesheet"]').forEach(link => {
      const href = link.getAttribute('href');
      if (href && !document.querySelector(`link[href="${href}"]`)) {
        const el = document.createElement('link');
        el.rel = 'stylesheet'; el.href = href;
        document.head.appendChild(el);
      }
    });

    currMain.innerHTML = newMain.innerHTML;

    // 기존 학생 스크립트 제거 후 직원 스크립트 로드
    document.querySelectorAll('script[src*="/js/staff/"]').forEach(s => s.remove());
    for (const s of doc.querySelectorAll('script[src*="/js/staff/"]')) {
      await new Promise(resolve => {
        const el = document.createElement('script');
        el.src = s.getAttribute('src');
        el.onload = el.onerror = resolve;
        document.head.appendChild(el);
      });
    }

    currMain.querySelectorAll('select.hm-input:not([data-cs-defer])').forEach(el => {
      if (!el.customSelect && window.initCustomSelect) window.initCustomSelect(el);
    });
    await initDeferredSelects(currMain);

    history.pushState({ url: '/admin/employees' }, doc.title || '', '/admin/employees');
    if (doc.title) document.title = doc.title;

  } catch {
    location.href = '/admin/employees';
  }
}

/* ─── 페이징 + 필터 + 정렬 ─── */
var currentHrPage = 1;
var HR_SCREEN_SIZE = 7;
var HR_BLOCK_SIZE = 5;
var hrSortCol = null;
var hrSortAsc = true;
var hrFilteredRows = null;

var filterDebounceTimer = null;

function syncClassFilter() {
  const type = document.getElementById("hr-type-filter").value;
  const wrap = document.getElementById("hr-class-filter-wrap");
  const classEl = document.getElementById("hr-class-filter");
  const isOffline = type === "오프라인";
  if (wrap) wrap.style.display = isOffline ? "" : "none";
  if (!isOffline && classEl) classEl.value = "";
}

function filterHrList() {
  syncClassFilter();
  clearTimeout(filterDebounceTimer);
  filterDebounceTimer = setTimeout(() => doFilterHrList(1), 300);
}

async function fetchStudentStats() {
  try {
    const res = await fetch("/admin/employees/students/stats");
    if (!res.ok) {
      console.error("학생 통계 조회 실패 (HTTP " + res.status + "):", await res.text());
      return;
    }
    const data = await res.json();
    const toNum = v => (v == null ? 0 : Number(v));
    const el = id => document.getElementById(id);
    if (el("stat-total"))          el("stat-total").textContent          = toNum(data.total);
    if (el("stat-role-user"))      el("stat-role-user").textContent      = toNum(data.roleUser);
    if (el("stat-role-student"))   el("stat-role-student").textContent   = toNum(data.roleStudent);
    if (el("stat-unregistered"))   el("stat-unregistered").textContent   = toNum(data.unregistered);
    if (el("stat-no-parent"))      el("stat-no-parent").textContent      = toNum(data.noParent);
  } catch (e) {
    console.error("학생 통계 조회 실패:", e);
  }
}
fetchStudentStats();

async function doFilterHrList(page) {
  page = page || 1;
  currentHrPage = page;
  const keyword = document.getElementById("hr-search").value.trim();
  const year    = document.getElementById("hr-year").value;
  const type    = document.getElementById("hr-type-filter").value;
  const status  = document.getElementById("hr-status-filter").value;

  const classFilter  = type === "오프라인" ? document.getElementById("hr-class-filter").value : "";
  const parentFilter = document.getElementById("hr-parent-filter").value;

  const params = new URLSearchParams();
  if (keyword) params.set("keyword",  keyword);
  if (year)    params.set("year",     year);
  if (classFilter) {
    params.set("classStatus", classFilter);
  } else {
    const typeToRole = { '일반': 'ROLE_USER', '오프라인': 'ROLE_STUDENT' };
    if (type) params.set("userRole", typeToRole[type] || type);
  }
  if (status)       params.set("enable",       status);
  if (parentFilter) params.set("parentStatus", parentFilter);
  if (hrSortCol) {
    params.set("orderBy",        hrSortCol);
    params.set("orderDirection", hrSortAsc ? "ASC" : "DESC");
  }
  params.set("page",       page);
  params.set("screenSize", HR_SCREEN_SIZE);

  try {
    const res  = await fetch("/admin/employees/students/search?" + params);
    const data = await res.json();
    renderStudentTable(data.items, data.totalCount);
  } catch (e) {
    console.error("학생 검색 실패:", e);
  }
}

function renderStudentTable(students, totalCount) {
  const tbody = document.getElementById("hr-table-body");
  if (!students || students.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-sm text-slate-400">등록된 학생이 없습니다.</td></tr>';
    renderHrPagination(0);
    return;
  }

  tbody.innerHTML = students.map(stu => {
    const pro  = stu.userProfile || "";
    const nm   = stu.userName || "";
    const uid  = stu.userId || "";
    const jn   = stu.joinDt ? String(stu.joinDt).substring(0, 10) : "";
    const en   = stu.enable || "";
    const role = stu.userRole || "";
    const addr = (stu.userAddr || "") + (stu.userDaddr ? " " + stu.userDaddr : "");

    const typeNm  = { "ROLE_USER":"일반","ROLE_STUDENT":"오프라인" }[role] || "-";
    const statNm  = en === "Y" ? "정상" : "탈퇴";
    const statCls = en === "Y" ? "status-active" : "status-resigned";
    const avatar  = (pro && pro.startsWith("http"))
      ? `<img src="${escHtml(pro)}" class="w-7 h-7 rounded-lg object-cover" alt="프로필">`
      : `<div class="w-7 h-7 rounded-lg bg-blue-100 flex items-center justify-center text-xs font-bold text-[#3b82f6]">${escHtml(nm.charAt(0))}</div>`;

    // 강사·강좌 표시
    let classTd = '-';
    if (role === 'ROLE_STUDENT') {
      const cnt = stu.classCnt || 0;
      if (cnt === 0) {
        classTd = '<span class="status-badge status-resigned">미등록</span>';
      } else {
        const instrNm  = escHtml(stu.firstInstrNm || "");
        const classNm  = escHtml(stu.firstClassNm || "");
        const instrStr = cnt > 1 ? `${instrNm} 외 ${cnt - 1}` : instrNm;
        const classStr = cnt > 1 ? `${classNm} 외 ${cnt - 1}` : classNm;
        classTd = `<span class="text-slate-700 font-medium">${instrStr}</span><br><span class="text-xs text-slate-400">${classStr}</span>`;
      }
    }

    // 관리 버튼 그룹
    const detailBtn = `<button type="button" data-id="${escHtml(uid)}"
      onclick="openDetail(this.getAttribute('data-id'))"
      class="text-xs text-[#3b82f6] hover:underline font-semibold whitespace-nowrap">상세</button>`;

    const classBtn = role === 'ROLE_STUDENT'
      ? `<button type="button" data-id="${escHtml(uid)}" data-name="${escHtml(nm)}"
           onclick="openClassRegister(this.dataset.id, this.dataset.name)"
           class="text-xs text-blue-600 hover:underline font-semibold whitespace-nowrap">클래스</button>`
      : '';

    const parentLinked = stu.prntUserId;
    const linkBtn = role === 'ROLE_STUDENT'
      ? (parentLinked
          ? `<span class="inline-flex items-center gap-1 text-xs font-semibold text-emerald-600 bg-emerald-50 rounded-lg px-2 py-0.5 whitespace-nowrap"><i class="fa-solid fa-link text-[9px]"></i>연동됨</span>`
          : `<button type="button" data-id="${escHtml(uid)}" data-name="${escHtml(nm)}"
               onclick="openParentLinkModal(this.dataset.id, this.dataset.name)"
               class="inline-flex items-center gap-1 text-xs font-semibold text-white bg-blue-400 hover:bg-blue-500 rounded-lg px-2 py-0.5 whitespace-nowrap transition-colors"><i class="fa-solid fa-paper-plane text-[9px]"></i>연동</button>`)
      : '';

    const actionBtns = [detailBtn, classBtn, linkBtn].filter(Boolean).join(
      '<span class="text-slate-200 mx-0.5">|</span>'
    );

    return `<tr class="hr-data-row hover:bg-slate-50 transition-colors"
      data-id="${escHtml(uid)}" data-name="${escHtml(nm)}"
      data-email="${escHtml(stu.userEmailAddr||"")}" data-phone="${escHtml(stu.userTelno||"")}"
      data-birthdate="${escHtml(stu.userBrdt||"")}" data-gender="${escHtml(stu.userGndrCd||"")}"
      data-zip="${escHtml(stu.userZip||"")}" data-addr-base="${escHtml(stu.userAddr||"")}"
      data-addr-detail="${escHtml(stu.userDaddr||"")}" data-addr="${escHtml(addr)}"
      data-profile="${escHtml(pro)}" data-type="${escHtml(role)}"
      data-join="${escHtml(jn)}" data-enable="${escHtml(en)}"
      data-parent-id="${escHtml(stu.prntUserId||"")}" data-parent-name="${escHtml(stu.prntUserName||"")}"
      data-parent-phone="${escHtml(stu.prntTelno||"")}" data-parent-email="${escHtml(stu.prntEmailAddr||"")}">
      <td class="py-3 px-4">
        <div class="flex items-center gap-2">
          ${avatar}
          <div>
            <p class="font-bold text-slate-800">${escHtml(nm)}</p>
            <p class="text-xs text-slate-400">${escHtml(uid)}</p>
          </div>
        </div>
      </td>
      <td class="py-3 px-4 text-sm text-slate-600 truncate">${typeNm}</td>
      <td class="py-3 px-4 text-sm leading-snug">${classTd}</td>
      <td class="py-3 px-4 text-sm text-slate-600 whitespace-nowrap">${jn || "-"}</td>
      <td class="py-3 px-4"><span class="status-badge ${statCls}">${statNm}</span></td>
      <td class="py-3 px-4">
        <div class="flex items-center gap-1 flex-wrap">${actionBtns}</div>
      </td>
    </tr>`;
  }).join("");

  renderHrPagination(totalCount);
}

/* ─── 클래스 등록 모달 ─── */
var classRegisterUserId  = null;
var selectedClassSn      = null;
var selectedClassNm      = null;
var availableClassesData = [];

var CLASS_STAT_LABEL = { ACTIVE: "운영중", RECRUITING: "모집중", CLOSED: "종료", WAITING: "대기" };
var CLASS_STAT_CLS   = {
  ACTIVE:     "text-emerald-600 bg-emerald-50",
  RECRUITING: "text-[#3b82f6] bg-blue-50",
  CLOSED:     "text-slate-500 bg-slate-100",
  WAITING:    "text-amber-600 bg-amber-50",
};

function openClassRegister(userId, userName) {
  classRegisterUserId  = userId;
  selectedClassSn      = null;
  availableClassesData = [];

  document.getElementById("class-register-student-name").textContent = userName;
  document.getElementById("class-register-submit-btn").disabled = true;

  // 좌측 초기화
  document.getElementById("enrolled-list-loading").classList.remove("hidden");
  document.getElementById("enrolled-list-empty").classList.add("hidden");
  document.getElementById("enrolled-list-body").classList.add("hidden");
  document.getElementById("enrolled-list-body").innerHTML = "";

  // 우측 초기화
  document.getElementById("instr-list-loading").classList.remove("hidden");
  document.getElementById("instr-list-empty").classList.add("hidden");
  document.getElementById("instr-list-body").classList.add("hidden");
  document.getElementById("instr-list-body").innerHTML = "";
  showInstrPanel();

  openModal("modal-class-register");

  Promise.all([
    fetch(`/admin/employees/students/${encodeURIComponent(userId)}/enrolled-classes`).then(r => r.json()),
    fetch(`/admin/employees/students/${encodeURIComponent(userId)}/available-classes`).then(r => r.json()),
  ]).then(([enrolled, available]) => {
    renderEnrolledList(enrolled);
    availableClassesData = available || [];
    renderInstrList(availableClassesData);
  }).catch(() => {
    showHermesToast("클래스 정보를 불러오는 데 실패했습니다.", "error");
  });
}

function renderEnrolledList(list) {
  const loading = document.getElementById("enrolled-list-loading");
  const empty   = document.getElementById("enrolled-list-empty");
  const body    = document.getElementById("enrolled-list-body");
  loading.classList.add("hidden");
  if (!list || list.length === 0) { empty.classList.remove("hidden"); return; }
  body.innerHTML = list.map(cl => `
    <div class="p-2.5 rounded-lg border border-slate-200 bg-white">
      <div class="flex items-start justify-between gap-1 mb-0.5">
        <p class="text-xs font-semibold text-slate-700 leading-snug">${escHtml(cl.classNm)}</p>
        <span class="text-[10px] font-medium px-1.5 py-0.5 rounded-full flex-shrink-0 ${CLASS_STAT_CLS[cl.classStatCd] || 'text-slate-500 bg-slate-100'}">${CLASS_STAT_LABEL[cl.classStatCd] || cl.classStatCd}</span>
      </div>
      <p class="text-[11px] text-slate-400 truncate">${escHtml(cl.courseNm)}</p>
      <p class="text-[11px] text-slate-400 truncate">${escHtml(cl.instrNm)}</p>
    </div>`).join("");
  body.classList.remove("hidden");
}

function renderInstrList(classes) {
  const loading = document.getElementById("instr-list-loading");
  const empty   = document.getElementById("instr-list-empty");
  const body    = document.getElementById("instr-list-body");
  loading.classList.add("hidden");
  if (!classes || classes.length === 0) { empty.classList.remove("hidden"); return; }

  const instrMap = {};
  classes.forEach(cl => {
    if (!instrMap[cl.instrUserId]) instrMap[cl.instrUserId] = { instrNm: cl.instrNm, cnt: 0 };
    instrMap[cl.instrUserId].cnt++;
  });
  const instructors = Object.entries(instrMap)
    .map(([id, v]) => ({ instrUserId: id, instrNm: v.instrNm, cnt: v.cnt }))
    .sort((a, b) => a.instrNm.localeCompare(b.instrNm, "ko"));

  body.innerHTML = instructors.map(instr => `
    <button type="button"
            onclick="showClassPanel('${escHtml(instr.instrUserId)}', '${escHtml(instr.instrNm)}')"
            class="w-full flex items-center justify-between px-3 py-2.5 rounded-xl border border-slate-200 bg-white hover:border-[#3b82f6] hover:bg-blue-50 transition-colors text-left">
      <div class="flex items-center gap-2.5 min-w-0">
        <div class="w-8 h-8 rounded-lg bg-blue-100 flex items-center justify-center text-xs font-bold text-[#3b82f6] flex-shrink-0">
          ${escHtml(instr.instrNm.charAt(0))}
        </div>
        <span class="text-sm font-semibold text-slate-700 truncate">${escHtml(instr.instrNm)}</span>
      </div>
      <div class="flex items-center gap-1.5 flex-shrink-0 ml-2">
        <span class="text-xs text-slate-400">${instr.cnt}개 강좌</span>
        <i class="fa-solid fa-chevron-right text-xs text-slate-300"></i>
      </div>
    </button>`).join("");
  body.classList.remove("hidden");
}

function showInstrPanel() {
  document.getElementById("instr-panel").style.display = "";
  document.getElementById("class-panel").style.display = "none";
  selectedClassSn = null;
  document.getElementById("class-register-submit-btn").disabled = true;
}

function showClassPanel(instrUserId, instrNm) {
  document.getElementById("instr-panel").style.display = "none";
  document.getElementById("class-panel").style.display = "flex";
  document.getElementById("selected-instr-name").textContent = instrNm;

  const classes = availableClassesData.filter(cl => cl.instrUserId === instrUserId);
  const body    = document.getElementById("class-list-body2");
  const empty   = document.getElementById("class-list-empty2");

  if (!classes || classes.length === 0) {
    empty.classList.remove("hidden");
    body.innerHTML = "";
    return;
  }
  empty.classList.add("hidden");
  body.innerHTML = classes.map(cl => `
    <label class="flex items-center gap-3 p-3 rounded-xl border border-slate-200 bg-white cursor-pointer hover:border-[#3b82f6] has-[:checked]:border-[#3b82f6] has-[:checked]:bg-blue-50 transition-colors">
      <input type="radio" name="class-radio" value="${cl.classSn}" onchange="onClassRadioChange(${cl.classSn})" class="accent-[#3b82f6] w-4 h-4 flex-shrink-0">
      <div class="flex-1 min-w-0">
        <p class="font-semibold text-slate-800 text-sm truncate">${escHtml(cl.classNm)}</p>
        <p class="text-xs text-slate-400 truncate">${escHtml(cl.courseNm)}</p>
      </div>
      <div class="flex flex-col items-end gap-1 flex-shrink-0">
        <span class="text-[11px] font-medium px-2 py-0.5 rounded-full ${CLASS_STAT_CLS[cl.classStatCd] || 'text-slate-500 bg-slate-100'}">${CLASS_STAT_LABEL[cl.classStatCd] || cl.classStatCd}</span>
        <span class="text-[11px] text-slate-400">수강 ${cl.enrolledCnt}명</span>
      </div>
    </label>`).join("");
}

function onClassRadioChange(classSn) {
  selectedClassSn = classSn;
  const cl = availableClassesData.find(c => String(c.classSn) === String(classSn));
  selectedClassNm = cl ? cl.classNm : String(classSn);
  document.getElementById("class-register-submit-btn").disabled = false;
}

function submitClassRegister() {
  if (!classRegisterUserId || !selectedClassSn) return;
  const btn = document.getElementById("class-register-submit-btn");
  btn.disabled = true;

  fetch(`/admin/employees/students/${encodeURIComponent(classRegisterUserId)}/class`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ classSn: selectedClassSn, classNm: selectedClassNm }),
  })
    .then(res => res.json())
    .then(data => {
      if (data.result === "success") {
        closeModal("modal-class-register");
        showHermesToast("결재 요청이 완료되었습니다. 승인 후 반영됩니다.", "info");
      } else {
        showHermesToast("등록 실패: " + (data.message || "서버 오류"), "error");
        btn.disabled = false;
      }
    })
    .catch(() => {
      showHermesToast("클래스 등록 요청 중 오류가 발생했습니다.", "error");
      btn.disabled = false;
    });
}

/* ─── 학부모 연동 모달 ─── */
function openParentLinkModal(studentId, studentName) {
  document.getElementById("parent-link-student-id").value = studentId;
  document.getElementById("parent-link-student-name").textContent = studentName;
  document.getElementById("parent-link-phone").value = "";
  const modal = document.getElementById("modal-parent-link");
  modal.classList.remove("hidden");
  modal.classList.add("flex");
  setTimeout(() => document.getElementById("parent-link-phone").focus(), 100);
}

function closeParentLinkModal() {
  const modal = document.getElementById("modal-parent-link");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
}

function formatParentLinkPhone(input) {
  let v = input.value.replace(/\D/g, "");
  if (v.length <= 3)       input.value = v;
  else if (v.length <= 7)  input.value = v.slice(0,3) + "-" + v.slice(3);
  else                     input.value = v.slice(0,3) + "-" + v.slice(3,7) + "-" + v.slice(7,11);
}

function sendParentLink() {
  const studentId  = document.getElementById("parent-link-student-id").value;
  const parentPhone = document.getElementById("parent-link-phone").value.trim();
  if (!parentPhone) {
    showHermesToast("학부모 전화번호를 입력해 주세요.", "error");
    return;
  }
  const params = new URLSearchParams({ studentId, parentPhone });
  fetch("/admin/parent/join-message/send", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: params.toString(),
  })
    .then(res => {
      closeParentLinkModal();
      if (res.ok) {
        showHermesToast("가입 링크가 발송되었습니다.", "success");
      } else {
        showHermesToast("발송 실패: " + (res.statusText || "알 수 없는 오류"), "error");
      }
    })
    .catch(() => showHermesToast("서버 오류가 발생했습니다.", "error"));
}

function filterByNoParent() {
  document.getElementById("hr-search").value = "";
  ["hr-year", "hr-type-filter", "hr-status-filter", "hr-class-filter"].forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    if (el.customSelect) el.customSelect.setValue("");
    else el.value = "";
  });
  document.getElementById("hr-parent-filter").value = "02";
  syncClassFilter();
  doFilterHrList(1);
}

function resetHrFilter() {
  document.getElementById("hr-search").value = "";
  ["hr-year", "hr-type-filter", "hr-status-filter", "hr-class-filter", "hr-parent-filter"].forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    if (el.customSelect) el.customSelect.setValue("");
    else el.value = "";
  });
  syncClassFilter();
  hrSortCol = null;
  hrSortAsc = true;
  updateHrSortIcons(null);
  doFilterHrList(1);
}

function sortHrBy(col) {
  if (hrSortCol === col) hrSortAsc = !hrSortAsc;
  else { hrSortCol = col; hrSortAsc = true; }
  updateHrSortIcons(col);
  doFilterHrList(1);
}

function applyHrSort(rows) {
  rows.sort((a, b) => {
    const va = a.dataset[hrSortCol] || "";
    const vb = b.dataset[hrSortCol] || "";
    if (hrSortCol === "join") {
      if (!va && !vb) return 0;
      if (!va) return 1;
      if (!vb) return -1;
      return hrSortAsc ? va.localeCompare(vb) : vb.localeCompare(va);
    }
    return hrSortAsc ? va.localeCompare(vb, "ko") : vb.localeCompare(va, "ko");
  });
}

function updateHrSortIcons(activeCol) {
  document.querySelectorAll("#hr-thead th[data-col]").forEach((th) => {
    const icon = th.querySelector(".sort-icon");
    if (!icon) return;
    if (th.dataset.col === activeCol) {
      icon.textContent = hrSortAsc ? "↑" : "↓";
      icon.classList.replace("text-slate-300", "text-[#3b82f6]");
    } else {
      icon.textContent = "↕";
      icon.classList.replace("text-[#3b82f6]", "text-slate-300");
    }
  });
}

function applyHrPaging() {
  const tbody = document.getElementById("hr-table-body");
  const allRows = Array.from(tbody.querySelectorAll(".hr-data-row"));
  const rows = hrFilteredRows !== null ? hrFilteredRows : allRows;

  rows.forEach((r) => tbody.appendChild(r));
  allRows.filter((r) => !rows.includes(r)).forEach((r) => tbody.appendChild(r));

  allRows.forEach((r) => (r.style.display = "none"));
  const offset = (currentHrPage - 1) * HR_SCREEN_SIZE;
  rows.slice(offset, offset + HR_SCREEN_SIZE).forEach((r) => (r.style.display = ""));

  renderHrPagination(rows.length);
}

function renderHrPagination(totalCount) {
  const container = document.getElementById("hr-pagination");
  const info      = document.getElementById("hr-pagination-info");
  if (!container) return;

  const totalPage = Math.ceil(totalCount / HR_SCREEN_SIZE);
  const start = totalCount === 0 ? 0 : (currentHrPage - 1) * HR_SCREEN_SIZE + 1;
  const end   = Math.min(currentHrPage * HR_SCREEN_SIZE, totalCount);
  if (info) {
    info.textContent = totalCount === 0
      ? '데이터가 없습니다.'
      : `전체 ${totalCount}명 중 ${start} – ${end} 표시`;
  }

  if (totalPage <= 1) { container.innerHTML = ""; return; }

  const endPage   = Math.min(Math.ceil(currentHrPage / HR_BLOCK_SIZE) * HR_BLOCK_SIZE, totalPage);
  const startPage = Math.max(endPage - HR_BLOCK_SIZE + 1, 1);

  const BASE = "w-8 h-8 rounded-lg text-xs flex items-center justify-center";
  let html = "";
  if (startPage > 1)
    html += `<button onclick="goHrPage(${startPage - 1})" class="${BASE} text-slate-400 hover:bg-slate-100">‹</button>`;
  for (let p = startPage; p <= endPage; p++) {
    const cls = p === currentHrPage ? "bg-blue-500 text-white font-bold" : "text-slate-400 hover:bg-slate-100";
    html += `<button onclick="goHrPage(${p})" class="${BASE} ${cls}">${p}</button>`;
  }
  if (endPage < totalPage)
    html += `<button onclick="goHrPage(${endPage + 1})" class="${BASE} text-slate-400 hover:bg-slate-100">›</button>`;
  container.innerHTML = html;
}

function goHrPage(p) {
  const scrollY = window.scrollY;
  doFilterHrList(p).then(() => window.scrollTo({ top: scrollY, behavior: "instant" }));
}


/* ─── 학생 상세 모달 열기 ─── */
function openDetail(id) {
  const row = document.querySelector('.hr-data-row[data-id="' + id + '"]');
  if (!row) return;

  const typeVal   = row.dataset.type   || "";
  const enableVal = row.dataset.enable || "";

  const stuType  = typeVal === "ROLE_USER" ? "일반" : typeVal === "ROLE_STUDENT" ? "오프라인" : "-";
  const statusTx = enableVal === "Y" ? "정상" : "탈퇴";

  const zip  = row.dataset.zip  || "";
  const addr = (row.dataset.addr || "").trim() || "-";
  const joinRaw = (row.dataset.join || "").substring(0, 10);

  selectedEmpId = id;

  // 아바타
  const profileUrl = row.dataset.profile || "";
  const avatarImg  = document.getElementById("detail-avatar-img");
  const avatarDiv  = document.getElementById("detail-avatar");
  if (profileUrl.startsWith("http")) {
    if (avatarImg) {
      avatarImg.src = profileUrl;
      avatarImg.classList.remove("hidden");
      avatarImg.style.cursor = "zoom-in";
      avatarImg.onclick = () => openProfileLightbox(profileUrl);
    }
    if (avatarDiv) avatarDiv.classList.add("hidden");
  } else {
    if (avatarImg) {
      avatarImg.classList.add("hidden");
      avatarImg.onclick = null;
    }
    if (avatarDiv) {
      avatarDiv.classList.remove("hidden");
      avatarDiv.textContent = row.dataset.name ? row.dataset.name[0] : "?";
    }
  }

  document.getElementById("detail-title").textContent = (row.dataset.name || "-") + " · 학생 상세";
  document.getElementById("detail-name").textContent  = row.dataset.name || "-";

  const badge = document.getElementById("detail-status-badge");
  badge.className   = "status-badge " + (enableVal === "Y" ? "status-active" : "status-resigned");
  badge.textContent = statusTx;

  document.getElementById("detail-dept-tag").textContent = stuType;
  document.getElementById("detail-role-tag").textContent = joinRaw ? "가입일 " + joinRaw : "";

  document.getElementById("detail-phone").textContent = formatPhoneDisplay(row.dataset.phone);
  document.getElementById("detail-email").textContent = row.dataset.email || "-";
  document.getElementById("detail-addr").textContent  = zip ? "(" + zip + ") " + addr : addr;

  document.getElementById("detail-work-type").textContent = stuType;
  document.getElementById("detail-entry").textContent     = joinRaw || "-";
  document.getElementById("detail-enable").textContent    = statusTx;

  document.getElementById("resign-confirm-name").textContent = row.dataset.name || "";

  // 학부모 정보 섹션 (오프라인 학생만)
  const parentSection = document.getElementById("detail-parent-section");
  if (typeVal === "ROLE_STUDENT") {
    parentSection.classList.remove("hidden");
    const linked   = (row.dataset.parentId || "").trim() !== "";
    const badge    = document.getElementById("detail-parent-badge");
    const linkedEl = document.getElementById("detail-parent-linked");
    const unlinkEl = document.getElementById("detail-parent-unlinked");

    if (linked) {
      badge.className   = "inline-flex items-center gap-1 text-xs font-semibold rounded-lg px-2 py-0.5 text-emerald-600 bg-emerald-50";
      badge.innerHTML   = '<i class="fa-solid fa-link text-[9px]"></i>연동됨';
      linkedEl.classList.remove("hidden");
      unlinkEl.classList.add("hidden");
      document.getElementById("detail-parent-name").textContent  = row.dataset.parentName || "-";
      document.getElementById("detail-parent-phone").textContent = formatPhoneDisplay(row.dataset.parentPhone);
      document.getElementById("detail-parent-email").textContent = row.dataset.parentEmail || "-";
    } else {
      badge.className   = "inline-flex items-center gap-1 text-xs font-semibold rounded-lg px-2 py-0.5 text-orange-500 bg-orange-50";
      badge.innerHTML   = '<i class="fa-solid fa-link-slash text-[9px]"></i>미연동';
      linkedEl.classList.add("hidden");
      unlinkEl.classList.remove("hidden");
      const btn = document.getElementById("detail-parent-link-btn");
      btn.onclick = () => { closeModal("modal-emp-detail"); openParentLinkModal(row.dataset.id, row.dataset.name); };
    }
  } else {
    parentSection.classList.add("hidden");
  }

  if (detailEditMode) toggleDetailEdit();
  openModal("modal-emp-detail");
}

/* ─── 수정 폼 토글 ─── */
function toggleDetailEdit() {
  detailEditMode = !detailEditMode;
  const form = document.getElementById("detail-edit-form");
  const btn  = document.getElementById("detail-edit-btn");
  form.classList.toggle("hidden", !detailEditMode);
  btn.innerHTML = detailEditMode
    ? '<i class="fa-solid fa-xmark mr-1"></i>취소'
    : '<i class="fa-solid fa-pen mr-1"></i>수정';

  if (detailEditMode) {
    const row = document.querySelector('.hr-data-row[data-id="' + selectedEmpId + '"]');
    if (!row) return;

    document.getElementById("edit-name").value        = row.dataset.name       || "";
    document.getElementById("edit-phone").value       = formatPhoneDisplay(row.dataset.phone);
    document.getElementById("edit-email").value       = row.dataset.email      || "";
    document.getElementById("edit-zipcode").value     = row.dataset.zip        || "";
    document.getElementById("edit-addr").value        = row.dataset.addrBase   || "";
    document.getElementById("edit-addr-detail").value = row.dataset.addrDetail || "";

    const rawProfile = row.dataset.profile || "";
    document.getElementById("edit-profile-path").value = rawProfile.startsWith("http") ? rawProfile : "";
    document.getElementById("edit-profile").value = "";

    document.getElementById("edit-stu-type").value = row.dataset.type   || "ROLE_USER";
  }
}

/* ─── 프로필 이미지 확대 ─── */
function openProfileLightbox(url) {
  const overlay = document.createElement("div");
  overlay.style.cssText = [
    "position:fixed", "inset:0", "z-index:9999",
    "background:rgba(0,0,0,0.75)", "display:flex",
    "align-items:center", "justify-content:center", "cursor:zoom-out",
  ].join(";");

  const img = document.createElement("img");
  img.src = url;
  img.style.cssText = "max-width:80vw;max-height:80vh;border-radius:1rem;box-shadow:0 8px 40px rgba(0,0,0,0.5)";
  overlay.appendChild(img);
  document.body.appendChild(overlay);

  const close = () => overlay.remove();
  overlay.addEventListener("click", close);
  document.addEventListener("keydown", function onKey(e) {
    if (e.key === "Escape") { close(); document.removeEventListener("keydown", onKey); }
  });
}

/* ─── 테이블 행 셀 즉시 동기화 ─── */
function syncRowCells(row) {
  const cells     = row.cells;
  const typeLabel = row.dataset.type === "ROLE_USER" ? "일반" : row.dataset.type === "ROLE_STUDENT" ? "오프라인" : "-";

  // col 0: 이름 + 아바타 이니셜
  const nameEl = cells[0].querySelector("p.font-bold");
  if (nameEl) nameEl.textContent = row.dataset.name || "";
  const textAvatar = cells[0].querySelector(".bg-blue-100");
  if (textAvatar) textAvatar.textContent = (row.dataset.name || "?")[0];

  // col 1: 유형
  cells[1].textContent = typeLabel;

  // col 2: 이메일
  cells[2].textContent = row.dataset.email || "-";

  // col 3: 가입일
  cells[3].textContent = (row.dataset.join || "").substring(0, 10) || "-";

  // col 4: 상태 badge
  const badge = cells[4].querySelector(".status-badge");
  if (badge) {
    const isActive = row.dataset.enable === "Y";
    badge.textContent = isActive ? "정상" : "탈퇴";
    badge.className   = "status-badge " + (isActive ? "status-active" : "status-resigned");
  }
}

/* ─── 수정 저장 ─── */
function saveDetailEdit() {
  const row = document.querySelector('.hr-data-row[data-id="' + selectedEmpId + '"]');
  if (!row) return;

  const addrDetail = (document.getElementById("edit-addr-detail").value || "").trim();
  const addrBase   = document.getElementById("edit-addr").value.trim();

  const _before = {
    name:  row.dataset.name                       || '-',
    phone: formatPhoneDisplay(row.dataset.phone)  || '-',
    email: row.dataset.email                      || '-',
    type:  row.dataset.type                       || '-',
  };

  const formData = new FormData();
  formData.append("userId",        selectedEmpId);
  formData.append("userName",      document.getElementById("edit-name").value.trim());
  formData.append("userTelno",     document.getElementById("edit-phone").value);
  formData.append("userEmailAddr", document.getElementById("edit-email").value.trim());
  formData.append("userZip",       document.getElementById("edit-zipcode").value.trim());
  formData.append("userAddr",      addrBase);
  formData.append("userDaddr",     addrDetail);
  formData.append("userProfile",   document.getElementById("edit-profile-path").value.trim());
  formData.append("userRole",      document.getElementById("edit-stu-type").value);
  const profileFile = document.getElementById("edit-profile").files[0];
  if (profileFile) formData.append("editProfileImage", profileFile);

  showHermesApprovalConfirm({
    title: '학생 정보 수정 결재 등록',
    type: 'update',
    fields: [
      { label: '이름',   before: _before.name,  after: document.getElementById("edit-name").value.trim() },
      { label: '연락처', before: _before.phone, after: document.getElementById("edit-phone").value },
      { label: '이메일', before: _before.email, after: document.getElementById("edit-email").value.trim() },
      { label: '유형',   before: _before.type,  after: document.getElementById("edit-stu-type").value },
    ],
    onConfirm: () => {
      closeHermesApprovalConfirm();
      fetch("/admin/students/update", { method: "PUT", body: formData })
        .then((res) => res.json())
        .then((data) => {
          if (data.result === "success") {
            // 결재 요청 성공 — DOM은 현재 DB 값 그대로 유지 (승인 후 반영)
            toggleDetailEdit();
            showHermesToast("결재 요청이 완료되었습니다. 승인 후 반영됩니다.", "info");
          } else {
            showHermesToast("수정 실패: " + (data.message || "서버 오류"), "error");
          }
        })
        .catch(() => showHermesToast("수정 요청 중 오류가 발생했습니다.", "error"));
    },
  });
}

/* ─── 탈퇴 처리 ─── */
function openResignConfirm() {
  const reason = document.getElementById("resign-reason");
  const reasonVal = reason && reason.customSelect ? reason.customSelect.getValue() : reason ? reason.value : "";
  const detail = document.getElementById("resign-reason-detail");

  if (!reasonVal) {
    showHermesToast("탈퇴 사유를 선택해주세요.", "error");
    return;
  }
  if (reasonVal === "04" && (!detail || !detail.value.trim())) {
    showHermesToast("기타 사유를 입력해주세요.", "error");
    return;
  }

  const row = document.querySelector('.hr-data-row[data-id="' + selectedEmpId + '"]');
  document.getElementById("resign-confirm-name").textContent = row ? row.dataset.name || "" : "";
  openModal("modal-resign-confirm");
}

function onResignReasonChange(elOrVal) {
  const val = typeof elOrVal === "string" ? elOrVal : elOrVal.value || "";
  const detail = document.getElementById("resign-reason-detail");
  if (val === "04") {
    detail.classList.remove("hidden");
    detail.focus();
  } else {
    detail.classList.add("hidden");
    detail.value = "";
  }
}

function executeResign() {
  const reasonSelectEl = document.getElementById("resign-reason");
  const reasonDetail   = document.getElementById("resign-reason-detail");
  const selectedVal    = reasonSelectEl.customSelect ? reasonSelectEl.customSelect.getValue() : reasonSelectEl.value;
  let withdrawRsn      = selectedVal === "04" ? (reasonDetail.value || "").trim() : selectedVal;

  if (!withdrawRsn) {
    showHermesToast(selectedVal === "04" ? "기타 사유를 입력해주세요." : "탈퇴 사유를 선택해주세요.", "error");
    return;
  }
  if (selectedVal === "04") withdrawRsn = "기타: " + withdrawRsn;

  const _stuName = document.querySelector('.hr-data-row[data-id="' + selectedEmpId + '"]')?.dataset.name || selectedEmpId;
  closeModal("modal-resign-confirm");

  showHermesApprovalConfirm({
    title: '학생 탈퇴 처리 결재 등록',
    type: 'delete',
    target: `${_stuName} — ${withdrawRsn}`,
    onConfirm: () => {
      closeHermesApprovalConfirm();
      fetch("/admin/students/" + selectedEmpId + "/retirement", {
        method: "PUT",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ withdrawRsn }),
      })
        .then((res) => res.json())
        .then((data) => {
          if (data.result === "success") {
            const row = document.querySelector('.hr-data-row[data-id="' + selectedEmpId + '"]');
            if (row) {
              row.dataset.enable = "N";
              syncRowCells(row);
            }
            closeModal("modal-emp-detail");
            fetchStudentStats();
            const rsEl = document.getElementById("resign-reason");
            if (rsEl.customSelect) rsEl.customSelect.setValue("");
            else rsEl.value = "";
            document.getElementById("resign-reason-detail").value = "";
            document.getElementById("resign-reason-detail").classList.add("hidden");
            filterHrList();
            showHermesToast("탈퇴 처리 및 계정이 비활성화되었습니다.", "success");
          } else {
            showHermesToast("탈퇴 처리 실패: " + (data.message || "서버 오류"), "error");
          }
        })
        .catch(() => showHermesToast("탈퇴 처리 요청 중 오류가 발생했습니다.", "error"));
    },
  });
}

/* ─── 학생 ID 자동 생성 (YYS00001 형식) ─── */
function autoGenStudentId() {
  const idEl = document.getElementById("new-stu-login-id");
  if (!idEl) return;

  const year          = new Date().getFullYear().toString().slice(-2); // "26"
  const baseId        = year + "S";                                    // "26S"
  const defaultSerial = "00001";

  fetch(`/admin/employees/students/next-id?baseId=${baseId}&defaultSerial=${defaultSerial}`)
    .then((res) => res.text())
    .then((nextId) => {
      idEl.value = nextId;
    })
    .catch(() => {
      idEl.value = baseId + defaultSerial;
    });
}


function autoFillTempPw() {
  const rrn = document.getElementById("new-rrn").value.replace(/\D/g, "");
  const pwEl = document.getElementById("new-stu-pw");
  if (pwEl) pwEl.value = rrn.length >= 8 ? rrn.slice(0, 8) : "";

  if (rrn.length >= 7) {
    const genderDigit = parseInt(rrn[6], 10);
    const yy = rrn.slice(0, 2), mm = rrn.slice(2, 4), dd = rrn.slice(4, 6);
    let century = "19";
    if (genderDigit === 3 || genderDigit === 4) century = "20";
    else if (genderDigit === 9 || genderDigit === 0) century = "18";

    const month = parseInt(mm, 10), day = parseInt(dd, 10);
    if (month < 1 || month > 12) {
      showHermesToast("주민등록번호의 '월' 입력이 잘못되었습니다.", "error");
      clearRrnDerived(); return;
    }
    if (rrn.length === 13) {
      const fullYear = parseInt(century + yy, 10);
      const dc = new Date(fullYear, month - 1, day);
      if (dc.getFullYear() !== fullYear || dc.getMonth() !== month - 1 || dc.getDate() !== day) {
        showHermesToast(`존재하지 않는 날짜입니다. (${month}월 ${day}일)`, "error");
        clearRrnDerived(); return;
      }
    }
    document.getElementById("new-brdt").value = century + yy + "-" + mm + "-" + dd;
    const gndrMap = { 1: "M", 2: "F", 3: "M", 4: "F" };
    const gndrVal = gndrMap[genderDigit] || "";
    document.getElementById("new-gndr").value = gndrVal;
    document.getElementById("new-gndr-display").value = gndrVal === "M" ? "남성" : gndrVal === "F" ? "여성" : "";
  } else {
    document.getElementById("new-brdt").value = "";
    document.getElementById("new-gndr").value = "";
    document.getElementById("new-gndr-display").value = "";
  }
}

function clearRrnDerived() {
  document.getElementById("new-brdt").value = "";
  document.getElementById("new-gndr").value = "";
  document.getElementById("new-gndr-display").value = "";
  const pwEl = document.getElementById("new-stu-pw");
  if (pwEl) pwEl.value = "";
}

function clearRrnOutputs() {
  HermesRrn.clear();
  document.getElementById("new-rrn").value = "";
  document.getElementById("new-rrn-display").value = "";
  clearRrnDerived();
  document.getElementById("new-rrn-display").focus();
}


/* ─── 신규 학생 등록 폼 유효성 검사 ─── */
function validateNewStu() {
  const nameEl  = document.getElementById("new-name");
  const rrnEl   = document.getElementById("new-rrn");
  const rrnDisp = document.getElementById("new-rrn-display");
  const phoneEl = document.getElementById("new-phone");
  const emailEl = document.getElementById("new-email");
  const addrEl  = document.getElementById("address");
  const idEl    = document.getElementById("new-stu-login-id");
  const pwEl    = document.getElementById("new-stu-pw");

  const name = nameEl.value.trim();
  if (!name) { showHermesToast("이름을 입력해주세요.", "error"); nameEl.focus(); return false; }
  if (name.length < 2 || name.length > 50) { showHermesToast("이름은 2자 이상 50자 이하로 입력해주세요.", "error"); nameEl.focus(); return false; }
  if (!/^[가-힣a-zA-Z]+$/.test(name)) { showHermesToast("이름은 한글 또는 영문만 입력 가능합니다.", "error"); nameEl.focus(); return false; }

  if (!rrnEl.value.trim()) { showHermesToast("주민등록번호를 입력해주세요.", "error"); rrnDisp.focus(); return false; }
  if (!/^\d{6}-\d{7}$/.test(rrnEl.value)) { showHermesToast("주민등록번호는 000000-0000000 형식이어야 합니다.", "error"); rrnDisp.focus(); return false; }
  if (!validateRrnChecksum(rrnEl.value.replace(/\D/g, ""))) { showHermesToast("유효하지 않은 주민등록번호입니다.", "error"); rrnDisp.focus(); return false; }

  if (!phoneEl.value.trim()) { showHermesToast("연락처를 입력해주세요.", "error"); phoneEl.focus(); return false; }
  if (!/^010-\d{3,4}-\d{4}$/.test(phoneEl.value.trim())) { showHermesToast("올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)", "error"); phoneEl.focus(); return false; }

  const email = emailEl.value.trim();
  if (email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) { showHermesToast("유효한 이메일 형식이 아닙니다.", "error"); emailEl.focus(); return false; }

  if (!addrEl.value.trim()) { showHermesToast("기본 주소를 입력해주세요.", "error"); return false; }

  if (!idEl.value.trim()) { showHermesToast("로그인 ID를 입력해주세요.", "error"); idEl.focus(); return false; }
  if (!pwEl.value.trim()) { showHermesToast("초기 비밀번호를 입력해주세요.", "error"); pwEl.focus(); return false; }

  // 유효성 통과 → 결재 확인 모달
  showHermesApprovalConfirm({
    title: '학생 신규 등록 결재 등록',
    type: 'create',
    fields: [
      { label: '이름',    value: nameEl.value.trim() },
      { label: '연락처', value: phoneEl.value.trim() },
      { label: '로그인 ID', value: idEl.value.trim() },
    ],
    onConfirm: () => {
      closeHermesApprovalConfirm();
      document.getElementById('form-new-student').submit();
    },
  });
  return false;
}


/* ─── 우편번호 검색 ─── */
function searchStuZipCode() {
  new daum.Postcode({
    oncomplete: function (data) {
      const addr = data.userSelectedType === "R" ? data.roadAddress : data.jibunAddress;
      document.getElementById("stu-postcode").value    = data.zonecode;
      document.getElementById("stu-address").value     = addr;
      document.getElementById("stu-detail-address").focus();
    },
  }).open();
}

document.querySelectorAll('[id^="modal-"]').forEach((m) => {
  let mousedownOnBackdrop = false;
  m.addEventListener("mousedown", (e) => { mousedownOnBackdrop = e.target === m; });
  m.addEventListener("click", (e) => {
    if (!(e.target === m && mousedownOnBackdrop)) return;
    if (m.id === "modal-emp-detail" && detailEditMode) {
      showWarningToast("수정 중입니다. X 버튼으로 닫거나 취소를 눌러주세요.");
      return;
    }
    m.classList.add("hidden");
  });
});

/* ─── 초기 렌더링 ─── */
(function () {
  const firstBtn = document.querySelector('.emp-tab-btn[data-tab="stu-tab-list"]');
  if (firstBtn) switchEmpTab("stu-tab-list", firstBtn);
  setTimeout(filterHrList, 0);

  const params = new URLSearchParams(window.location.search);
  const errMsg = params.get("error");
  const okMsg  = params.get("success");
  if (errMsg) {
    setTimeout(() => showHermesToast(decodeURIComponent(errMsg), "error"), 300);
    history.replaceState(null, "", window.location.pathname);
  }
  if (okMsg) {
    setTimeout(() => showHermesToast(decodeURIComponent(okMsg), "success"), 300);
    history.replaceState(null, "", window.location.pathname);
  }
})();

/* 탈퇴 사유 select: 모달 하단에 있어 드롭다운을 위로 펼치게 한다 */
document.addEventListener("DOMContentLoaded", function () {
  initDeferredSelects();
});
