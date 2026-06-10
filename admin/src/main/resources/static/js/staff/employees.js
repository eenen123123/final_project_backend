
var selectedEmpId = null;
var detailEditMode = false;

/* ─── 탭 전환 ─── */
function switchEmpTab(tabId, btn) {
  document
    .querySelectorAll(".emp-tab-btn")
    .forEach((b) => b.classList.remove("active"));
  document
    .querySelectorAll(".emp-tab-panel")
    .forEach((p) => p.classList.remove("active"));
  btn.classList.add("active");
  document.getElementById(tabId).classList.add("active");
}

/* ─── 페이지 깜빡임 없는 탭 이동 ─── */
async function navigateToStudents(btn) {
  btn.disabled = true;
  try {
    const res = await fetch('/admin/employees/students');
    if (!res.ok) { location.href = '/admin/employees/students'; return; }

    const html = await res.text();
    const doc  = new DOMParser().parseFromString(html, 'text/html');
    const newMain  = doc.querySelector('main');
    const currMain = document.querySelector('main');
    if (!newMain || !currMain) { location.href = '/admin/employees/students'; return; }

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

    // 기존 직원 스크립트 제거 후 학생 스크립트 로드
    document.querySelectorAll('script[src*="/js/staff/"]').forEach(s => s.remove());
    for (const s of doc.querySelectorAll('script[src*="/js/staff/"]')) {
      await new Promise(resolve => {
        const el = document.createElement('script');
        el.src = s.getAttribute('src');
        el.onload = el.onerror = resolve;
        document.head.appendChild(el);
      });
    }

    currMain.querySelectorAll('select.hm-input:not([data-ts-defer])').forEach(el => {
      if (!el.tomselect && window.initTomSelect) window.initTomSelect(el);
    });

    history.pushState({ url: '/admin/employees/students' }, doc.title || '', '/admin/employees/students');
    if (doc.title) document.title = doc.title;

  } catch {
    location.href = '/admin/employees/students';
  }
}

/* ─── 직원 현황 카드 렌더링 ─── */
/* ═══ 인사 기록 탭 페이징 + 필터 + 정렬 ═══ */
var currentHrPage = 1;
var HR_SCREEN_SIZE = 7;
var HR_BLOCK_SIZE = 5;
var hrSortCol = null;
var hrSortAsc = true;
var hrFilteredRows = null;

var filterDebounceTimer = null;

function filterHrList() {
  clearTimeout(filterDebounceTimer);
  filterDebounceTimer = setTimeout(() => doFilterHrList(1), 300);
}

async function doFilterHrList(page) {
  page = page || 1;
  currentHrPage = page;

  const keyword   = document.getElementById("hr-search").value.trim();
  const year      = document.getElementById("hr-year").value;
  const status    = document.getElementById("hr-status-filter").value;
  const deptCd    = document.getElementById("hr-dept-filter").value;
  const jbgrCd    = document.getElementById("hr-role-filter").value;
  const emplType  = document.getElementById("hr-type-filter").value;

  const params = new URLSearchParams();
  if (keyword)   params.set("keyword",        keyword);
  if (year)      params.set("year",           year);
  if (status)    params.set("status",         status);
  if (deptCd)    params.set("deptCd",         deptCd);
  if (jbgrCd)    params.set("jbgrCd",         jbgrCd);
  if (emplType)  params.set("emplTypeCd",     emplType);
  if (hrSortCol) {
    params.set("orderBy",        hrSortCol);
    params.set("orderDirection", hrSortAsc ? "ASC" : "DESC");
  }
  params.set("page",       page);
  params.set("screenSize", HR_SCREEN_SIZE);

  try {
    const res  = await fetch("/admin/employees/search?" + params);
    const data = await res.json();
    renderEmployeeTable(data.items, data.totalCount);
  } catch (e) {
    console.error("직원 검색 실패:", e);
  }
}

// defer 스크립트와 AJAX 탭 이동 모두에서 DOM 준비 완료 후 실행되므로 직접 호출
doFilterHrList(1);

