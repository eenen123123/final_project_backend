/* ─── 탭 전환 ─── */
function switchHlTab(tabId, btn) {
  document.querySelectorAll(".hl-tab-btn").forEach(b => b.classList.remove("active"));
  document.querySelectorAll(".hl-tab-panel").forEach(p => p.classList.remove("active"));
  btn.classList.add("active");
  document.getElementById(tabId).classList.add("active");
  if (tabId === "hl-tab-balance" && !balanceLoaded) { balanceLoaded = true; doBalanceSearch(1); }
}

/* ─── 공통: 페이지네이션 렌더 ─── */
var HL_SCREEN_SIZE = 10;
var HL_BLOCK_SIZE = 5;

function renderPager(containerId, infoId, totalCount, page, goFnName) {
  const box  = document.getElementById(containerId);
  const info = document.getElementById(infoId);
  const totalPages = Math.max(1, Math.ceil(totalCount / HL_SCREEN_SIZE));
  const start = totalCount === 0 ? 0 : (page - 1) * HL_SCREEN_SIZE + 1;
  const end   = Math.min(page * HL_SCREEN_SIZE, totalCount);
  info.textContent = `총 ${totalCount}건 중 ${start}-${end}`;

  const blockStart = Math.floor((page - 1) / HL_BLOCK_SIZE) * HL_BLOCK_SIZE + 1;
  const blockEnd   = Math.min(blockStart + HL_BLOCK_SIZE - 1, totalPages);
  let html = "";
  html += `<button class="emp-page-btn" ${page <= 1 ? "disabled" : ""} onclick="${goFnName}(${page - 1})"><i class="fa-solid fa-angle-left"></i></button>`;
  for (let p = blockStart; p <= blockEnd; p++) {
    html += `<button class="emp-page-btn ${p === page ? "active" : ""}" onclick="${goFnName}(${p})">${p}</button>`;
  }
  html += `<button class="emp-page-btn" ${page >= totalPages ? "disabled" : ""} onclick="${goFnName}(${page + 1})"><i class="fa-solid fa-angle-right"></i></button>`;
  box.innerHTML = html;
}

function fmtYmd(s) {
  if (!s || s.length < 8) return s || "-";
  return s.substring(0,4) + "-" + s.substring(4,6) + "-" + s.substring(6,8);
}
function fmtDays(d) {
  if (d == null) return "-";
  const n = Number(d);
  return (Number.isInteger(n) ? n : n.toFixed(1)) + "일";
}

/* ════════ 탭1: 휴가 현황 (서버 페이징/필터) ════════ */
var lvPage = 1, lvSortCol = null, lvSortAsc = false, lvDebounce = null;

function filterLeave() { clearTimeout(lvDebounce); lvDebounce = setTimeout(() => doLeaveSearch(1), 300); }
function sortLeave(col) {
  if (lvSortCol === col) lvSortAsc = !lvSortAsc; else { lvSortCol = col; lvSortAsc = true; }
  doLeaveSearch(1);
}
function goLeavePage(p) { doLeaveSearch(p); }

async function doLeaveSearch(page) {
  lvPage = page || 1;
  const params = new URLSearchParams();
  const kw = document.getElementById("lv-search").value.trim();
  const ty = document.getElementById("lv-type").value;
  const dp = document.getElementById("lv-dept").value;
  const yr = document.getElementById("lv-year").value;
  const mo = document.getElementById("lv-month").value;
  if (kw) params.set("keyword", kw);
  if (ty) params.set("type", ty);
  if (dp) params.set("deptCd", dp);
  if (yr) params.set("year", yr);
  if (mo) params.set("month", mo);
  if (lvSortCol) { params.set("orderBy", lvSortCol); params.set("orderDirection", lvSortAsc ? "ASC" : "DESC"); }
  params.set("page", lvPage);
  params.set("screenSize", HL_SCREEN_SIZE);

  try {
    const res = await fetch("/admin/hr/leave/search?" + params);
    const data = await res.json();
    renderLeaveTable(data.items, data.totalCount);
  } catch (e) { console.error("휴가 현황 조회 실패:", e); }
}

function renderLeaveTable(items, totalCount) {
  const tbody = document.getElementById("lv-table-body");
  if (!items || items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-12 text-sm text-slate-400">조회된 휴가 내역이 없습니다.</td></tr>';
    renderPager("lv-pagination", "lv-page-info", 0, 1, "goLeavePage");
    return;
  }
  tbody.innerHTML = items.map(it => `
    <tr class="hover:bg-slate-50 transition-colors">
      <td class="py-3 px-4 font-semibold text-slate-800 truncate">${escHtml(it.userName || "-")}</td>
      <td class="py-3 px-4 text-slate-600 truncate">${escHtml(it.deptNm || "-")}</td>
      <td class="py-3 px-4 text-slate-600 truncate">${escHtml(it.jbgrNm || "-")}</td>
      <td class="py-3 px-4"><span class="type-badge">${escHtml(it.annTypeNm || it.annTypeCd || "-")}</span></td>
      <td class="py-3 px-4 text-slate-600 whitespace-nowrap">${fmtYmd(it.annStrtYmd)} ~ ${fmtYmd(it.annEndYmd)}</td>
      <td class="py-3 px-4 text-slate-700 font-semibold">${fmtDays(it.annReqDays)}</td>
      <td class="py-3 px-4 text-slate-500 truncate" title="${escHtml(it.annRsnCn || '')}">${escHtml(it.annRsnCn || "-")}</td>
    </tr>`).join("");
  renderPager("lv-pagination", "lv-page-info", totalCount, lvPage, "goLeavePage");
}

