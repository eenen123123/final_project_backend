/* ============================================================
   퇴원 방어 및 유지 관리 (manager/retention.html)
   · 대상: 오프라인 학생 (ROLE_STUDENT)
   · 검색/페이징 서버사이드(/admin/retention/*), select은 공통코드
   · 근태는 Excel 업로드, 위험 학생 → 상담 프로세스 진행
   · 학생 검색/수강 강좌(보기)는 상담관리 엔드포인트 재사용
============================================================ */

const RET_BASE = "/admin/retention";
const CON_BASE = "/admin/consultation"; // 학생 검색 / 수강 강좌 재사용

/* ── 공통 유틸 ── */
const esc = (s) =>
  s == null ? "" : String(s).replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
const fmtDt = (s) => (s ? String(s).replace("T", " ").substring(0, 16) : "-");
const fmtD = (s) => (s ? String(s).substring(0, 10) : "-");
function qs(obj) {
  return Object.entries(obj)
    .filter(([, v]) => v != null && v !== "")
    .map(([k, v]) => encodeURIComponent(k) + "=" + encodeURIComponent(v))
    .join("&");
}
function riskBadge(level) {
  const map = { 고위험: "bg-red-50 text-red-600", 중위험: "bg-amber-50 text-amber-600", 저위험: "bg-sky-50 text-sky-600" };
  return `<span class="px-2.5 py-0.5 rounded-full text-xs font-bold ${map[level] || "bg-slate-100 text-slate-500"}">${esc(level)}</span>`;
}
function resultBadge(name) {
  const map = { 유지: "bg-emerald-50 text-emerald-700", 진행중: "bg-violet-50 text-violet-700", 퇴원: "bg-slate-100 text-slate-500" };
  return name ? `<span class="px-2 py-0.5 rounded-full text-xs font-semibold ${map[name] || "bg-slate-100 text-slate-500"}">${esc(name)}</span>` : "-";
}
function attGauge(count, max, color) {
  const pct = Math.min(100, Math.round((count / max) * 100));
  return `<div class="flex items-center gap-2">
    <div class="att-gauge-track"><div class="att-gauge-fill ${color}" style="width:${pct}%"></div></div>
    <span class="text-xs font-bold w-4 text-right">${count}</span>
  </div>`;
}

/* ── 페이징 (블록 5) ── */
function renderPaging(containerId, total, page, screenSize, onMove) {
  const totalPage = Math.max(1, Math.ceil(total / screenSize));
  const block = 5;
  const start = Math.floor((page - 1) / block) * block + 1;
  const end = Math.min(start + block - 1, totalPage);
  const box = document.getElementById(containerId);
  const btn = (label, p, disabled, active) =>
    `<button ${disabled ? "disabled" : ""} onclick="${onMove}(${p})"
      class="w-8 h-8 rounded-lg text-xs flex items-center justify-center ${
        active ? "bg-violet-600 text-white font-bold" : "border border-slate-200 text-slate-400 hover:bg-slate-50"
      } ${disabled ? "opacity-40 cursor-not-allowed" : ""}">${label}</button>`;
  let html = btn('<i class="fa-solid fa-chevron-left"></i>', page - 1, page <= 1, false);
  for (let p = start; p <= end; p++) html += btn(p, p, false, p === page);
  html += btn('<i class="fa-solid fa-chevron-right"></i>', page + 1, page >= totalPage, false);
  box.innerHTML = html;
}

/* ── 탭 전환 ── */
function switchRetTab(tabId, btn) {
  document.querySelectorAll(".ret-tab-panel").forEach((p) => p.classList.add("hidden"));
  document.querySelectorAll(".ret-tab-btn").forEach((b) => b.classList.remove("is-active"));
  document.getElementById(tabId).classList.remove("hidden");
  btn.classList.add("is-active");
}

/* ── 요약 카드 ── */
function loadSummary() {
  fetch(`${RET_BASE}/summary`)
    .then((r) => r.json())
    .then((s) => {
      document.getElementById("sumTotal").textContent = s.totalStudents;
      document.getElementById("sumRisk").textContent = s.riskCnt;
      document.getElementById("sumAttn").textContent = s.attnAbnormalCnt;
      document.getElementById("sumRetained").textContent = s.retainedCnt;
    });
}