function escHtml(s) {
  return String(s == null ? "" : s)
    .replace(/&/g, "&amp;").replace(/</g, "&lt;")
    .replace(/>/g, "&gt;").replace(/"/g, "&quot;");
}

function renderEmployeeTable(employees, totalCount) {
  const tbody = document.getElementById("hr-table-body");
  if (!employees || employees.length === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="text-center py-10 text-sm text-slate-400">등록된 직원이 없습니다.</td></tr>';
    renderHrPagination(0);
    return;
  }

  tbody.innerHTML = employees.map(emp => {
    const m   = emp.member || {};
    const ei  = emp.employeeInfo || {};
    const uid = emp.userId || "";
    const nm  = m.userName || "";
    const pro = m.userProfile || "";
    const jn  = ei.joinYmd   ? String(ei.joinYmd).substring(0, 10)   : "";
    const et  = (ei.emplTypeCd || "").trim();
    const es  = ei.emplStatCd || "";
    const ct  = ei.ctrctEndYmd ? String(ei.ctrctEndYmd).substring(0, 10) : "";
    const addr = (m.userAddr || "") + (m.userDaddr ? " " + m.userDaddr : "");

    const typeNm  = { "01":"정규직","02":"계약직","03":"파트타임" }[et] || "-";
    const statNm  = { "01":"재직","02":"휴직","03":"퇴사" }[es] || "미등록";
    const statCls = { "01":"status-active","02":"status-leave","03":"status-resigned" }[es] || "status-resigned";
    const avatar  = (pro && pro.startsWith("http"))
      ? `<img src="${escHtml(pro)}" class="w-7 h-7 rounded-lg object-cover" alt="프로필">`
      : `<div class="w-7 h-7 rounded-lg bg-blue-100 flex items-center justify-center text-xs font-bold text-[#3b82f6]">${escHtml(nm.charAt(0))}</div>`;

    return `<tr class="hr-data-row hover:bg-slate-50 transition-colors"
      data-id="${escHtml(uid)}" data-name="${escHtml(nm)}" data-grade="${escHtml(emp.jbgrNm||"")}"
      data-grade-cd="${escHtml(ei.jbgrCd||"")}" data-join="${escHtml(jn)}" data-type="${escHtml(et)}"
      data-dept="${escHtml(emp.deptCd||"")}" data-dept-nm="${escHtml(emp.deptNm||"")}"
      data-status="${escHtml(es)}" data-phone="${escHtml(m.userTelno||"")}"
      data-email="${escHtml(m.userEmailAddr||"")}" data-birthdate="${escHtml(m.userBrdt||"")}"
      data-gender="${escHtml(m.userGndrCd||"")}" data-zip="${escHtml(m.userZip||"")}"
      data-addr-base="${escHtml(m.userAddr||"")}" data-addr-detail="${escHtml(m.userDaddr||"")}"
      data-addr="${escHtml(addr)}" data-contract-end="${escHtml(ct)}"
      data-duty="${escHtml(ei.chrgDutyCn||"")}" data-salary="${escHtml(emp.baseSalary||"")}"
      data-profile="${escHtml(pro)}">
      <td class="py-3 px-4">
        <div class="flex items-center gap-2">
          ${avatar}
          <div>
            <p class="font-bold text-slate-800">${escHtml(nm)}</p>
            <p class="text-xs text-slate-400">${escHtml(uid)}</p>
          </div>
        </div>
      </td>
      <td class="py-3 px-4 text-sm text-slate-600 truncate">${escHtml(emp.deptNm||"-")}</td>
      <td class="py-3 px-4"><span class="text-sm text-slate-600 block truncate">${escHtml(emp.jbgrNm||"-")}</span></td>
      <td class="py-3 px-4 text-sm text-slate-600 whitespace-nowrap">${typeNm}</td>
      <td class="py-3 px-4 text-sm text-slate-600 whitespace-nowrap">${jn || "-"}</td>
      <td class="py-3 px-4 text-sm text-slate-600 truncate">${ct || "-"}</td>
      <td class="py-3 px-4"><span class="status-badge ${statCls}">${statNm}</span></td>
      <td class="py-3 px-4">
        <button type="button" data-id="${escHtml(uid)}"
          onclick="openDetail(this.getAttribute('data-id'))"
          class="text-xs text-[#3b82f6] hover:underline font-semibold">상세</button>
      </td>
    </tr>`;
  }).join("");

  renderHrPagination(totalCount);
}

// 직급 필터 전체 옵션 원본 저장 (Tom Select 초기화 전에 native select에서 읽음)
var allRoleOptions = Array.from(
  document.querySelectorAll('#hr-role-filter option[value]:not([value=""])'),
).map((opt) => ({
  value: opt.value,
  text: opt.textContent.trim(),
  dept: opt.dataset.dept || "",
}));

function updateHrRoleFilter(deptCd) {
  const ts = document.getElementById("hr-role-filter").tomselect;
  if (!ts) return;
  ts.clear(true);
  ts.clearOptions();
  const filtered = deptCd
    ? allRoleOptions.filter((o) => o.dept === deptCd)
    : allRoleOptions;
  filtered.forEach((o) => ts.addOption({ value: o.value, text: o.text }));
  ts.refreshOptions(false);
}

function onHrDeptChange(deptCd) {
  updateHrRoleFilter(deptCd);
  filterHrList();
}

function resetHrFilter() {
  document.getElementById("hr-search").value = "";
  updateHrRoleFilter("");
  [
    "hr-year",
    "hr-status-filter",
    "hr-dept-filter",
    "hr-role-filter",
    "hr-type-filter",
  ].forEach((id) => {
    const el = document.getElementById(id);
    if (el.tomselect) el.tomselect.setValue("");
    else el.value = "";
  });
  hrSortCol = null;
  hrSortAsc = true;
  updateHrSortIcons(null);
  filterHrList();
}

function sortHrBy(col) {
  if (hrSortCol === col) {
    hrSortAsc = !hrSortAsc;
  } else {
    hrSortCol = col;
    hrSortAsc = true;
  }
  updateHrSortIcons(col);
  doFilterHrList(1);
}

function applyHrSort(rows) {
  rows.sort((a, b) => {
    const va = a.dataset[hrSortCol] || "";
    const vb = b.dataset[hrSortCol] || "";
    if (hrSortCol === "join" || hrSortCol === "contractEnd") {
      if (!va && !vb) return 0;
      if (!va) return 1;
      if (!vb) return -1;
      if (va < vb) return hrSortAsc ? -1 : 1;
      if (va > vb) return hrSortAsc ? 1 : -1;
      return 0;
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

  // 정렬 순서로 DOM 재배치
  rows.forEach((r) => tbody.appendChild(r));
  allRows.filter((r) => !rows.includes(r)).forEach((r) => tbody.appendChild(r));

  // 현재 페이지만 표시
  allRows.forEach((r) => (r.style.display = "none"));
  const offset = (currentHrPage - 1) * HR_SCREEN_SIZE;
  rows
    .slice(offset, offset + HR_SCREEN_SIZE)
    .forEach((r) => (r.style.display = ""));

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

  if (totalPage <= 1) {
    container.innerHTML = "";
    return;
  }

  const endPage = Math.min(
    Math.ceil(currentHrPage / HR_BLOCK_SIZE) * HR_BLOCK_SIZE,
    totalPage,
  );
  const startPage = Math.max(endPage - HR_BLOCK_SIZE + 1, 1);

  const BASE = "w-8 h-8 rounded-lg text-xs flex items-center justify-center";
  let html = "";
  if (startPage > 1) {
    html += `<button onclick="goHrPage(${startPage - 1})" class="${BASE} text-slate-400 hover:bg-slate-100">‹</button>`;
  }
  for (let p = startPage; p <= endPage; p++) {
    const cls = p === currentHrPage ? "bg-blue-500 text-white font-bold" : "text-slate-400 hover:bg-slate-100";
    html += `<button onclick="goHrPage(${p})" class="${BASE} ${cls}">${p}</button>`;
  }
  if (endPage < totalPage) {
    html += `<button onclick="goHrPage(${endPage + 1})" class="${BASE} text-slate-400 hover:bg-slate-100">›</button>`;
  }
  container.innerHTML = html;
}

function goHrPage(p) {
  const scrollY = window.scrollY;
  doFilterHrList(p).then(() => window.scrollTo({ top: scrollY, behavior: "instant" }));
}

/* ─── 인사 기록 테이블 렌더링 ─── */
/* ─── 전화번호 하이픈 포맷 (표시용) ─── */
function formatPhoneDisplay(tel) {
  if (!tel) return "-";
  const v = tel.replace(/\D/g, "");
  if (v.length === 11)
    return v.slice(0, 3) + "-" + v.slice(3, 7) + "-" + v.slice(7);
  if (v.length === 10)
    return v.slice(0, 3) + "-" + v.slice(3, 6) + "-" + v.slice(6);
  return tel;
}

/* ─── 직원 상세 모달 열기 ─── */
function openDetail(id) {
  const card = document.querySelector('.emp-card[data-id="' + id + '"]');
  const row = document.querySelector('.hr-data-row[data-id="' + id + '"]');
  const src = card || row;

  if (!src) return;

  // row 우선 — 수정 저장 후에도 최신값 반영
  const typeCd =
    (row ? row.dataset.type : card ? card.dataset.emplTypeCd : "") || "";
  const statCd =
    (row ? row.dataset.status : card ? card.dataset.emplStatCd : "") || "";

  const statusText =
    statCd === "01"
      ? "재직"
      : statCd === "02"
        ? "휴직"
        : statCd === "03"
          ? "퇴사"
          : "-";
  const workType =
    typeCd === "01"
      ? "정규직"
      : typeCd === "02"
        ? "계약직"
        : typeCd === "03"
          ? "파트타임"
          : "-";

  const zip = src.dataset.zip || (card ? card.dataset.zip : "") || "";
  const addr = (src.dataset.addr || "").trim() || "-";
  const e = {
    id: id,
    name: src.dataset.name || (card ? card.dataset.name : "") || "-",
    phone: formatPhoneDisplay(src.dataset.phone),
    email: src.dataset.email || "-",
    zip: zip,
    addr: addr,
    addrFull: zip ? "(" + zip + ") " + addr : addr,
    status: statusText,
    dept: src.dataset.deptNm || "-",
    role: card ? card.dataset.jbgrNm || "-" : row.dataset.grade || "-",
    entry: card ? card.dataset.joinYmd || "-" : row.dataset.join || "-",
    workType: workType,
    contractEnd: (
      (row ? row.dataset.contractEnd : src.dataset.contractEnd) || ""
    ).substring(0, 10),
    duty: src.dataset.duty || (card ? card.dataset.chrgDutyCn : "") || "-",
    salary:
      (row ? row.dataset.salary : "") ||
      (card ? card.dataset.salary : "") ||
      "-",
  };

  selectedEmpId = id;

  // 선택된 카드 강조
  document
    .querySelectorAll(".emp-card")
    .forEach((c) => c.classList.remove("selected"));
  if (card) card.classList.add("selected");

  const statusClass = {
    재직: "status-active",
    휴직: "status-leave",
    퇴사: "status-resigned",
  };

  document.getElementById("detail-title").textContent = e.name + " · 직원 상세";
  const profileUrl = (row ? row.dataset.profile : src.dataset.profile) || "";
  const avatarImg = document.getElementById("detail-avatar-img");
  const avatarDiv = document.getElementById("detail-avatar");
  if (profileUrl.startsWith("http")) {
    avatarImg.src = profileUrl;
    avatarImg.classList.remove("hidden");
    avatarImg.style.cursor = "zoom-in";
    avatarImg.onclick = () => openProfileLightbox(profileUrl);
    avatarDiv.classList.add("hidden");
  } else {
    avatarImg.classList.add("hidden");
    avatarImg.onclick = null;
    avatarDiv.classList.remove("hidden");
    avatarDiv.textContent = e.name && e.name !== "-" ? e.name[0] : "?";
  }
  document.getElementById("detail-name").textContent = e.name;
  document.getElementById("detail-status-badge").className =
    "status-badge " + (statusClass[e.status] || "status-resigned");
  document.getElementById("detail-status-badge").textContent = e.status;
  document.getElementById("detail-role-tag").className = "text-xs text-slate-500";
  document.getElementById("detail-role-tag").textContent = e.role;
  document.getElementById("detail-dept-tag").textContent = e.dept;
  document.getElementById("detail-phone").textContent = e.phone;
  document.getElementById("detail-email").textContent = e.email;
  document.getElementById("detail-addr").textContent = e.addrFull;
  document.getElementById("detail-entry").textContent = e.entry;
  document.getElementById("detail-tenure").textContent =
    e.entry && e.entry !== "-" ? calcTenure(e.entry) : "-";
  document.getElementById("detail-work-type").textContent = e.workType;
  document.getElementById("detail-salary").textContent = e.salary;
  const isContract = typeCd === "02" || typeCd === "03";
  document
    .getElementById("detail-contract-end-row")
    .classList.toggle("hidden", !isContract);
  document.getElementById("detail-contract-end").textContent =
    e.contractEnd || "-";
  document.getElementById("detail-duty").textContent = e.duty;

  // 수정 폼 초기화
  if (detailEditMode) toggleDetailEdit();
  document.getElementById("resign-confirm-name").textContent = e.name;

  openModal("modal-emp-detail");
}

function calcTenure(entry) {
  const d = new Date(entry);
  if (isNaN(d.getTime())) return "-";
  if (d.getFullYear() < 1900 || d > new Date()) return "날짜 오류";
  const now = new Date();
  let y = now.getFullYear() - d.getFullYear();
  let m = now.getMonth() - d.getMonth();
  if (now.getDate() < d.getDate()) m--;
  if (m < 0) {
    y--;
    m += 12;
  }
  if (y === 0 && m === 0) return "1개월 미만";
  return y > 0 ? `${y}년 ${m}개월` : `${m}개월`;
}

/* ─── 수정 폼 토글 ─── */
function toggleDetailEdit() {
  detailEditMode = !detailEditMode;
  const form = document.getElementById("detail-edit-form");
  const btn = document.getElementById("detail-edit-btn");
  form.classList.toggle("hidden", !detailEditMode);
  btn.innerHTML = detailEditMode
    ? '<i class="fa-solid fa-xmark mr-1"></i>취소'
    : '<i class="fa-solid fa-pen mr-1"></i>수정';
  if (detailEditMode) {
    const row = document.querySelector(
      '.hr-data-row[data-id="' + selectedEmpId + '"]',
    );
    if (row) {
      // 기본 기록
      document.getElementById("edit-name").value = row.dataset.name || "";
      document.getElementById("edit-birthdate").value = (
        row.dataset.birthdate || ""
      ).substring(0, 10);
      document.getElementById("edit-email").value = row.dataset.email || "";
      document.getElementById("edit-phone").value = formatPhoneDisplay(
        row.dataset.phone,
      );
      document.getElementById("edit-zipcode").value = row.dataset.zip || "";
      document.getElementById("edit-addr").value = row.dataset.addrBase || "";
      document.getElementById("edit-addr-detail").value =
        row.dataset.addrDetail || "";
      const genderEl = document.getElementById("edit-gender");
      if (genderEl.tomselect)
        genderEl.tomselect.setValue(row.dataset.gender || "U");
      else genderEl.value = row.dataset.gender || "U";

      // 인사 기록
      const deptEl = document.getElementById("edit-dept");
      if (deptEl.tomselect) deptEl.tomselect.setValue(row.dataset.dept || "");
      else deptEl.value = row.dataset.dept || "";
      filterEditRoleByDept(row.dataset.gradeCd || "");
      document.getElementById("edit-entry-date").value = row.dataset.join || "";
      const statusEl = document.getElementById("edit-status");
      const workTypeEl = document.getElementById("edit-work-type");
      if (statusEl.tomselect)
        statusEl.tomselect.setValue(row.dataset.status || "01");
      else statusEl.value = row.dataset.status || "01";
      if (workTypeEl.tomselect)
        workTypeEl.tomselect.setValue(row.dataset.type || "01");
      else workTypeEl.value = row.dataset.type || "01";
      toggleEditContractPeriod(row.dataset.type || "01");
      document.getElementById("edit-contract-end").value = (
        row.dataset.contractEnd || ""
      ).substring(0, 10);
      document.getElementById("edit-duty").value = row.dataset.duty || "";
      document.getElementById("edit-salary").value = row.dataset.salary || "";

      const profilePathEl = document.getElementById("edit-profile-path");
      const rawProfile = row.dataset.profile || "";
      if (profilePathEl) profilePathEl.value = rawProfile.startsWith("http") ? rawProfile : "";
      const profileFileEl = document.getElementById("edit-profile");
      if (profileFileEl) profileFileEl.value = "";
    }
  }
}

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

function syncRowCells(row) {
  const cells = row.cells;
  const typeMap = { "01": "정규직", "02": "계약직", "03": "파트타임" };
  const statusInfo = {
    "01": ["재직", "status-active"],
    "02": ["휴직", "status-leave"],
    "03": ["퇴사", "status-resigned"],
  };

  // col 0: 이름 텍스트 + 아바타 이니셜
  const nameEl = cells[0].querySelector("p.font-bold");
  if (nameEl) nameEl.textContent = row.dataset.name || "";
  const textAvatar = cells[0].querySelector(".bg-blue-100");
  if (textAvatar) textAvatar.textContent = (row.dataset.name || "?")[0];

  // col 1: 부서
  cells[1].textContent = row.dataset.deptNm || "-";

  // col 2: 직급
  const gradeSpan = cells[2].querySelector("span");
  if (gradeSpan) gradeSpan.textContent = row.dataset.grade || "-";

  // col 3: 근무 유형
  cells[3].textContent = typeMap[row.dataset.type] || "-";

  // col 4: 입사일
  cells[4].textContent = row.dataset.join || "-";

  // col 5: 계약 기간
  const end = (row.dataset.contractEnd || "").substring(0, 10);
  cells[5].textContent = end || "-";

  // col 6: 상태 badge
  const badge = cells[6].querySelector(".status-badge");
  if (badge) {
    const [text, cls] = statusInfo[row.dataset.status] || ["미등록", "status-resigned"];
    badge.textContent = text;
    badge.className = "status-badge " + cls;
  }
}

function saveDetailEdit() {
  const row = document.querySelector(
    '.hr-data-row[data-id="' + selectedEmpId + '"]',
  );
  if (!row) return;

  const deptVal = document.getElementById("edit-dept").value;
  const deptOpt = document.querySelector(
    '#edit-dept option[value="' + deptVal + '"]',
  );
  const gradeVal = document.getElementById("edit-role").value;
  const gradeOpt = document.querySelector(
    '#edit-role option[value="' + gradeVal + '"]',
  );
  const addrDetail = (
    document.getElementById("edit-addr-detail").value || ""
  ).trim();

  row.dataset.name = document.getElementById("edit-name").value.trim();
  row.dataset.dept = deptVal;
  row.dataset.deptNm = deptOpt ? deptOpt.text : row.dataset.deptNm;
  row.dataset.gradeCd = gradeVal;
  row.dataset.grade = gradeOpt ? gradeOpt.text : row.dataset.grade;
  row.dataset.join = document.getElementById("edit-entry-date").value;
  row.dataset.status = document.getElementById("edit-status").value;
  row.dataset.type = document.getElementById("edit-work-type").value;
  row.dataset.contractEnd = document.getElementById("edit-contract-end").value;
  const addrBase = document.getElementById("edit-addr").value.trim();
  row.dataset.phone = document
    .getElementById("edit-phone")
    .value.replace(/-/g, "");
  row.dataset.zip = document.getElementById("edit-zipcode").value;
  row.dataset.addrBase = addrBase;
  row.dataset.addrDetail = addrDetail;
  row.dataset.addr = (addrBase + (addrDetail ? " " + addrDetail : "")).trim();
  row.dataset.duty = document.getElementById("edit-duty").value;
  row.dataset.salary = document.getElementById("edit-salary").value;

  // 카드가 DOM에 있으면 type/status도 동기화 (openDetail에서 stale 값 참조 방지)
  const card = document.querySelector(
    '.emp-card[data-id="' + selectedEmpId + '"]',
  );
  if (card) {
    card.dataset.emplTypeCd = row.dataset.type;
    card.dataset.emplStatCd = row.dataset.status;
  }

  const formData = new FormData();
  formData.append("userId", selectedEmpId);
  formData.append("userName", document.getElementById("edit-name").value.trim());
  formData.append("userGndrCd", document.getElementById("edit-gender").value);
  formData.append("userBrdt", document.getElementById("edit-birthdate").value);
  formData.append("userTelno", document.getElementById("edit-phone").value);
  formData.append("userEmailAddr", document.getElementById("edit-email").value.trim());
  formData.append("userZip", document.getElementById("edit-zipcode").value.trim());
  formData.append("userAddr", document.getElementById("edit-addr").value.trim());
  formData.append("userDaddr", addrDetail);
  formData.append("userProfile", document.getElementById("edit-profile-path").value.trim());
  formData.append("deptCd", deptVal);
  formData.append("jbgrCd", gradeVal);
  formData.append("joinYmd", document.getElementById("edit-entry-date").value);
  formData.append("emplStatCd", document.getElementById("edit-status").value);
  formData.append("emplTypeCd", document.getElementById("edit-work-type").value);
  formData.append("ctrctEndYmd", document.getElementById("edit-contract-end").value);
  formData.append("chrgDutyCn", document.getElementById("edit-duty").value.trim());
  formData.append("baseSalary", document.getElementById("edit-salary").value);
  const profileFile = document.getElementById("edit-profile").files[0];
  if (profileFile) formData.append("editProfileImage", profileFile);

  fetch("/admin/employees/update", {
    method: "PUT",
    body: formData,
  })
    .then((res) => res.json())
    .then((data) => {
      if (data.result === "success") {
        const newProfileUrl = data.profileUrl || "";
        row.dataset.profile = newProfileUrl;

        // 테이블 행 아바타 즉시 동기화 (새로고침 없이 반영)
        const avatarWrap = row.querySelector("td:first-child .flex");
        if (avatarWrap) {
          let img = avatarWrap.querySelector("img");
          const textDiv = avatarWrap.querySelector(".bg-blue-100");
          if (newProfileUrl.startsWith("http")) {
            if (!img) {
              img = document.createElement("img");
              img.className = "w-7 h-7 rounded-lg object-cover";
              img.alt = "프로필";
              avatarWrap.prepend(img);
            }
            img.src = newProfileUrl;
            if (textDiv) textDiv.style.display = "none";
          } else {
            if (img) img.remove();
            if (textDiv) textDiv.style.display = "";
          }
        }

        syncRowCells(row);
        toggleDetailEdit();
        openDetail(selectedEmpId);
        showHermesToast("결재 요청이 완료되었습니다. 승인 후 처리됩니다.", "success");
      } else {
        showHermesToast("수정 실패: " + (data.message || "서버 오류"), "error");
      }
    })
    .catch(() => showHermesToast("수정 요청 중 오류가 발생했습니다.", "error"));
}

// edit-role 전체 옵션 원본 저장
var allEditRoleOptions = Array.from(
  document.querySelectorAll('#edit-role option[value]:not([value=""])'),
).map((opt) => ({
  value: opt.value,
  text: opt.textContent.trim(),
  dept: opt.dataset.dept || "",
}));

function filterEditRoleByDept(selectValue) {
  const deptCd = document.getElementById("edit-dept").value;
  const ts = document.getElementById("edit-role").tomselect;
  if (!ts) return;
  ts.clear(true);
  ts.clearOptions();
  const filtered = deptCd
    ? allEditRoleOptions.filter((o) => o.dept === deptCd)
    : allEditRoleOptions;
  filtered.forEach((o) => ts.addOption({ value: o.value, text: o.text }));
  ts.refreshOptions(false);
  if (selectValue) ts.setValue(selectValue);
}

function toggleEditContractPeriod(val) {
  const wrap = document.getElementById("edit-contract-period-wrap");
  const end = document.getElementById("edit-contract-end");
  const show = val === "02" || val === "03";
  wrap.classList.toggle("hidden", !show);
  if (!show) end.value = "";
}

/* ─── 퇴사 처리 ─── */
function openResignConfirm() {
  const reason = document.getElementById("resign-reason");
  const reasonVal =
    reason && reason.tomselect
      ? reason.tomselect.getValue()
      : reason
        ? reason.value
        : "";
  const detail = document.getElementById("resign-reason-detail");
  const date = document.getElementById("resign-date");

  if (!reasonVal) {
    showHermesToast("퇴사 사유를 선택해주세요.", "error");
    return;
  }
  if (reasonVal === "기타" && (!detail || !detail.value.trim())) {
    showHermesToast("기타 사유를 입력해주세요.", "error");
    return;
  }
  if (!date || !date.value) {
    showHermesToast("퇴사일을 입력해주세요.", "error");
    return;
  }

  const row = document.querySelector(
    '.hr-data-row[data-id="' + selectedEmpId + '"]',
  );
  document.getElementById("resign-confirm-name").textContent = row
    ? row.dataset.name || ""
    : "";
  openModal("modal-resign-confirm");
}

function openResignConfirmDirect(id, name) {
  selectedEmpId = id;
  document.getElementById("resign-confirm-name").textContent = name;
  openModal("modal-resign-confirm");
}

function onResignReasonChange(elOrVal) {
  const val = typeof elOrVal === "string" ? elOrVal : elOrVal.value || "";
  const detail = document.getElementById("resign-reason-detail");
  if (val === "기타") {
    detail.classList.remove("hidden");
    detail.focus();
  } else {
    detail.classList.add("hidden");
    detail.value = "";
  }
}

function executeResign() {
  const reasonSelectEl = document.getElementById("resign-reason");
  const reasonDetail = document.getElementById("resign-reason-detail");
  const selectedVal = reasonSelectEl.tomselect
    ? reasonSelectEl.tomselect.getValue()
    : reasonSelectEl.value;
  let retmtRsn =
    selectedVal === "기타" ? (reasonDetail.value || "").trim() : selectedVal;

  if (!retmtRsn) {
    showHermesToast(
      selectedVal === "기타"
        ? "기타 사유를 입력해주세요."
        : "퇴사 사유를 선택해주세요.",
      "error",
    );
    return;
  }
  if (selectedVal === "기타") retmtRsn = "기타: " + retmtRsn;
  if (!retmtRsn) {
    showHermesToast("퇴사 사유를 입력해주세요.", "error");
    return;
  }

  fetch("/admin/employees/" + selectedEmpId + "/retirement", {
    method: "PUT",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify({ retmtRsn }),
  })
    .then((res) => res.json())
    .then((data) => {
      if (data.result === "success") {
        const row = document.querySelector(
          '.hr-data-row[data-id="' + selectedEmpId + '"]',
        );
        if (row) {
          row.dataset.status = "03";
          const card = document.querySelector(
            '.emp-card[data-id="' + selectedEmpId + '"]',
          );
          if (card) card.dataset.emplStatCd = "03";
        }
        closeModal("modal-resign-confirm");
        closeModal("modal-emp-detail");
        const rsEl = document.getElementById("resign-reason");
        if (rsEl.tomselect) rsEl.tomselect.setValue("");
        else rsEl.value = "";
        document.getElementById("resign-reason-detail").value = "";
        document.getElementById("resign-reason-detail").classList.add("hidden");
        filterHrList();
        showHermesToast("퇴사 처리 및 계정이 비활성화되었습니다.", "success");
      } else {
        showHermesToast(
          "퇴사 처리 실패: " + (data.message || "서버 오류"),
          "error",
        );
      }
    })
    .catch(() =>
      showHermesToast("퇴사 처리 요청 중 오류가 발생했습니다.", "error"),
    );
}

/* ─── 개인정보 파기 ─── */

/* ─── 한글 초성 → 영어 로마자 변환 ─── */
function koreanInitialsToEng(name) {
  const map = {
    ㄱ: "g",
    ㄲ: "kk",
    ㄴ: "n",
    ㄷ: "d",
    ㄸ: "tt",
    ㄹ: "r",
    ㅁ: "m",
    ㅂ: "b",
    ㅃ: "pp",
    ㅅ: "s",
    ㅆ: "ss",
    ㅇ: "y",
    ㅈ: "j",
    ㅉ: "jj",
    ㅊ: "ch",
    ㅋ: "k",
    ㅌ: "t",
    ㅍ: "p",
    ㅎ: "h",
  };

  const cho = [
    "ㄱ",
    "ㄲ",
    "ㄴ",
    "ㄷ",
    "ㄸ",
    "ㄹ",
    "ㅁ",
    "ㅂ",
    "ㅃ",
    "ㅅ",
    "ㅆ",
    "ㅇ",
    "ㅈ",
    "ㅉ",
    "ㅊ",
    "ㅋ",
    "ㅌ",
    "ㅍ",
    "ㅎ",
  ];

  let result = "";

  for (const char of name) {
    const code = char.charCodeAt(0) - 44032;

    if (code >= 0 && code <= 11171) {
      const choIndex = Math.floor(code / 588);
      result += map[cho[choIndex]];
    }
  }

  return result;
}

/* ─── 로그인 ID 자동 생성 (DB 순번 ++ 방식 반영) ─── */
function autoGenLoginId(force) {
  const name = document.getElementById("new-name").value.trim();
  const entry = document.getElementById("new-entry-date").value;
  const idEl = document.getElementById("new-login-id");
  if (!name || !entry) return;

  const year = entry.slice(0, 4);
  const month = entry.slice(5, 7);
  const initials = koreanInitialsToEng(name);

  // 기준점이 될 앞자리 패턴 (예: "202605KH")
  const baseId = year + month + initials;

  // 기본 일련번호 — 서버 next-id API로 실제 중복 없는 번호를 발급받음
  const defaultSerial = "01";
  const targetId = baseId + defaultSerial;

  // [실시간 중복 조회 및 차기 순번 발급]
  // 파라미터로 베이스 패턴과 기본 일련번호를 넘겨줍니다.
  fetch(
    `/admin/employees/next-id?baseId=${baseId}&defaultSerial=${defaultSerial}`,
  )
    .then((res) => res.text()) // JSON 대신 단순 문자열(ID)을 응답으로 받습니다.
    .then((nextAvailableId) => {
      // 만약 서버가 준 사용 가능 ID가 처음에 예측한 targetId와 다르다면 중복이 있었다는 뜻입니다.
      if (targetId !== nextAvailableId) {
        idEl.value = nextAvailableId; // 자동으로 ++된 ID(예: 202605KH08) 주입
        showHermesToast(
          `해당 ID 순번이 중복되어 다음 번호 [${nextAvailableId}]로 자동 지정되었습니다.`,
          "info",
        );
      } else {
        // 중복이 없다면 원래 예측한 순서대로 노출
        idEl.value = targetId;
      }
    })
    .catch((err) => console.error("ID 생성 중 서버 통신 오류:", err));
}

/* ─── 계약 기간 필드 show/hide ─── */
function toggleContractPeriod(val) {
  const wrap = document.getElementById("contract-period-wrap");
  const end = document.getElementById("new-contract-end");
  const show = val === "02" || val === "03";
  wrap.classList.toggle("hidden", !show);
  end.required = show;
  if (!show) end.value = "";
}

/* ─── 주민등록번호 체크섬(루한 알고리즘) 검증 ─── */
function validateRrnChecksum(digits) {
  // digits: 하이픈 없는 13자리 숫자 문자열
  const weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
  const sum = weights.reduce((acc, w, i) => acc + parseInt(digits[i], 10) * w, 0);
  const expected = (11 - (sum % 11)) % 10;
  return parseInt(digits[12], 10) === expected;
}

/* ─── 주민번호에서 생년월일·성별·임시비밀번호 자동 파생 및 날짜 유효성 체크 ─── */
/* ─── 주민번호 입력 시 실시간 날짜 유효성 검증 및 생년월일·성별 자동 파생 ─── */
function autoFillTempPw() {
  // 숫자만 추출
  const rrn = document.getElementById("new-rrn").value.replace(/\D/g, "");

  // 임시 비밀번호: 주민등록번호 앞 8자리 자동 설정
  document.getElementById("new-temp-pw").value =
    rrn.length >= 8 ? rrn.slice(0, 8) : "";

  // 1. 최소 7 자리가 입력되어 성별 코드가 확인되는 시점부터 분석 시작
  if (rrn.length >= 7) {
    const genderDigit = parseInt(rrn[6], 10);
    const yy = rrn.slice(0, 2);
    const mm = rrn.slice(2, 4);
    const dd = rrn.slice(4, 6);

    // 연도 세기 결정 (3, 4 -> 2000년대 / 1, 2 -> 1900년대 / 9, 0 -> 1800년대)
    let century = "19";
    if (genderDigit === 3 || genderDigit === 4) {
      century = "20";
    } else if (genderDigit === 9 || genderDigit === 0) {
      century = "18";
    }

    const fullYear = parseInt(century + yy, 10);
    const month = parseInt(mm, 10);
    const day = parseInt(dd, 10);

    // 2. 실시간 날짜 정규화 체크 (월 범위 검증)
    if (month < 1 || month > 12) {
      showHermesToast(
        "주민등록번호의 '월' 입력이 잘못되었습니다. (1월~12월 사이)",
        "error",
      );
      clearRrnDerived();
      return;
    }

    // 3. 13자리 전체가 완벽히 입력되었을 때 '일(Day)', '윤년', '체크섬' 최종 검증 발동
    if (rrn.length === 13) {
      const dateCheck = new Date(fullYear, month - 1, day);

      // 실제 존재하지 않는 날짜 판별 (예: 2월 30일, 4월 31일 등)
      if (
        dateCheck.getFullYear() !== fullYear ||
        dateCheck.getMonth() !== month - 1 ||
        dateCheck.getDate() !== day
      ) {
        showHermesToast(
          `존재하지 않는 날짜입니다. 입력된 날짜를 확인해 주세요. (${month}월 ${day}일)`,
          "error",
        );
        clearRrnDerived();
        return;
      }

    }

    // 4. 모든 검증을 통과한 유효한 날짜 포맷일 때만 화면에 반영
    document.getElementById("new-brdt").value =
      century + yy + "-" + mm + "-" + dd;

    // 성별 코드 매핑
    const gndrMap = { 1: "M", 2: "F", 3: "M", 4: "F" };
    const gndrVal = gndrMap[genderDigit] || "";
    document.getElementById("new-gndr").value = gndrVal;
    document.getElementById("new-gndr-display").value =
      gndrVal === "M" ? "남성" : gndrVal === "F" ? "여성" : "";
  } else {
    // 7자리 미만일 때는 입력값 초기화
    document.getElementById("new-brdt").value = "";
    document.getElementById("new-gndr").value = "";
    document.getElementById("new-gndr-display").value = "";
  }
}

/* ─── 파생 필드만 초기화 (주민번호 입력은 유지) ─── */
function clearRrnDerived() {
  document.getElementById("new-brdt").value = "";
  document.getElementById("new-gndr").value = "";
  document.getElementById("new-gndr-display").value = "";
  document.getElementById("new-temp-pw").value = "";
}

/* ─── 잘못된 주민번호 감지 시 입력 필드를 비우고 포커스를 주는 공통 함수 ─── */
function clearRrnOutputs() {
  rrnRealValue = "";
  document.getElementById("new-rrn").value = "";
  document.getElementById("new-rrn-display").value = "";
  document.getElementById("new-brdt").value = "";
  document.getElementById("new-gndr").value = "";
  document.getElementById("new-gndr-display").value = "";
  document.getElementById("new-temp-pw").value = "";
  document.getElementById("new-rrn-display").focus();
}

/* ─── 우편번호 검색 (Daum 우편번호 서비스 연동 준비) ─── */
function searchZipCode() {
  new daum.Postcode({
    oncomplete: function (data) {
      // 3. 사용자가 주소를 선택했을 때 실행되는 콜백 영역

      // 사용자가 도로명 주소를 선택했을 때와 지번 주소를 선택했을 때를 고려하여 변수를 설정한다.
      let addr = "";

      if (data.userSelectedType === "R") {
        // 사용자가 도로명 주소를 선택했을 경우
        addr = data.roadAddress;
      } else {
        // 사용자가 지번 주소를 선택했을 경우
        addr = data.jibunAddress;
      }

      // 4. 각각의 input 상자에 값을 매핑하여 넣어준다.
      document.getElementById("postcode").value = data.zonecode; // 5자리 우편번호
      document.getElementById("address").value = addr; // 주소 정보

      // 주소 입력이 완료되면 상세주소 입력창으로 포커스를 이동시킨다.
      document.getElementById("detailAddress").focus();
    },
  }).open(); // 팝업창을 연다.
}

function searchDetailZipCode() {
  new daum.Postcode({
    oncomplete: function (data) {
      const addr =
        data.userSelectedType === "R" ? data.roadAddress : data.jibunAddress;
      document.getElementById("edit-zipcode").value = data.zonecode;
      document.getElementById("edit-addr").value = addr;
      document.getElementById("edit-addr-detail").focus();
    },
  }).open();
}

/* ─── 신규 직원 등록 submit 전 유효성 검사 (SignupRequestRecord 기준 통일) ─── */
function validateNewEmp() {
  const nameEl = document.getElementById("new-name");
  const rrnEl = document.getElementById("new-rrn");
  const rrnDisplayEl = document.getElementById("new-rrn-display");
  const phoneEl = document.getElementById("new-phone");
  const emailEl = document.getElementById("new-email");
  const postcodeEl = document.getElementById("postcode");
  const addrEl = document.getElementById("address");
  const deptEl = document.getElementById("new-dept");
  const roleEl = document.getElementById("new-role");
  const entryEl = document.getElementById("new-entry-date");
  const typeEl = document.getElementById("new-work-type");
  const contractEndEl = document.getElementById("new-contract-end");
  const loginIdEl = document.getElementById("new-login-id");
  const baseSalary = document.getElementById("new-salary");
  const chrgDutyCnt = document.getElementById("new-chrg-duty");
  const brdt = document.getElementById("new-brdt").value;

  // [이름] NotBlank, Size(2~50), 한글·영문만
  const name = nameEl.value.trim();
  if (!name) {
    showHermesToast("이름을 입력해주세요.", "error");
    nameEl.focus();
    return false;
  }
  if (name.length < 2 || name.length > 50) {
    showHermesToast("이름은 2자 이상 50자 이하로 입력해주세요.", "error");
    nameEl.focus();
    return false;
  }
  if (!/^[가-힣a-zA-Z]+$/.test(name)) {
    showHermesToast("이름은 한글 또는 영문만 입력 가능합니다.", "error");
    nameEl.focus();
    return false;
  }

  // [주민등록번호] NotBlank, Pattern ^\d{6}-\d{7}$
  if (!rrnEl.value.trim()) {
    showHermesToast("주민등록번호를 입력해주세요.", "error");
    rrnDisplayEl.focus();
    return false;
  }
  if (!/^\d{6}-\d{7}$/.test(rrnEl.value)) {
    showHermesToast(
      "주민등록번호는 000000-0000000 형식이어야 합니다.",
      "error",
    );
    rrnDisplayEl.focus();
    return false;
  }
  if (!validateRrnChecksum(rrnEl.value.replace(/\D/g, ""))) {
    showHermesToast("유효하지 않은 주민등록번호입니다. 다시 확인해주세요.", "error");
    rrnDisplayEl.focus();
    return false;
  }
  if (!brdt) {
    showHermesToast(
      "주민등록번호에 기재된 날짜 정보가 유효하지 않습니다.",
      "error",
    );
    rrnDisplayEl.focus();
    return false;
  }

  // [연락처] NotBlank, Pattern ^010-\d{3,4}-\d{4}$
  if (!phoneEl.value.trim()) {
    showHermesToast("연락처를 입력해주세요.", "error");
    phoneEl.focus();
    return false;
  }
  if (!/^010-\d{3,4}-\d{4}$/.test(phoneEl.value.trim())) {
    showHermesToast(
      "올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)",
      "error",
    );
    phoneEl.focus();
    return false;
  }

  // [이메일] NotBlank, Email 형식, Size(max=100)
  const email = emailEl.value.trim();
  if (!email) {
    showHermesToast("이메일을 입력해주세요.", "error");
    emailEl.focus();
    return false;
  }
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
    showHermesToast("유효한 이메일 형식이 아닙니다.", "error");
    emailEl.focus();
    return false;
  }
  if (email.length > 100) {
    showHermesToast("이메일은 100자 이하로 입력해주세요.", "error");
    emailEl.focus();
    return false;
  }

  // [우편번호] NotBlank, Size(5~10)
  const zip = postcodeEl.value.trim();
  if (!zip) {
    showHermesToast("우편번호 검색을 해주세요.", "error");
    return false;
  }

  // [주소] NotBlank, Size(max=200)
  const addr = addrEl.value.trim();
  if (!addr) {
    showHermesToast("기본 주소를 입력해주세요.", "error");
    return false;
  }
  if (addr.length > 200) {
    showHermesToast("주소는 200자 이하로 입력해주세요.", "error");
    return false;
  }

  // [소속 부서] 필수
  if (!deptEl.value) {
    showHermesToast("소속 부서를 선택해주세요.", "error");
    deptEl.focus();
    return false;
  }

  // [직급] 필수
  if (!roleEl.value) {
    showHermesToast("직급을 선택해주세요.", "error");
    roleEl.focus();
    return false;
  }

  // [입사일] 필수, 연도 범위
  if (!entryEl.value) {
    showHermesToast("입사일을 입력해주세요.", "error");
    entryEl.focus();
    return false;
  }
  const entryYear = new Date(entryEl.value).getFullYear();
  if (isNaN(entryYear) || entryYear < 2000 || entryYear > 2099) {
    showHermesToast(
      "입사일 연도를 올바르게 입력해주세요. (2000~2099)",
      "error",
    );
    entryEl.focus();
    return false;
  }

  // [근무 유형] 필수
  if (!typeEl.value) {
    showHermesToast("근무 유형을 선택해주세요.", "error");
    typeEl.focus();
    return false;
  }

  // [계약 종료일] 정규직(01) 외에는 필수
  if (typeEl.value !== "01" && !contractEndEl.value) {
    showHermesToast("계약 종료일을 입력해주세요.", "error");
    contractEndEl.focus();
    return false;
  }

  // [로그인 ID] 자동 생성 — 생성 여부만 확인
  if (!loginIdEl.value.trim()) {
    showHermesToast("로그인 ID가 생성되지 않았습니다.", "error");
    return false;
  }

  // [급여] 필수
  if (!baseSalary.value.trim()) {
    showHermesToast("급여(기본급)를 입력해주세요.", "error");
    baseSalary.focus();
    return false;
  }

  // [담당 업무] 필수
  if (!chrgDutyCnt.value.trim()) {
    showHermesToast("담당 업무를 입력해주세요.", "error");
    chrgDutyCnt.focus();
    return false;
  }

  return true;
}

/* ─── 비밀번호 재설정 ─── */
function resetPw(id, name) {
  showHermesToast(name + "에게 임시 비밀번호가 발급되었습니다.", "success");
}

/* ─── 입력 포맷 ─── */
function formatPhone(el) {
  let v = el.value.replace(/\D/g, ""); // 숫자만 남기기
  if (v.length > 11) v = v.slice(0, 11);

  // [보완] 최소 3자리가 입력되었을 때 휴대폰 앞자리 형식이 맞는지 체크
  // 010 외에 012, 015까지 허용하려면 /^(010|012|015)/ 형태로 확장 가능합니다.
  if (v.length >= 3) {
    if (!/^010/.test(v)) {
      showHermesToast("휴대폰 번호는 010으로 시작해야 합니다.", "error");
      // 앞자리가 010이 아니면 입력값을 강제로 010으로 초기화하거나 비워줍니다.
      el.value = "010-";
      return;
    }
  }

  // 하이픈 포맷팅 로직
  if (v.length >= 8) {
    v = v.slice(0, 3) + "-" + v.slice(3, 7) + "-" + v.slice(7);
  } else if (v.length >= 4) {
    v = v.slice(0, 3) + "-" + v.slice(3);
  }
  el.value = v;
}

function formatRrn(el) {
  let v = el.value.replace(/\D/g, "");
  if (v.length > 13) v = v.slice(0, 13);
  if (v.length > 6) v = v.slice(0, 6) + "-" + v.slice(6);
  el.value = v;
}

/* ─── 모달 공통 ─── */
function openModal(id) {
  document.getElementById(id).classList.remove("hidden");
}
function closeModal(id) {
  document.getElementById(id).classList.add("hidden");
}

function showWarningToast(message) {
  let container = document.getElementById("hm-toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "hm-toast-container";
    container.className =
      "fixed bottom-6 right-6 z-50 space-y-3 pointer-events-none";
    document.body.appendChild(container);
  }
  const toast = document.createElement("div");
  toast.className =
    "px-5 py-3.5 rounded-xl shadow-xl text-xs font-bold text-white bg-amber-500 transition-all duration-300 transform translate-y-4 opacity-0 flex items-center gap-3 pointer-events-auto";
  toast.innerHTML =
    '<i class="fa-solid fa-triangle-exclamation text-sm"></i> <span>' +
    message +
    "</span>";
  container.appendChild(toast);
  setTimeout(() => toast.classList.remove("translate-y-4", "opacity-0"), 10);
  setTimeout(() => {
    toast.classList.add("opacity-0", "translate-y-4");
    setTimeout(() => toast.remove(), 300);
  }, 3000);
}
document.querySelectorAll('[id^="modal-"]').forEach((m) => {
  let mousedownOnBackdrop = false;
  m.addEventListener("mousedown", (e) => {
    mousedownOnBackdrop = e.target === m;
  });
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
  const firstBtn = document.querySelector(
    '.emp-tab-btn[data-tab="emp-tab-hr"]',
  );
  if (firstBtn) switchEmpTab("emp-tab-hr", firstBtn);

  // Tailwind CDN 스타일 적용 후 실행되도록 다음 이벤트 루프로 지연
  setTimeout(filterHrList, 0);

  // 등록/수정 결과 토스트 처리
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

/* ─── 신규 등록 폼: 부서 → 직급 필터 ─── */
var allNewRoleOptions = Array.from(
  document.querySelectorAll('#new-role option[value]:not([value=""])'),
).map((opt) => ({
  value: opt.value,
  text: opt.textContent.trim(),
  dept: opt.dataset.dept || "",
}));

function filterNewRoleByDept() {
  const deptCd = document.getElementById("new-dept").value;
  const ts = document.getElementById("new-role").tomselect;
  if (ts) {
    ts.clear(true);
    ts.clearOptions();
    const filtered = deptCd
      ? allNewRoleOptions.filter((o) => o.dept === deptCd)
      : allNewRoleOptions;
    filtered.forEach((o) => ts.addOption({ value: o.value, text: o.text }));
    ts.refreshOptions(false);
  } else {
    const roleSelect = document.getElementById("new-role");
    roleSelect.value = "";
    Array.from(roleSelect.options).forEach((opt) => {
      if (!opt.value) return;
      opt.hidden = deptCd ? opt.dataset.dept !== deptCd : false;
    });
  }
}

document
  .getElementById("new-dept")
  .addEventListener("change", filterNewRoleByDept);

/* ─── 신규 등록 폼 입력 시 파란 배경 토글 ─── */
function syncFilled(el) {
  const wrapper = el.tomselect ? el.tomselect.wrapper : el;
  const val = el.tomselect ? el.tomselect.getValue() : el.value;
  wrapper.classList.toggle("filled", val !== "" && val != null);
}

// select / date / file — change 이벤트로 처리
[
  "new-dept",
  "new-role",
  "new-empl-stat",
  "new-work-type",
  "new-entry-date",
  "new-atch-file",
].forEach((id) => {
  const el = document.getElementById(id);
  if (!el) return;
  el.addEventListener("change", () => syncFilled(el));
});

// new-brdt 는 JS가 value를 직접 세팅하므로 MutationObserver 대신 input 이벤트 + 직접 호출
var brdtEl = document.getElementById("new-brdt");
if (brdtEl) {
  brdtEl.addEventListener("input", () => syncFilled(brdtEl));
  // autoFillTempPw 가 value를 세팅한 직후 호출
  const _origAutoFill = window.autoFillTempPw;
  window.autoFillTempPw = function () {
    if (_origAutoFill) _origAutoFill();
    syncFilled(brdtEl);
  };
}

/* ─── 주민등록번호 마스킹 ─── */
var rrnRealValue = ""; // 실제 값 (하이픈 포함, 예: 900101-1234567)
var rrnEyeOpen = false; // 눈 아이콘 상태

function maskRrn(formatted) {
  const digits = formatted.replace(/\D/g, "");
  if (digits.length <= 6) return formatted;
  return (
    digits.slice(0, 6) +
    "-" +
    digits[6] +
    "*".repeat(Math.max(0, digits.length - 7))
  );
}

function onRrnInput(el) {
  let digits = el.value.replace(/\D/g, "").replace(/\*/g, "");
  if (digits.length > 13) digits = digits.slice(0, 13);
  const formatted =
    digits.length > 6 ? digits.slice(0, 6) + "-" + digits.slice(6) : digits;
  rrnRealValue = formatted;
  document.getElementById("new-rrn").value = formatted;
  el.value = formatted; // 타이핑 중엔 항상 실제값 표시
  autoFillTempPw();
}

function onRrnFocus() {
  document.getElementById("new-rrn-display").value = rrnRealValue;
}

function onRrnBlur() {
  if (!rrnEyeOpen) {
    document.getElementById("new-rrn-display").value = maskRrn(rrnRealValue);
  }
}

function toggleRrnVisibility() {
  rrnEyeOpen = !rrnEyeOpen;
  const icon = document.getElementById("rrn-eye-icon");
  const displayEl = document.getElementById("new-rrn-display");
  icon.className = rrnEyeOpen
    ? "fa-regular fa-eye text-sm"
    : "fa-regular fa-eye-slash text-sm";
  displayEl.value = rrnEyeOpen ? rrnRealValue : maskRrn(rrnRealValue);
}

/* ─── 이름 실시간 포맷 (한글 외 즉시 제거) ─── */
function formatName(el) {
  const before = el.value;
  // ㄱ-ㅎ, ㅏ-ㅣ: IME 조합 중 임시 자모 허용
  el.value = before.replace(/[^가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z]/g, "");
  if (el.value !== before)
    showHermesToast("이름은 한글 또는 영문만 입력 가능합니다.", "error");
}

/* ─── 실시간 blur 검증 ─── */
function blurValidateName(el) {
  const v = el.value.trim();
  if (!v) return;
  if (v.length < 2 || v.length > 50) {
    showHermesToast("이름은 2자 이상 50자 이하로 입력해주세요.", "error");
    return;
  }
  if (!/^[가-힣a-zA-Z]+$/.test(v))
    showHermesToast("이름은 한글 또는 영문만 입력 가능합니다.", "error");
}

function blurValidatePhone(el) {
  const v = el.value.trim();
  if (!v) return;
  if (!/^010-\d{3,4}-\d{4}$/.test(v))
    showHermesToast(
      "올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)",
      "error",
    );
}

function blurValidateEmail(el) {
  const v = el.value.trim();
  if (!v) return;
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v))
    showHermesToast("유효한 이메일 형식이 아닙니다.", "error");
  else if (v.length > 100)
    showHermesToast("이메일은 100자 이하로 입력해주세요.", "error");
}

/* ─── 퇴사 사유 select: 모달 하단에 있어 드롭다운을 위로 펼치게 한다 ───
       TomSelect 라이브러리/전역 init 이후 실행되도록 DOMContentLoaded 에서
       기존 인스턴스의 wrapper 에 ts-dropup 클래스만 부여한다. */
document.addEventListener("DOMContentLoaded", function () {
  const ts =
    document.getElementById("resign-reason") &&
    document.getElementById("resign-reason").tomselect;
  if (ts) ts.wrapper.classList.add("ts-dropup");
});