function resetLeaveFilter() {
  document.getElementById("lv-search").value = "";
  document.getElementById("lv-month").value = "";
  ["lv-type", "lv-dept", "lv-year"].forEach(id => {
    const el = document.getElementById(id);
    if (el.customSelect) el.customSelect.setValue(id === "lv-year" ? String(new Date().getFullYear()) : "");
    else el.value = id === "lv-year" ? String(new Date().getFullYear()) : "";
  });
  lvSortCol = null; lvSortAsc = false;
  doLeaveSearch(1);
}

/* ════════ 탭2: 잔여 연차 현황 (서버 페이징/필터) ════════ */
var balPage = 1, balSortCol = null, balSortAsc = true, balDebounce = null, balanceLoaded = false;

function filterBalance() { clearTimeout(balDebounce); balDebounce = setTimeout(() => doBalanceSearch(1), 300); }
function sortBalance(col) {
  if (balSortCol === col) balSortAsc = !balSortAsc; else { balSortCol = col; balSortAsc = true; }
  doBalanceSearch(1);
}
function goBalancePage(p) { doBalanceSearch(p); }

async function doBalanceSearch(page) {
  balPage = page || 1;
  const params = new URLSearchParams();
  const kw = document.getElementById("bal-search").value.trim();
  const dp = document.getElementById("bal-dept").value;
  const yr = document.getElementById("bal-year").value;
  if (kw) params.set("keyword", kw);
  if (dp) params.set("deptCd", dp);
  if (yr) params.set("year", yr);
  if (balSortCol) { params.set("orderBy", balSortCol); params.set("orderDirection", balSortAsc ? "ASC" : "DESC"); }
  params.set("page", balPage);
  params.set("screenSize", HL_SCREEN_SIZE);

  try {
    const res = await fetch("/admin/hr/leave/balance?" + params);
    const data = await res.json();
    renderBalanceTable(data.items, data.totalCount);
  } catch (e) { console.error("잔여 연차 조회 실패:", e); }
}

function renderBalanceTable(items, totalCount) {
  const tbody = document.getElementById("bal-table-body");
  if (!items || items.length === 0) {
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-12 text-sm text-slate-400">조회된 직원이 없습니다.</td></tr>';
    renderPager("bal-pagination", "bal-page-info", 0, 1, "goBalancePage");
    return;
  }
  tbody.innerHTML = items.map(it => {
    const total = Number(it.totalDays || 0);
    const used  = Number(it.usedDays || 0);
    const remain = Number(it.remainDays != null ? it.remainDays : total - used);
    const pct = total > 0 ? Math.min(100, Math.round(used / total * 100)) : 0;
    const remainCls = remain <= 0 ? "text-red-600" : (remain <= 3 ? "text-amber-600" : "text-slate-800");
    const barCls = remain <= 0 ? "bg-red-500" : (remain <= 3 ? "bg-amber-500" : "bg-blue-500");
    return `
    <tr class="hover:bg-slate-50 transition-colors">
      <td class="py-3 px-4 font-semibold text-slate-800 truncate">${escHtml(it.userName || "-")}</td>
      <td class="py-3 px-4 text-slate-600 truncate">${escHtml(it.deptNm || "-")}</td>
      <td class="py-3 px-4 text-slate-600 truncate">${escHtml(it.jbgrNm || "-")}</td>
      <td class="py-3 px-4 text-slate-700">${fmtDays(total)}</td>
      <td class="py-3 px-4 text-slate-700">${fmtDays(used)}</td>
      <td class="py-3 px-4 font-bold ${remainCls}">${fmtDays(remain)}</td>
      <td class="py-3 px-4">
        <div class="flex items-center gap-2">
          <div class="leave-bar-track flex-1"><div class="leave-bar-fill ${barCls}" style="width:${pct}%"></div></div>
          <span class="text-xs text-slate-400 w-9 text-right">${pct}%</span>
        </div>
      </td>
    </tr>`;
  }).join("");
  renderPager("bal-pagination", "bal-page-info", totalCount, balPage, "goBalancePage");
}

/* ─── 연도 셀렉트 채우기 (현재년 포함 최근 4개) ─── */
function fillYearSelect(id) {
  const el = document.getElementById(id);
  if (!el) return;
  const now = new Date().getFullYear();
  let html = "";
  for (let y = now; y >= now - 3; y--) html += `<option value="${y}" ${y === now ? "selected" : ""}>${y}년</option>`;
  el.innerHTML = html;
  // 이미 admin-core 가 커스텀셀렉트로 감쌌다면 옵션 변경을 반영
  if (el.customSelect && el.customSelect.refresh) el.customSelect.refresh();
}

/* ─── 초기화 ─── */
// admin-core.js(defer)·select 자동 래핑·initDeferredSelects 정의가 모두 끝난 load 시점에 실행
window.addEventListener("load", function () {
  fillYearSelect("lv-year");
  fillYearSelect("bal-year");
  // 공통코드 기반 select(휴가 유형 cl=221) 로드 + 커스텀셀렉트 초기화 후 첫 조회
  if (window.initDeferredSelects) {
    Promise.resolve(initDeferredSelects()).then(() => doLeaveSearch(1));
  } else {
    doLeaveSearch(1);
  }
});