/* ── 탭 1: 근태 특이사항 ── */
const ANOMALY_SIZE = 10;
function loadAnomalies(page) {
  const params = {
    keyword: document.getElementById("anoKeyword").value,
    atndTypeCd: document.getElementById("anoType").value,
    riskLevel: document.getElementById("anoRisk").value,
    page: page,
    screenSize: ANOMALY_SIZE,
  };
  fetch(`${RET_BASE}/anomalies?${qs(params)}`)
    .then((r) => r.json())
    .then((d) => {
      const tbody = document.getElementById("anomalyTableBody");
      document.getElementById("anomalyTotal").textContent = d.totalCount;
      if (!d.items.length) {
        tbody.innerHTML = `<tr><td colspan="8" class="py-10 text-center text-slate-300 text-sm">근태 특이사항이 없습니다.</td></tr>`;
      } else {
        tbody.innerHTML = d.items
          .map(
            (r) => `
          <tr class="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
            <td class="py-3 px-3 font-medium text-slate-800">${esc(r.studentNm)}</td>
            <td class="py-3 px-3 text-xs text-slate-500">${esc(r.className) || "-"}</td>
            <td class="py-3 px-3 w-24">${attGauge(r.absentCnt, 7, "bg-red-400")}</td>
            <td class="py-3 px-3 w-24">${attGauge(r.lateCnt, 10, "bg-amber-400")}</td>
            <td class="py-3 px-3 w-24">${attGauge(r.earlyCnt, 5, "bg-sky-400")}</td>
            <td class="py-3 px-3 text-xs text-slate-500 ret-note">${esc(r.lastNote) || "-"}</td>
            <td class="py-3 px-3">${riskBadge(r.riskLevel)}</td>
            <td class="py-3 px-3 whitespace-nowrap">
              <button onclick='openProcessModal(${JSON.stringify({ stdUserId: r.stdUserId, studentNm: r.studentNm }).replace(/'/g, "&#39;")})'
                class="text-violet-500 hover:text-violet-700 text-xs font-semibold mr-2"><i class="fa-solid fa-comments"></i> 상담</button>
              <button onclick="openCoursesModal('${esc(r.stdUserId)}', '${esc(r.studentNm)}')"
                class="text-slate-400 hover:text-violet-600 text-xs font-semibold"><i class="fa-solid fa-book"></i> 보기</button>
            </td>
          </tr>`,
          )
          .join("");
      }
      renderPaging("anomalyPaging", d.totalCount, page, ANOMALY_SIZE, "loadAnomalies");
    });
}

/* ── 탭 2: 상담 프로세스 이력 ── */
const PROCESS_SIZE = 10;
function loadProcesses(page) {
  const params = {
    keyword: document.getElementById("procKeyword").value,
    result: document.getElementById("procResult").value,
    page: page,
    screenSize: PROCESS_SIZE,
  };
  fetch(`${RET_BASE}/processes?${qs(params)}`)
    .then((r) => r.json())
    .then((d) => {
      const tbody = document.getElementById("processTableBody");
      document.getElementById("processTotal").textContent = d.totalCount;
      if (!d.items.length) {
        tbody.innerHTML = `<tr><td colspan="7" class="py-10 text-center text-slate-300 text-sm">상담 이력이 없습니다.</td></tr>`;
      } else {
        tbody.innerHTML = d.items
          .map(
            (r) => `
          <tr class="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
            <td class="py-3 px-3 font-medium text-slate-800">${esc(r.studentNm)}</td>
            <td class="py-3 px-3 text-xs text-slate-500">${esc(r.className) || "-"}</td>
            <td class="py-3 px-3 text-sm text-slate-600">${esc(r.wdrwRsnNm) || "-"}</td>
            <td class="py-3 px-3 text-center"><span class="px-2.5 py-0.5 bg-violet-50 text-violet-700 rounded-full text-xs font-bold">${r.cnslCnt}회</span></td>
            <td class="py-3 px-3 text-sm text-slate-500">${fmtD(r.lastDt)}</td>
            <td class="py-3 px-3 text-sm text-slate-600">${esc(r.chrgNm) || "-"}</td>
            <td class="py-3 px-3">${resultBadge(r.rtnpRsltNm)}</td>
          </tr>`,
          )
          .join("");
      }
      renderPaging("processPaging", d.totalCount, page, PROCESS_SIZE, "loadProcesses");
    });
}

