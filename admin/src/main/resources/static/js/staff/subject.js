/* ── 상태 ── */
let selectedClId = null;
let deletingClId = null;
let deletingSubjId = null;

function escapeHtml(str) {
  return String(str ?? "")
    .replace(/&/g, "&amp;").replace(/</g, "&lt;")
    .replace(/>/g, "&gt;").replace(/"/g, "&quot;").replace(/'/g, "&#39;");
}

/* ══════════════════════════════════════════════
   대분류 선택
══════════════════════════════════════════════ */
function selectClassification(clId) {
  selectedClId = clId;

  document.querySelectorAll(".classification-card").forEach((el) => {
    el.classList.remove("border-blue-400", "bg-blue-50/40");
    el.classList.add("border-slate-100");
  });
  const card = document.getElementById("card-" + clId);
  if (card) {
    card.classList.remove("border-slate-100");
    card.classList.add("border-blue-400", "bg-blue-50/40");
  }

  document.getElementById("panel-title").textContent =
    card.querySelector(".card-cl-nm").textContent;

  document.getElementById("panel-empty").classList.add("hidden");
  const panelContent = document.getElementById("panel-content");
  panelContent.classList.remove("hidden");
  panelContent.classList.add("flex");

  document.getElementById("subject-create-panel").classList.add("hidden");
  loadSubjects();
}

/* ══════════════════════════════════════════════
   과목 목록 로드
══════════════════════════════════════════════ */
async function loadSubjects() {
  const listEl = document.getElementById("subject-list");
  const emptyEl = document.getElementById("subject-list-empty");
  const countEl = document.getElementById("subject-count");

  listEl.innerHTML = '<p class="text-sm text-slate-400 text-center py-4">불러오는 중...</p>';
  emptyEl.classList.add("hidden");

  try {
    const res = await fetch(`/admin/subject/classification/${selectedClId}/subjects`);
    if (!res.ok) {
      listEl.innerHTML = `<p class="text-sm text-red-400 text-center py-4">과목 목록을 불러오지 못했습니다. (${res.status})</p>`;
      return;
    }
    const subjects = await res.json();
    countEl.textContent = `총 ${subjects.length}개`;

    if (subjects.length === 0) {
      listEl.innerHTML = "";
      emptyEl.classList.remove("hidden");
    } else {
      emptyEl.classList.add("hidden");
      listEl.innerHTML = subjects.map(buildSubjectRow).join("");
    }
  } catch (e) {
    listEl.innerHTML = '<p class="text-sm text-red-400 text-center py-4">과목 목록을 불러오지 못했습니다.</p>';
  }
}

function buildSubjectRow(s) {
  const nm = escapeHtml(s.subjNm) || "-";
  const expln = s.subjExplnCn
    ? `<p class="text-xs text-slate-400 mt-0.5 truncate">${escapeHtml(s.subjExplnCn)}</p>`
    : "";
  return `
  <div class="subject-row flex items-center gap-3 px-4 py-3 rounded-xl border border-slate-100 bg-slate-50/50 hover:bg-white hover:shadow-sm transition-all"
       data-subj-id="${s.subjId}">
    <div class="w-7 h-7 rounded-lg bg-blue-50 flex items-center justify-center shrink-0">
      <i class="fa-solid fa-book text-blue-400 text-xs"></i>
    </div>
    <div class="flex-1 min-w-0">
      <p class="text-sm font-medium text-slate-800 truncate subj-nm-text">${nm}</p>
      ${expln}
    </div>
    <div class="flex gap-1.5 shrink-0">
      <button type="button"
              data-subj-id="${s.subjId}"
              data-nm="${escapeHtml(s.subjNm)}"
              data-expln="${escapeHtml(s.subjExplnCn ?? '')}"
              onclick="openSubjectEditInline(this)"
              class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium text-blue-600 hover:bg-blue-50 transition-colors border border-blue-100">
        <i class="fa-solid fa-pen mr-1"></i>수정
      </button>
      <button type="button"
              data-subj-id="${s.subjId}"
              data-nm="${escapeHtml(s.subjNm)}"
              onclick="openDeleteSubjectModal(Number(this.dataset.subjId), this.dataset.nm)"
              class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-medium text-red-500 hover:bg-red-50 transition-colors border border-red-100">
        <i class="fa-solid fa-trash mr-1"></i>삭제
      </button>
    </div>
  </div>
  <div class="subject-edit-form hidden px-4 pb-3 pt-2 space-y-3 bg-blue-50/30 border border-blue-100 rounded-xl -mt-2"
       data-for-subj="${s.subjId}">
    <div>
      <label class="block text-xs font-semibold text-slate-500 mb-1">과목명 <span class="text-red-400">*</span></label>
      <input type="text" class="subj-edit-nm hm-input w-full" maxlength="100" />
    </div>
    <div>
      <label class="block text-xs font-semibold text-slate-500 mb-1">설명</label>
      <textarea class="subj-edit-expln hm-input w-full resize-none" rows="2" maxlength="4000"></textarea>
    </div>
    <div class="subj-edit-error hidden text-xs text-red-500 bg-red-50 rounded-lg px-3 py-2"></div>
    <div class="flex gap-2">
      <button type="button" onclick="closeSubjectEditInline(this)"
              class="flex-1 hm-btn-secondary justify-center">취소</button>
      <button type="button" onclick="submitSubjectEdit(this)"
              class="subj-edit-submit flex-1 hm-btn-primary hm-btn-blue justify-center">
        <i class="fa-solid fa-check mr-1"></i>저장
      </button>
    </div>
  </div>`;
}

/* ══════════════════════════════════════════════
   과목 인라인 수정
══════════════════════════════════════════════ */
function openSubjectEditInline(btn) {
  const subjId = btn.dataset.subjId;
  document.querySelectorAll(".subject-edit-form").forEach((f) => f.classList.add("hidden"));

  const form = document.querySelector(`.subject-edit-form[data-for-subj="${subjId}"]`);
  form.querySelector(".subj-edit-nm").value = btn.dataset.nm ?? "";
  form.querySelector(".subj-edit-expln").value = btn.dataset.expln ?? "";
  form.querySelector(".subj-edit-error").classList.add("hidden");
  form.classList.remove("hidden");
  form.querySelector(".subj-edit-nm").focus();
}

function closeSubjectEditInline(btn) {
  btn.closest(".subject-edit-form").classList.add("hidden");
}

async function submitSubjectEdit(btn) {
  const form = btn.closest(".subject-edit-form");
  const subjId = form.dataset.forSubj;
  const nm = form.querySelector(".subj-edit-nm").value.trim();
  const errorEl = form.querySelector(".subj-edit-error");

  if (!nm) {
    errorEl.textContent = "과목명은 필수 입력 항목입니다.";
    errorEl.classList.remove("hidden");
    return;
  }
  btn.disabled = true;

  const params = new URLSearchParams();
  params.append("subjNm", nm);
  params.append("subjExplnCn", form.querySelector(".subj-edit-expln").value);

  try {
    const res = await fetch(`/admin/subject/classification/${selectedClId}/${subjId}/update`, {
      method: "POST", body: params,
    });
    const data = await res.json();
    if (data.success) {
      btn.disabled = false;
      form.classList.add("hidden");
      showHermesToast("결재 요청이 완료되었습니다. 승인 후 처리됩니다.", "success");
    } else {
      errorEl.textContent = data.message || "수정에 실패했습니다.";
      errorEl.classList.remove("hidden");
      btn.disabled = false;
    }
  } catch (e) {
    errorEl.textContent = "서버 오류가 발생했습니다.";
    errorEl.classList.remove("hidden");
    btn.disabled = false;
  }
}

/* ══════════════════════════════════════════════
   과목 등록 인라인 폼 토글
══════════════════════════════════════════════ */
function toggleSubjectCreatePanel() {
  const panel = document.getElementById("subject-create-panel");
  if (!panel.classList.contains("hidden")) {
    panel.classList.add("hidden");
    return;
  }
  document.getElementById("subject-create-nm").value = "";
  document.getElementById("subject-create-expln").value = "";
  document.getElementById("subject-create-error").classList.add("hidden");
  panel.classList.remove("hidden");
  document.getElementById("subject-create-nm").focus();
}

async function submitCreateSubject() {
  const nm = document.getElementById("subject-create-nm").value.trim();
  const errorEl = document.getElementById("subject-create-error");
  if (!nm) {
    errorEl.textContent = "과목명은 필수 입력 항목입니다.";
    errorEl.classList.remove("hidden");
    return;
  }
  const btn = document.getElementById("subject-create-btn");
  btn.disabled = true;

  const params = new URLSearchParams();
  params.append("subjNm", nm);
  params.append("subjExplnCn", document.getElementById("subject-create-expln").value);

  try {
    const res = await fetch(`/admin/subject/classification/${selectedClId}/create`, {
      method: "POST", body: params,
    });
    const data = await res.json();
    if (data.success) {
      toggleSubjectCreatePanel();
      showHermesToast("결재 요청이 완료되었습니다. 승인 후 처리됩니다.", "success");
      btn.disabled = false;
    } else {
      errorEl.textContent = data.message || "등록에 실패했습니다.";
      errorEl.classList.remove("hidden");
      btn.disabled = false;
    }
  } catch (e) {
    errorEl.textContent = "서버 오류가 발생했습니다.";
    errorEl.classList.remove("hidden");
    btn.disabled = false;
  }
}

/* ══════════════════════════════════════════════
   대분류 등록 폼
══════════════════════════════════════════════ */
function openCreateForm() {
  document.getElementById("create-form-card").classList.remove("hidden");
  document.getElementById("header-create-btn").classList.add("hidden");
  document.getElementById("create-subjClNm").focus();
}
function closeCreateForm() {
  document.getElementById("create-form-card").classList.add("hidden");
  document.getElementById("header-create-btn").classList.remove("hidden");
  document.getElementById("create-subjClNm").value = "";
  document.getElementById("create-error").classList.add("hidden");
}
async function submitCreate() {
  const nm = document.getElementById("create-subjClNm").value.trim();
  const errorEl = document.getElementById("create-error");
  if (!nm) {
    errorEl.textContent = "대분류명은 필수 입력 항목입니다.";
    errorEl.classList.remove("hidden");
    return;
  }
  const btn = document.getElementById("create-submit-btn");
  btn.disabled = true;

  const params = new URLSearchParams();
  params.append("subjClNm", nm);

  try {
    const res = await fetch("/admin/subject/classification/create", {
      method: "POST", body: params,
    });
    const data = await res.json();
    if (data.success) {
      closeCreateForm();
      showHermesToast("결재 요청이 완료되었습니다. 승인 후 처리됩니다.", "success");
      btn.disabled = false;
    } else {
      errorEl.textContent = data.message || "등록에 실패했습니다.";
      errorEl.classList.remove("hidden");
      btn.disabled = false;
    }
  } catch (e) {
    errorEl.textContent = "서버 오류가 발생했습니다.";
    errorEl.classList.remove("hidden");
    btn.disabled = false;
  }
}

/* ══════════════════════════════════════════════
   대분류 수정 폼
══════════════════════════════════════════════ */
function openEditForm(btn) {
  const card = btn.closest(".classification-card");
  const editDiv = card.querySelector(".edit-form");

  document.querySelectorAll(".edit-form").forEach((f) => {
    if (f !== editDiv) f.classList.add("hidden");
  });

  editDiv.querySelector(".edit-subjClNm").value = btn.dataset.nm ?? "";
  editDiv.querySelector(".edit-error").classList.add("hidden");
  editDiv.classList.remove("hidden");
  editDiv.querySelector(".edit-subjClNm").focus();
}
function closeEditForm(btn) {
  btn.closest(".edit-form").classList.add("hidden");
}
async function submitEdit(btn) {
  const editDiv = btn.closest(".edit-form");
  const card = editDiv.closest(".classification-card");
  const clId = card.id.replace("card-", "");
  const nm = editDiv.querySelector(".edit-subjClNm").value.trim();
  const errorEl = editDiv.querySelector(".edit-error");

  if (!nm) {
    errorEl.textContent = "대분류명은 필수 입력 항목입니다.";
    errorEl.classList.remove("hidden");
    return;
  }
  btn.disabled = true;

  const params = new URLSearchParams();
  params.append("subjClNm", nm);

  try {
    const res = await fetch(`/admin/subject/classification/${clId}/update`, {
      method: "POST", body: params,
    });
    const data = await res.json();
    if (data.success) {
      btn.disabled = false;
      editDiv.classList.add("hidden");
      showHermesToast("결재 요청이 완료되었습니다. 승인 후 처리됩니다.", "success");
    } else {
      errorEl.textContent = data.message || "수정에 실패했습니다.";
      errorEl.classList.remove("hidden");
      btn.disabled = false;
    }
  } catch (e) {
    errorEl.textContent = "서버 오류가 발생했습니다.";
    errorEl.classList.remove("hidden");
    btn.disabled = false;
  }
}

/* ══════════════════════════════════════════════
   대분류 삭제 모달
══════════════════════════════════════════════ */
function openDeleteModal(btn) {
  deletingClId = btn.dataset.id;
  document.getElementById("delete-nm").textContent = btn.dataset.nm;
  document.getElementById("delete-modal").classList.replace("hidden", "flex");
}
function closeDeleteModal() {
  deletingClId = null;
  document.getElementById("delete-modal").classList.replace("flex", "hidden");
}
async function submitDelete() {
  if (!deletingClId) return;
  const btn = document.getElementById("delete-confirm-btn");
  btn.disabled = true;
  try {
    const res = await fetch(`/admin/subject/classification/${deletingClId}/delete`, {
      method: "POST",
    });
    const data = await res.json();
    if (data.success) {
      btn.disabled = false;
      closeDeleteModal();
      showHermesToast("결재 요청이 완료되었습니다. 승인 후 처리됩니다.", "success");
    } else {
      alert(data.message || "삭제에 실패했습니다.");
      btn.disabled = false;
      closeDeleteModal();
    }
  } catch (e) {
    alert("서버 오류가 발생했습니다.");
    btn.disabled = false;
    closeDeleteModal();
  }
}

/* ══════════════════════════════════════════════
   과목 삭제 모달
══════════════════════════════════════════════ */
function openDeleteSubjectModal(subjId, subjNm) {
  deletingSubjId = subjId;
  document.getElementById("delete-subject-nm").textContent = subjNm;
  document.getElementById("delete-subject-modal").classList.replace("hidden", "flex");
}
function closeDeleteSubjectModal() {
  deletingSubjId = null;
  document.getElementById("delete-subject-modal").classList.replace("flex", "hidden");
}
async function submitDeleteSubject() {
  if (!deletingSubjId) return;
  const btn = document.getElementById("delete-subject-confirm-btn");
  btn.disabled = true;
  try {
    const res = await fetch(`/admin/subject/classification/${selectedClId}/${deletingSubjId}/delete`, {
      method: "POST",
    });
    const data = await res.json();
    if (data.success) {
      btn.disabled = false;
      closeDeleteSubjectModal();
      showHermesToast("결재 요청이 완료되었습니다. 승인 후 처리됩니다.", "success");
    } else {
      alert(data.message || "삭제에 실패했습니다.");
      btn.disabled = false;
      closeDeleteSubjectModal();
    }
  } catch (e) {
    alert("서버 오류가 발생했습니다.");
    btn.disabled = false;
    closeDeleteSubjectModal();
  }
}
