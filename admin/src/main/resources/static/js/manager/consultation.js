/* ============================================================
   학부모 상담 관리 (manager/consultation.html)
   · 검색/페이징은 서버사이드(/admin/consultation/*)
   · select 옵션은 공통코드(211 유형 / 212 상태) — loadCommonCodes 사용
   · 학생 선택은 모달 검색, 학부모명은 자동 매칭
============================================================ */

const CON_BASE = "/admin/consultation";

/* ── 공통 유틸 ── */
const esc = (s) =>
  s == null
    ? ""
    : String(s).replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
const fmtDt = (s) => (s ? String(s).replace("T", " ").substring(0, 16) : "-");
function qs(obj) {
  return Object.entries(obj)
    .filter(([, v]) => v != null && v !== "")
    .map(([k, v]) => encodeURIComponent(k) + "=" + encodeURIComponent(v))
    .join("&");
}
function typeBadge(name) {
  const map = { 대면: "bg-emerald-50 text-emerald-700", 전화: "bg-blue-50 text-blue-700", 문자: "bg-slate-100 text-slate-600" };
  return name
    ? `<span class="px-2 py-0.5 rounded-full text-xs font-medium ${map[name] || "bg-slate-100 text-slate-500"}">${esc(name)}</span>`
    : "-";
}
function statusBadge(name) {
  const map = {
    예정: "bg-sky-50 text-sky-700",
    완료: "bg-emerald-50 text-emerald-700",
    "후속 조치": "bg-amber-50 text-amber-700",
    취소: "bg-slate-100 text-slate-500",
  };
  return name
    ? `<span class="px-2 py-0.5 rounded-full text-xs font-semibold ${map[name] || "bg-slate-100 text-slate-500"}">${esc(name)}</span>`
    : "-";
}
function statColor(name) {
  return name === "완료" ? "#10b981" : name === "예정" ? "#0ea5e9" : name === "후속 조치" ? "#f59e0b" : "#94a3b8";
}
function dotColor(name) {
  return "background-color:" + statColor(name);
}

/* ── 페이징 버튼 렌더 (블록 5) ── */
function renderPaging(containerId, total, page, screenSize, onMove) {
  const totalPage = Math.max(1, Math.ceil(total / screenSize));
  const block = 5;
  const start = Math.floor((page - 1) / block) * block + 1;
  const end = Math.min(start + block - 1, totalPage);
  const box = document.getElementById(containerId);
  const btn = (label, p, disabled, active) =>
    `<button ${disabled ? "disabled" : ""} onclick="${onMove}(${p})"
      class="w-8 h-8 rounded-lg text-xs flex items-center justify-center ${
        active ? "bg-emerald-600 text-white font-bold" : "border border-slate-200 text-slate-400 hover:bg-slate-50"
      } ${disabled ? "opacity-40 cursor-not-allowed" : ""}">${label}</button>`;
  let html = btn('<i class="fa-solid fa-chevron-left"></i>', page - 1, page <= 1, false);
  for (let p = start; p <= end; p++) html += btn(p, p, false, p === page);
  html += btn('<i class="fa-solid fa-chevron-right"></i>', page + 1, page >= totalPage, false);
  box.innerHTML = html;
}

/* ── 탭 전환 ── */
function switchConTab(tabId, btn) {
  document.querySelectorAll(".con-tab-panel").forEach((p) => p.classList.add("hidden"));
  document.querySelectorAll(".con-tab-btn").forEach((b) => b.classList.remove("is-active"));
  document.getElementById(tabId).classList.remove("hidden");
  btn.classList.add("is-active");
}
function goWriteTab() {
  switchConTab("tab-write", document.querySelector('.con-tab-btn[data-tab="tab-write"]'));
}

/* ── 요약 카드 ── */
function loadSummary() {
  fetch(`${CON_BASE}/summary`)
    .then((r) => r.json())
    .then((s) => {
      document.getElementById("sumMonth").textContent = s.monthCnt;
      document.getElementById("sumScheduled").textContent = s.scheduledCnt;
      document.getElementById("sumDone").textContent = s.doneCnt;
      document.getElementById("sumFollowup").textContent = s.followupCnt;
    });
}