/* ── 상담 프로세스 추가 모달 ── */
function openProcessModal(student) {
  document.getElementById("procModalStudentId").value = student && student.stdUserId ? student.stdUserId : "";
  document.getElementById("procModalStudentNm").value = student && student.studentNm ? student.studentNm : "";
  document.getElementById("procModalContent").value = "";
  document.getElementById("processModal").classList.remove("hidden");
}
function closeProcessModal() {
  document.getElementById("processModal").classList.add("hidden");
}
function saveProcessRecord() {
  const stdUserId = document.getElementById("procModalStudentId").value;
  const rtnpCn = document.getElementById("procModalContent").value.trim();
  if (!stdUserId) {
    showHermesToast("학생을 선택해 주세요.", "error");
    return;
  }
  if (!rtnpCn) {
    showHermesToast("상담 내용을 입력해 주세요.", "error");
    return;
  }
  const params = {
    stdUserId: stdUserId,
    wdrwRsnCd: document.getElementById("procModalReason").value,
    rtnpRsltCd: document.getElementById("procModalResult").value,
    rtnpCn: rtnpCn,
  };
  fetch(`${RET_BASE}/processes`, {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: qs(params),
  })
    .then((r) => r.json().then((d) => ({ ok: r.ok, d })))
    .then(({ ok, d }) => {
      if (!ok) {
        showHermesToast(d.error || "저장에 실패했습니다.", "error");
        return;
      }
      showHermesToast("상담 이력이 저장되었습니다.", "success");
      closeProcessModal();
      loadSummary();
      loadProcesses(1);
      refreshCalendar();
    })
    .catch(() => showHermesToast("저장 중 오류가 발생했습니다.", "error"));
}

/* ── 학생 검색 모달 (상담관리 /students 재사용) ── */
const STU_SIZE = 8;
function openStudentSearch() {
  document.getElementById("stuSearchKeyword").value = "";
  document.getElementById("studentSearchModal").classList.remove("hidden");
  loadStudentSearch(1);
}
function closeStudentSearch() {
  document.getElementById("studentSearchModal").classList.add("hidden");
}
function loadStudentSearch(page) {
  const params = { keyword: document.getElementById("stuSearchKeyword").value, page: page, screenSize: STU_SIZE };
  fetch(`${CON_BASE}/students?${qs(params)}`)
    .then((r) => r.json())
    .then((d) => {
      const tbody = document.getElementById("stuSearchBody");
      document.getElementById("stuSearchTotal").textContent = d.totalCount;
      if (!d.items.length) {
        tbody.innerHTML = `<tr><td colspan="3" class="py-8 text-center text-slate-300 text-sm">검색 결과가 없습니다.</td></tr>`;
      } else {
        tbody.innerHTML = d.items
          .map(
            (s) => `
          <tr class="border-t border-slate-50 hover:bg-violet-50/40 transition-colors">
            <td class="py-2.5 px-3 font-medium text-slate-800">${esc(s.studentNm)}</td>
            <td class="py-2.5 px-3 text-slate-500 text-xs">${esc(s.className) || "-"}</td>
            <td class="py-2.5 px-3 text-right">
              <button class="text-xs font-semibold text-violet-600 hover:text-violet-800"
                onclick="pickStudent('${esc(s.stdUserId)}', '${esc(s.studentNm)}')">선택</button>
            </td>
          </tr>`,
          )
          .join("");
      }
      renderPaging("stuSearchPaging", d.totalCount, page, STU_SIZE, "loadStudentSearch");
    });
}
function pickStudent(stdUserId, studentNm) {
  document.getElementById("procModalStudentId").value = stdUserId;
  document.getElementById("procModalStudentNm").value = studentNm;
  closeStudentSearch();
}

/* ── 수강 강좌 보기 모달 (상담관리 courses 재사용) ── */
function openCoursesModal(stdUserId, studentNm) {
  document.getElementById("coursesModalTitle").textContent = `${studentNm} · 수강 강좌`;
  const box = document.getElementById("coursesModalBody");
  box.innerHTML = `<p class="text-xs text-slate-300 py-4 text-center">불러오는 중...</p>`;
  document.getElementById("coursesModal").classList.remove("hidden");
  fetch(`${CON_BASE}/student/${encodeURIComponent(stdUserId)}/courses`)
    .then((r) => r.json())
    .then((list) => {
      if (!list.length) {
        box.innerHTML = `<p class="text-xs text-slate-300 py-4 text-center">수강 중인 강좌가 없습니다.</p>`;
        return;
      }
      box.innerHTML =
        `<div class="flex flex-wrap gap-2">` +
        list
          .map(
            (cr) => `
          <div class="px-3 py-2 bg-slate-50 border border-slate-100 rounded-lg">
            <p class="text-xs font-semibold text-slate-700">${esc(cr.courseNm) || "-"}</p>
            ${cr.instrNm ? `<p class="text-[11px] text-slate-400 mt-0.5">${esc(cr.instrNm)} 강사</p>` : ""}
          </div>`,
          )
          .join("") +
        `</div>`;
    })
    .catch(() => (box.innerHTML = `<p class="text-xs text-slate-300 py-4 text-center">강좌 정보를 불러오지 못했습니다.</p>`));
}
function closeCoursesModal() {
  document.getElementById("coursesModal").classList.add("hidden");
}

