let selectedEmpId = null;
let detailEditMode = false;

/* ─── 탭 전환 ─── */
function switchEmpTab(tabId, btn) {
  document.querySelectorAll(".emp-tab-btn").forEach((b) => b.classList.remove("active"));
  document.querySelectorAll(".emp-tab-panel").forEach((p) => p.classList.remove("active"));
  btn.classList.add("active");
  document.getElementById(tabId).classList.add("active");
}

/* ─── 페이징 + 필터 + 정렬 ─── */
let currentHrPage = 1;
const HR_SCREEN_SIZE = 7;
const HR_BLOCK_SIZE = 5;
let hrSortCol = null;
let hrSortAsc = true;
let hrFilteredRows = null;

function filterHrList() {
  const keyword = document.getElementById("hr-search").value.trim().toLowerCase();
  const year    = document.getElementById("hr-year").value;
  const type    = document.getElementById("hr-type-filter").value;
  const status  = document.getElementById("hr-status-filter").value;
  currentHrPage = 1;

  const allRows = Array.from(document.querySelectorAll("#hr-table-body .hr-data-row"));

  hrFilteredRows = allRows.filter((r) => {
    if (keyword && !(r.dataset.name || "").toLowerCase().includes(keyword)) return false;
    if (year   && !(r.dataset.join || "").startsWith(year))                  return false;
    if (type   && r.dataset.type   !== type)                                  return false;
    if (status && r.dataset.enable !== status)                                return false;
    return true;
  });

  if (hrSortCol) applyHrSort(hrFilteredRows);
  applyHrPaging();
}

function resetHrFilter() {
  document.getElementById("hr-search").value = "";
  ["hr-year", "hr-type-filter", "hr-status-filter"].forEach((id) => {
    const el = document.getElementById(id);
    if (!el) return;
    if (el.tomselect) el.tomselect.setValue("");
    else el.value = "";
  });
  hrSortCol = null;
  hrSortAsc = true;
  updateHrSortIcons(null);
  filterHrList();
}

function sortHrBy(col) {
  if (hrSortCol === col) hrSortAsc = !hrSortAsc;
  else { hrSortCol = col; hrSortAsc = true; }
  updateHrSortIcons(col);
  filterHrList();
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
  if (!container) return;
  const totalPage = Math.ceil(totalCount / HR_SCREEN_SIZE);
  if (totalPage <= 1) { container.innerHTML = ""; return; }

  const endPage   = Math.min(Math.ceil(currentHrPage / HR_BLOCK_SIZE) * HR_BLOCK_SIZE, totalPage);
  const startPage = Math.max(endPage - HR_BLOCK_SIZE + 1, 1);

  let html = "";
  if (startPage > 1)
    html += `<button onclick="goHrPage(${startPage - 1})" class="emp-page-btn">이전</button>`;
  for (let p = startPage; p <= endPage; p++)
    html += `<button onclick="goHrPage(${p})" class="emp-page-btn${p === currentHrPage ? " active" : ""}">${p}</button>`;
  if (endPage < totalPage)
    html += `<button onclick="goHrPage(${endPage + 1})" class="emp-page-btn">다음</button>`;
  container.innerHTML = html;
}

function goHrPage(p) {
  const scrollY = window.scrollY;
  currentHrPage = p;
  applyHrPaging();
  window.scrollTo({ top: scrollY, behavior: "instant" });
}

/* ─── 전화번호 표시 포맷 ─── */
function formatPhoneDisplay(tel) {
  if (!tel) return "-";
  const v = tel.replace(/\D/g, "");
  if (v.length === 11) return v.slice(0, 3) + "-" + v.slice(3, 7) + "-" + v.slice(7);
  if (v.length === 10) return v.slice(0, 3) + "-" + v.slice(3, 6) + "-" + v.slice(6);
  return tel;
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
    avatarImg.src = profileUrl;
    avatarImg.classList.remove("hidden");
    avatarImg.style.cursor = "zoom-in";
    avatarImg.onclick = () => openProfileLightbox(profileUrl);
    avatarDiv.classList.add("hidden");
  } else {
    avatarImg.classList.add("hidden");
    avatarImg.onclick = null;
    avatarDiv.classList.remove("hidden");
    avatarDiv.textContent = row.dataset.name ? row.dataset.name[0] : "?";
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
    document.getElementById("edit-enable").value   = row.dataset.enable || "Y";
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

  row.dataset.name       = document.getElementById("edit-name").value.trim();
  row.dataset.phone      = document.getElementById("edit-phone").value.replace(/-/g, "");
  row.dataset.email      = document.getElementById("edit-email").value.trim();
  row.dataset.zip        = document.getElementById("edit-zipcode").value;
  row.dataset.addrBase   = addrBase;
  row.dataset.addrDetail = addrDetail;
  row.dataset.addr       = (addrBase + (addrDetail ? " " + addrDetail : "")).trim();
  row.dataset.type       = document.getElementById("edit-stu-type").value;
  row.dataset.enable     = document.getElementById("edit-enable").value;

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
  formData.append("enable",        document.getElementById("edit-enable").value);
  const profileFile = document.getElementById("edit-profile").files[0];
  if (profileFile) formData.append("editProfileImage", profileFile);

  fetch("/admin/students/update", { method: "PUT", body: formData })
    .then((res) => res.json())
    .then((data) => {
      if (data.result === "success") {
        const newProfileUrl = data.profileUrl || "";
        row.dataset.profile = newProfileUrl;

        // 아바타 즉시 동기화
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
        showHermesToast("학생 정보가 수정되었습니다.", "success");
      } else {
        showHermesToast("수정 실패: " + (data.message || "서버 오류"), "error");
      }
    })
    .catch(() => showHermesToast("수정 요청 중 오류가 발생했습니다.", "error"));
}