/* ── 탭 1: 전체 상담 이력 ── */
const HISTORY_SIZE = 10;
function loadHistory(page) {
  const params = {
    keyword: document.getElementById("historyKeyword").value,
    cnslStatCd: document.getElementById("historyStat").value,
    cnslTypeCd: document.getElementById("historyType").value,
    page: page,
    screenSize: HISTORY_SIZE,
  };
  fetch(`${CON_BASE}/list?${qs(params)}`)
    .then((r) => r.json())
    .then((d) => {
      const tbody = document.getElementById("historyTableBody");
      document.getElementById("historyTotal").textContent = d.totalCount;
      if (!d.items.length) {
        tbody.innerHTML = `<tr><td colspan="8" class="py-10 text-center text-slate-300 text-sm">상담 이력이 없습니다.</td></tr>`;
      } else {
        tbody.innerHTML = d.items
          .map(
            (c) => `
          <tr class="border-b border-slate-50 hover:bg-slate-50/50 transition-colors">
            <td class="py-3 px-3 text-slate-600 whitespace-nowrap">${fmtDt(c.cnslDt)}</td>
            <td class="py-3 px-3 font-medium text-slate-800">${esc(c.studentNm)}${c.targetType === "신규문의" ? '<span class="target-badge">신규문의</span>' : ""}</td>
            <td class="py-3 px-3 text-slate-600">${esc(c.parentNm) || "-"}</td>
            <td class="py-3 px-3 text-slate-500 text-xs">${esc(c.chrgNm) || "-"}</td>
            <td class="py-3 px-3">${typeBadge(c.cnslTypeNm)}</td>
            <td class="py-3 px-3">${statusBadge(c.cnslStatNm)}</td>
            <td class="py-3 px-3 text-slate-500 text-xs con-summary">${esc(c.cnslSmry) || "-"}</td>
            <td class="py-3 px-3">
              <button onclick="openDetailModal(${c.cnslSn})" class="text-emerald-500 hover:text-emerald-700 text-xs font-semibold whitespace-nowrap">
                <i class="fa-solid fa-eye"></i> 보기
              </button>
            </td>
          </tr>`,
          )
          .join("");
      }
      renderPaging("historyPaging", d.totalCount, page, HISTORY_SIZE, "loadHistory");
    });
}

/* ── 탭 2: 학생별 조회 ── */
let selectedStudent = null;
function loadStudentHistory(stdUserId) {
  fetch(`${CON_BASE}/student/${encodeURIComponent(stdUserId)}`)
    .then((r) => r.json())
    .then((list) => {
      document.getElementById("studentDetailEmpty").classList.add("hidden");
      document.getElementById("studentDetailPanel").classList.remove("hidden");
      const s = selectedStudent || {};
      document.getElementById("detailStudentAvatar").textContent = (s.studentNm || "-").charAt(0);
      document.getElementById("detailStudentName").textContent = s.studentNm || "-";
      document.getElementById("detailStudentSub").textContent =
        [s.className, s.enrlSchlNm].filter(Boolean).join(" · ") || "-";
      document.getElementById("detailTotalCount").textContent = list.length;

      const timeline = document.getElementById("detailTimeline");
      if (!list.length) {
        timeline.innerHTML = `<p class="text-xs text-slate-300 text-center py-6">상담 이력이 없습니다.</p>`;
        return;
      }
      timeline.innerHTML = list
        .map(
          (c) => `
        <div class="con-timeline-item">
          <div class="con-timeline-dot" style="${dotColor(c.cnslStatNm)}"></div>
          <div class="bg-white border border-slate-100 rounded-xl p-3 hover:border-emerald-200 cursor-pointer transition-colors" onclick="openDetailModal(${c.cnslSn})">
            <div class="flex items-center justify-between mb-1">
              <span class="text-xs font-semibold text-slate-600">${fmtDt(c.cnslDt)}</span>
              <div class="flex gap-1">${typeBadge(c.cnslTypeNm)} ${statusBadge(c.cnslStatNm)}</div>
            </div>
            <p class="text-xs text-slate-700 font-medium">${esc(c.cnslSmry) || esc((c.cnslCn || "").substring(0, 60))}</p>
            ${c.fllwUpCn ? `<p class="text-xs text-amber-600 mt-1"><i class="fa-solid fa-arrow-right mr-1"></i>${esc(c.fllwUpCn)}</p>` : ""}
          </div>
        </div>`,
        )
        .join("");
    });
}
function writeForStudent() {
  if (!selectedStudent) return;
  applyStudent(selectedStudent);
  goWriteTab();
}