/* ── 근태 Excel 업로드 ── */
function uploadAttendance(input) {
  if (!input.files.length) return;
  const fd = new FormData();
  fd.append("file", input.files[0]);
  showHermesToast("업로드 중입니다...", "info");
  fetch(`${RET_BASE}/attendance/upload`, { method: "POST", body: fd })
    .then((r) => r.json().then((d) => ({ ok: r.ok, d })))
    .then(({ ok, d }) => {
      input.value = "";
      if (!ok) {
        showHermesToast(d.error || "업로드에 실패했습니다.", "error");
        return;
      }
      let msg = `근태 ${d.inserted}건 등록`;
      if (d.failed) msg += ` · 실패 ${d.failed}건`;
      showHermesToast(msg, d.failed ? "warning" : "success");
      loadSummary();
      loadAnomalies(1);
    })
    .catch(() => {
      input.value = "";
      showHermesToast("업로드 중 오류가 발생했습니다.", "error");
    });
}

/* ── 상담 프로세스 캘린더 (커스텀 월 그리드) ── */
let retCalYear = new Date().getFullYear();
let retCalMonth = new Date().getMonth();
let retPickerYear = retCalYear;
let retEventsByDate = {};

function pad2(n) {
  return String(n).padStart(2, "0");
}
function ymd(y, m, d) {
  return y + "-" + pad2(m + 1) + "-" + pad2(d);
}
function rsltClass(name) {
  return name === "유지" ? "rslt-retained" : name === "퇴원" ? "rslt-left" : "rslt-ongoing";
}