/* ─── 탈퇴 처리 ─── */
function openResignConfirm() {
  const reason = document.getElementById("resign-reason");
  const reasonVal = reason && reason.tomselect ? reason.tomselect.getValue() : reason ? reason.value : "";
  const detail = document.getElementById("resign-reason-detail");

  if (!reasonVal) {
    showHermesToast("탈퇴 사유를 선택해주세요.", "error");
    return;
  }
  if (reasonVal === "기타" && (!detail || !detail.value.trim())) {
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
  const reasonDetail   = document.getElementById("resign-reason-detail");
  const selectedVal    = reasonSelectEl.tomselect ? reasonSelectEl.tomselect.getValue() : reasonSelectEl.value;
  let withdrawRsn      = selectedVal === "기타" ? (reasonDetail.value || "").trim() : selectedVal;

  if (!withdrawRsn) {
    showHermesToast(selectedVal === "기타" ? "기타 사유를 입력해주세요." : "탈퇴 사유를 선택해주세요.", "error");
    return;
  }
  if (selectedVal === "기타") withdrawRsn = "기타: " + withdrawRsn;

  fetch("/admin/students/" + selectedEmpId + "/withdraw", {
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
        closeModal("modal-resign-confirm");
        closeModal("modal-emp-detail");
        const rsEl = document.getElementById("resign-reason");
        if (rsEl.tomselect) rsEl.tomselect.setValue("");
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

function searchDetailZipCode() {
  new daum.Postcode({
    oncomplete: function (data) {
      const addr = data.userSelectedType === "R" ? data.roadAddress : data.jibunAddress;
      document.getElementById("edit-zipcode").value    = data.zonecode;
      document.getElementById("edit-addr").value       = addr;
      document.getElementById("edit-addr-detail").focus();
    },
  }).open();
}

/* ─── 입력 포맷 ─── */
function formatPhone(el) {
  let v = el.value.replace(/\D/g, "");
  if (v.length > 11) v = v.slice(0, 11);
  if (v.length >= 3 && !/^010/.test(v)) {
    showHermesToast("휴대폰 번호는 010으로 시작해야 합니다.", "error");
    el.value = "010-";
    return;
  }
  if (v.length >= 8)      v = v.slice(0, 3) + "-" + v.slice(3, 7) + "-" + v.slice(7);
  else if (v.length >= 4) v = v.slice(0, 3) + "-" + v.slice(3);
  el.value = v;
}

function blurValidatePhone(el) {
  const v = el.value.trim();
  if (!v) return;
  if (!/^010-\d{3,4}-\d{4}$/.test(v))
    showHermesToast("올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)", "error");
}

function blurValidateEmail(el) {
  const v = el.value.trim();
  if (!v) return;
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v))
    showHermesToast("유효한 이메일 형식이 아닙니다.", "error");
  else if (v.length > 100)
    showHermesToast("이메일은 100자 이하로 입력해주세요.", "error");
}

/* ─── 모달 공통 ─── */
function openModal(id)  { document.getElementById(id).classList.remove("hidden"); }
function closeModal(id) { document.getElementById(id).classList.add("hidden"); }

function showWarningToast(message) {
  let container = document.getElementById("hm-toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "hm-toast-container";
    container.className = "fixed bottom-6 right-6 z-50 space-y-3 pointer-events-none";
    document.body.appendChild(container);
  }
  const toast = document.createElement("div");
  toast.className = "px-5 py-3.5 rounded-xl shadow-xl text-xs font-bold text-white bg-amber-500 transition-all duration-300 transform translate-y-4 opacity-0 flex items-center gap-3 pointer-events-auto";
  toast.innerHTML = '<i class="fa-solid fa-triangle-exclamation text-sm"></i> <span>' + message + "</span>";
  container.appendChild(toast);
  setTimeout(() => toast.classList.remove("translate-y-4", "opacity-0"), 10);
  setTimeout(() => {
    toast.classList.add("opacity-0", "translate-y-4");
    setTimeout(() => toast.remove(), 300);
  }, 3000);
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