/* ── 학생 검색 모달 (공용) ── */
let studentPickTarget = "write"; // 'write' | 'student'
const STU_SIZE = 8;
function openStudentSearch(target) {
  studentPickTarget = target;
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
        tbody.innerHTML = `<tr><td colspan="4" class="py-8 text-center text-slate-300 text-sm">검색 결과가 없습니다.</td></tr>`;
      } else {
        tbody.innerHTML = d.items
          .map(
            (s) => `
          <tr class="border-t border-slate-50 hover:bg-emerald-50/40 transition-colors">
            <td class="py-2.5 px-3 font-medium text-slate-800">${esc(s.studentNm)}</td>
            <td class="py-2.5 px-3 text-slate-500 text-xs">${esc(s.className) || "-"}</td>
            <td class="py-2.5 px-3 text-slate-500 text-xs">${esc(s.parentNm) || "-"}</td>
            <td class="py-2.5 px-3 text-right">
              <button class="text-xs font-semibold text-emerald-600 hover:text-emerald-800"
                onclick='pickStudent(${JSON.stringify(s).replace(/'/g, "&#39;")})'>선택</button>
            </td>
          </tr>`,
          )
          .join("");
      }
      renderPaging("stuSearchPaging", d.totalCount, page, STU_SIZE, "loadStudentSearch");
    });
}
function pickStudent(s) {
  selectedStudent = s;
  closeStudentSearch();
  if (studentPickTarget === "write") {
    applyStudent(s);
  } else {
    loadStudentHistory(s.stdUserId);
  }
}
/* 작성 폼에 학생 + 학부모 자동 매칭 반영 */
function applyStudent(s) {
  document.getElementById("writeStudentId").value = s.stdUserId;
  document.getElementById("writeStudentNm").value = s.studentNm || "";
  document.getElementById("writeParentNm").value = s.parentNm || "(매칭된 학부모 없음)";
}

/* ── 상담 대상 구분 (재원생 / 신규 문의) ── */
let writeMode = "member";
function setWriteMode(mode) {
  writeMode = mode;
  const isMember = mode === "member";
  document.getElementById("memberBlock").classList.toggle("hidden", !isMember);
  document.getElementById("prospectBlock").classList.toggle("hidden", isMember);
  document.getElementById("modeMemberBtn").classList.toggle("is-active", isMember);
  document.getElementById("modeProspectBtn").classList.toggle("is-active", !isMember);
  // 신규 문의는 전화 1차 상담이 기본 → 유형을 전화(02)로 기본 선택
  if (!isMember) {
    const typeSel = document.getElementById("writeType");
    if (typeSel) {
      typeSel.value = "02";
      if (window.initCustomSelect) window.initCustomSelect(typeSel);
    }
  }
}

/* ── 상세 모달 ── */
function openDetailModal(cnslSn) {
  fetch(`${CON_BASE}/detail/${cnslSn}`)
    .then((r) => r.json())
    .then((c) => {
      const isMember = !!c.stdUserId;
      const targetBadge = isMember
        ? `<span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-700">재원생</span>`
        : `<span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-orange-50 text-orange-600">신규문의</span>`;
      document.getElementById("detailModalTitle").textContent = `${c.studentNm} · ${fmtDt(c.cnslDt)} 상담 기록`;
      document.getElementById("detailModalBody").innerHTML = `
        <div class="grid grid-cols-2 gap-3 bg-slate-50 rounded-xl p-4">
          <div><p class="text-xs text-slate-400 mb-0.5">대상자</p><p class="font-medium flex items-center gap-1.5">${esc(c.studentNm)} ${targetBadge}</p></div>
          <div><p class="text-xs text-slate-400 mb-0.5">연락처</p><p class="font-medium">${esc(c.cnslTelno) || "-"}</p></div>
          ${isMember ? `<div><p class="text-xs text-slate-400 mb-0.5">학부모</p><p class="font-medium">${esc(c.parentNm) || "-"}</p></div>` : ""}
          <div><p class="text-xs text-slate-400 mb-0.5">담당강사</p><p class="font-medium">${esc(c.chrgNm) || "-"}</p></div>
          <div><p class="text-xs text-slate-400 mb-0.5">일시</p><p class="font-medium">${fmtDt(c.cnslDt)}</p></div>
          <div class="col-span-2"><p class="text-xs text-slate-400 mb-0.5">유형 / 상태</p><p class="font-medium flex gap-1">${typeBadge(c.cnslTypeNm)} ${statusBadge(c.cnslStatNm)}</p></div>
        </div>
        ${isMember ? `<div id="detailCourses"><p class="text-xs text-slate-400 mb-1 font-semibold uppercase tracking-wider">수강 강좌</p><p class="text-xs text-slate-300 px-1">불러오는 중...</p></div>` : ""}
        ${c.cnslCn ? `<div><p class="text-xs text-slate-400 mb-1 font-semibold uppercase tracking-wider">상담 내용</p><p class="text-sm leading-relaxed bg-white border border-slate-100 rounded-xl p-3 whitespace-pre-line">${esc(c.cnslCn)}</p></div>` : ""}
        ${c.cnslSmry ? `<div><p class="text-xs text-slate-400 mb-1 font-semibold uppercase tracking-wider">상담 요약</p><p class="text-sm leading-relaxed bg-white border border-slate-100 rounded-xl p-3">${esc(c.cnslSmry)}</p></div>` : ""}
        ${c.fllwUpCn ? `<div><p class="text-xs text-slate-400 mb-1 font-semibold uppercase tracking-wider">후속 조치</p><p class="text-sm leading-relaxed bg-amber-50 border border-amber-100 rounded-xl p-3 text-amber-700 whitespace-pre-line">${esc(c.fllwUpCn)}</p></div>` : ""}
      `;
      document.getElementById("detailModal").classList.remove("hidden");
      if (isMember) loadDetailCourses(c.stdUserId);
    });
}