function initCalendar() {
  document.addEventListener("click", (e) => {
    const picker = document.getElementById("ret-month-picker");
    const btn = document.getElementById("ret-month-picker-btn");
    if (picker && btn && !picker.contains(e.target) && !btn.contains(e.target)) picker.classList.add("hidden");
  });
  loadCalendar();
}
function loadCalendar() {
  const firstDow = new Date(retCalYear, retCalMonth, 1).getDay();
  const gridStart = new Date(retCalYear, retCalMonth, 1 - firstDow);
  const gridEnd = new Date(gridStart);
  gridEnd.setDate(gridEnd.getDate() + 42);
  const startStr = ymd(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate());
  const endStr = ymd(gridEnd.getFullYear(), gridEnd.getMonth(), gridEnd.getDate());
  fetch(`${RET_BASE}/calendar?start=${startStr}&end=${endStr}`)
    .then((r) => r.json())
    .then((list) => {
      retEventsByDate = {};
      list.forEach((c) => {
        const d = (c.rtnpDt || "").substring(0, 10);
        if (!d) return;
        (retEventsByDate[d] = retEventsByDate[d] || []).push(c);
      });
      renderCalendar();
    });
}
function refreshCalendar() {
  loadCalendar();
}
function renderCalendar() {
  document.getElementById("ret-cal-year").textContent = retCalYear;
  document.getElementById("ret-cal-month").textContent = pad2(retCalMonth + 1);

  const today = new Date();
  const todayStr = ymd(today.getFullYear(), today.getMonth(), today.getDate());
  const firstDow = new Date(retCalYear, retCalMonth, 1).getDay();
  const daysInMonth = new Date(retCalYear, retCalMonth + 1, 0).getDate();
  const daysInPrev = new Date(retCalYear, retCalMonth, 0).getDate();

  const cells = [];
  for (let i = firstDow - 1; i >= 0; i--)
    cells.push({ day: daysInPrev - i, year: retCalMonth === 0 ? retCalYear - 1 : retCalYear, month: retCalMonth === 0 ? 11 : retCalMonth - 1, other: true });
  for (let d = 1; d <= daysInMonth; d++) cells.push({ day: d, year: retCalYear, month: retCalMonth, other: false });
  const rem = 42 - cells.length;
  for (let d = 1; d <= rem; d++)
    cells.push({ day: d, year: retCalMonth === 11 ? retCalYear + 1 : retCalYear, month: retCalMonth === 11 ? 0 : retCalMonth + 1, other: true });

  const grid = document.getElementById("ret-cal-grid");
  grid.innerHTML = "";
  cells.forEach((cell, idx) => {
    const ds = ymd(cell.year, cell.month, cell.day);
    const isSun = idx % 7 === 0;
    const isSat = idx % 7 === 6;
    const isToday = ds === todayStr;
    const dayEvents = cell.other ? [] : retEventsByDate[ds] || [];

    const div = document.createElement("div");
    div.className = "cal-cell border-r border-b border-slate-100 p-1.5 flex flex-col gap-0.5 transition-colors";
    if (cell.other) div.classList.add("other-month");
    else {
      if (isSun) div.classList.add("sun-bg");
      if (isToday) div.classList.add("today-cell");
    }

    const dateDiv = document.createElement("div");
    dateDiv.className = "flex items-center gap-1 mb-0.5";
    const span = document.createElement("span");
    span.className = "text-xs font-medium";
    span.textContent = cell.day;
    if (cell.other) span.classList.add("text-slate-300");
    else if (isSun) span.classList.add("text-red-400");
    else if (isSat) span.classList.add("text-blue-400");
    else span.classList.add("text-slate-700");
    dateDiv.appendChild(span);
    if (isToday && !cell.other) {
      const tb = document.createElement("span");
      tb.className = "text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-violet-500 text-white leading-none";
      tb.textContent = "Today";
      dateDiv.appendChild(tb);
    }
    div.appendChild(dateDiv);

    dayEvents.slice(0, 3).forEach((ev) => {
      const bar = document.createElement("div");
      bar.className = "event-bar " + rsltClass(ev.rtnpRsltNm);
      const label = (ev.studentNm || "") + (ev.rtnpRsltNm ? " " + ev.rtnpRsltNm : "");
      bar.textContent = label.length > 14 ? label.slice(0, 14) + "..." : label;
      bar.title = label;
      div.appendChild(bar);
    });
    if (dayEvents.length > 3) {
      const more = document.createElement("span");
      more.className = "text-[10px] text-slate-400";
      more.textContent = "+" + (dayEvents.length - 3) + "건";
      div.appendChild(more);
    }
    grid.appendChild(div);
  });
}
function retCalPrev() {
  if (retCalMonth === 0) {
    retCalYear--;
    retCalMonth = 11;
  } else retCalMonth--;
  loadCalendar();
}
function retCalNext() {
  if (retCalMonth === 11) {
    retCalYear++;
    retCalMonth = 0;
  } else retCalMonth++;
  loadCalendar();
}
function retCalToday() {
  const t = new Date();
  retCalYear = t.getFullYear();
  retCalMonth = t.getMonth();
  loadCalendar();
}
function retTogglePicker() {
  const p = document.getElementById("ret-month-picker");
  p.classList.toggle("hidden");
  retPickerYear = retCalYear;
  retRenderPickerMonths();
}
function retRenderPickerMonths() {
  document.getElementById("ret-picker-year").textContent = retPickerYear;
  const c = document.getElementById("ret-picker-months");
  c.innerHTML = "";
  for (let m = 0; m < 12; m++) {
    const b = document.createElement("button");
    b.textContent = m + 1 + "월";
    b.className =
      "py-1.5 rounded-lg text-xs font-medium transition-colors cursor-pointer " +
      (retPickerYear === retCalYear && m === retCalMonth ? "bg-violet-600 text-white" : "hover:bg-slate-100 text-slate-700");
    b.onclick = () => {
      retCalYear = retPickerYear;
      retCalMonth = m;
      document.getElementById("ret-month-picker").classList.add("hidden");
      loadCalendar();
    };
    c.appendChild(b);
  }
}
function retPickerPrevYear() {
  retPickerYear--;
  retRenderPickerMonths();
}
function retPickerNextYear() {
  retPickerYear++;
  retRenderPickerMonths();
}

/* ── 초기화 ── */
document.addEventListener("DOMContentLoaded", function () {
  loadCommonCodes("anoType", "226", "전체 유형");
  loadCommonCodes("procResult", "227", "전체 결과");
  loadCommonCodes("procModalReason", "203");
  loadCommonCodes("procModalResult", "227");

  initCalendar();
  loadSummary();
  loadAnomalies(1);
  loadProcesses(1);
  switchRetTab("tab-attendance", document.querySelector('.ret-tab-btn[data-tab="tab-attendance"]'));
});