/* 상담 상세 모달 · 학생 수강 강좌 로드 */
function loadDetailCourses(stdUserId) {
  fetch(`${CON_BASE}/student/${encodeURIComponent(stdUserId)}/courses`)
    .then((r) => r.json())
    .then((list) => {
      const box = document.getElementById("detailCourses");
      if (!box) return;
      const head = `<p class="text-xs text-slate-400 mb-1 font-semibold uppercase tracking-wider">수강 강좌</p>`;
      if (!list.length) {
        box.innerHTML = head + `<p class="text-xs text-slate-300 px-1">수강 중인 강좌가 없습니다.</p>`;
        return;
      }
      box.innerHTML =
        head +
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
    .catch(() => {
      const box = document.getElementById("detailCourses");
      if (box) box.innerHTML = `<p class="text-xs text-slate-300 px-1">강좌 정보를 불러오지 못했습니다.</p>`;
    });
}
function closeDetailModal() {
  document.getElementById("detailModal").classList.add("hidden");
}

/* ── 상담 기록 저장 ── */
function saveConsultRecord() {
  const cnslDt = document.getElementById("writeDatetime").value;
  const cnslCn = document.getElementById("writeContent").value.trim();

  const params = {
    cnslDt: cnslDt,
    cnslTypeCd: document.getElementById("writeType").value,
    cnslStatCd: document.getElementById("writeStatus").value,
    cnslCn: cnslCn,
    cnslSmry: document.getElementById("writeSummary").value.trim(),
    fllwUpCn: document.getElementById("writeFollowup").value.trim(),
  };

  if (writeMode === "member") {
    const stdUserId = document.getElementById("writeStudentId").value;
    if (!stdUserId) {
      showHermesToast("재원생을 선택해 주세요.", "error");
      return;
    }
    params.stdUserId = stdUserId;
  } else {
    const cnslNm = document.getElementById("writeCnslNm").value.trim();
    if (!cnslNm) {
      showHermesToast("문의자 이름을 입력해 주세요.", "error");
      return;
    }
    params.cnslNm = cnslNm;
    params.cnslTelno = document.getElementById("writeCnslTelno").value.trim();
  }
  if (!cnslDt) {
    showHermesToast("상담 일시를 입력해 주세요.", "error");
    return;
  }
  if (!cnslCn) {
    showHermesToast("상담 내용을 입력해 주세요.", "error");
    return;
  }
  fetch(`${CON_BASE}/save`, {
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
      showHermesToast("상담 기록이 저장되었습니다.", "success");
      resetWriteForm();
      loadSummary();
      loadHistory(1);
      SharedCalendar.reload();
    })
    .catch(() => showHermesToast("저장 중 오류가 발생했습니다.", "error"));
}
function resetWriteForm() {
  ["writeStudentId", "writeStudentNm", "writeParentNm", "writeCnslNm", "writeCnslTelno", "writeContent", "writeSummary", "writeFollowup"].forEach((id) => {
    const el = document.getElementById(id);
    if (el) el.value = "";
  });
  document.getElementById("writeDatetime").value = defaultDatetime();
  setWriteMode("member");
}
function defaultDatetime() {
  const now = new Date();
  now.setMinutes(now.getMinutes() - now.getTimezoneOffset());
  return now.toISOString().slice(0, 16);
}

/* ── 초기화 ── */
document.addEventListener("DOMContentLoaded", function () {
  // 공통코드 select (211 유형 / 212 상태)
  loadCommonCodes("historyType", "211", "전체 유형");
  loadCommonCodes("historyStat", "212", "전체 상태");
  loadCommonCodes("writeType", "211");
  loadCommonCodes("writeStatus", "212");

  document.getElementById("writeDatetime").value = defaultDatetime();
  setWriteMode("member");

  SharedCalendar.init({
    onEventClick: (ev) => {
      if (ev.source === "상담") openDetailModal(ev.refId);
    },
  });
  loadSummary();
  loadHistory(1);
  switchConTab("tab-history", document.querySelector('.con-tab-btn[data-tab="tab-history"]'));
});
