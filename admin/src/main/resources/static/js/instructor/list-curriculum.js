/* ── 상태 ── */
let selectedCurriculumId = null;
let deletingId = null;
let removingCourseSn = null;
let sortable = null;
let allAvailableCourses = [];
let reorderInProgress = false;


/**
 * "HH:MM:SS" 또는 "H:MM:SS" 형태의 시간 문자열을 "H시간 M분" 으로 변환.
 * 분이 0이면 시간만, 시간이 0이면 분만 표시.
 */
function formatDuration(val) {
  if (!val) return "";
  const parts = String(val).split(":");
  if (parts.length < 2) return val;
  const h = parseInt(parts[0], 10);
  const m = parseInt(parts[1], 10);
  if (h > 0 && m > 0) return `${h}시간 ${m}분`;
  if (h > 0) return `${h}시간`;
  if (m > 0) return `${m}분`;
  return "";
}

/* ══════════════════════════════════════════════
   커리큘럼 선택
══════════════════════════════════════════════ */
function selectCurriculum(curriculumId, strt, end) {
  selectedCurriculumId = curriculumId;

  document.querySelectorAll(".curriculum-card").forEach((el) => {
    el.classList.remove("border-sky-400", "bg-sky-50/40");
    el.classList.add("border-slate-100");
  });
  const card = document.getElementById("card-" + curriculumId);
  if (card) {
    card.classList.remove("border-slate-100");
    card.classList.add("border-sky-400", "bg-sky-50/40");
  }

  const titleEl = card?.querySelector(".card-title");
  document.getElementById("panel-title").textContent = titleEl ? titleEl.textContent : "";
  document.getElementById("panel-period").textContent = strt
    ? `${strt} ~ ${end}`
    : "기간 미설정";

  document.getElementById("panel-empty").classList.add("hidden");
  const panelContent = document.getElementById("panel-content");
  panelContent.classList.remove("hidden");
  panelContent.classList.add("flex");

  document.getElementById("add-course-panel").classList.add("hidden");

  loadMappedCourses();
}

/* ══════════════════════════════════════════════
   배정된 강좌 목록 로드
══════════════════════════════════════════════ */
async function loadMappedCourses() {
  const listEl = document.getElementById("mapped-course-list");
  const emptyEl = document.getElementById("mapped-course-empty");
  const countEl = document.getElementById("mapped-count");
  const addBtn = document.getElementById("add-course-toggle-btn");

  if (addBtn) addBtn.disabled = true;
  try {
    const res = await fetch(
      `/instructor/curriculum/${selectedCurriculumId}/courses`,
    );
    if (!res.ok) {
      listEl.innerHTML = `<p class="text-sm text-red-400 text-center py-4">강좌 목록을 불러오지 못했습니다. (${res.status})</p>`;
      return;
    }
    const courses = await res.json();

    countEl.textContent = `총 ${courses.length}개`;

    const card = document.getElementById("card-" + selectedCurriculumId);
    const cardCountEl = card?.querySelector(".card-course-count");
    if (cardCountEl) cardCountEl.textContent = courses.length;

    if (courses.length === 0) {
      listEl.innerHTML = "";
      emptyEl.classList.remove("hidden");
    } else {
      emptyEl.classList.add("hidden");
      listEl.innerHTML = courses.map(buildMappedCourseRow).join("");
    }

    if (sortable) sortable.destroy();
    sortable = Sortable.create(listEl, {
      animation: 150,
      handle: ".drag-handle",
      onEnd: saveOrder,
    });
  } catch (e) {
    listEl.innerHTML =
      '<p class="text-sm text-red-400 text-center py-4">강좌 목록을 불러오지 못했습니다.</p>';
  } finally {
    if (addBtn) addBtn.disabled = false;
  }
}

function buildMappedCourseRow(c) {
  const opnnBadge =
    c.opnnYn === "Y"
      ? '<span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-600">공개</span>'
      : '<span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-400">비공개</span>';
  const dur = formatDuration(c.totLrnTimeCnt);
  const duration = dur
    ? `<span class="text-xs text-slate-400">${escHtml(dur)}</span>`
    : "";
  const nm = escHtml(c.courseNm) || "-";

  return `
  <div class="flex items-center gap-3 px-4 py-3 rounded-xl border border-slate-100 bg-slate-50/50 hover:bg-white hover:shadow-sm transition-all"
       data-sn="${c.courseSn}">
    <span class="drag-handle cursor-grab text-slate-300 hover:text-slate-500 active:cursor-grabbing shrink-0">
      <i class="fa-solid fa-grip-vertical"></i>
    </span>
    <span class="w-6 h-6 rounded-md bg-sky-100 text-sky-500 text-xs font-bold flex items-center justify-center shrink-0 sort-ord">
      ${c.sortOrd ?? "-"}
    </span>
    <div class="flex-1 min-w-0">
      <p class="text-sm font-medium text-slate-800 truncate">${nm}</p>
      ${duration}
    </div>
    ${opnnBadge}
    <a href="/admin/course/detail?courseSn=${c.courseSn}"
       class="relative group shrink-0 p-1.5 rounded-lg text-slate-300 hover:text-sky-500 hover:bg-sky-50 transition-colors">
      <i class="fa-solid fa-arrow-up-right-from-square text-xs"></i>
      <span class="pointer-events-none absolute right-full top-1/2 -translate-y-1/2 mr-2 whitespace-nowrap rounded-lg bg-slate-800 px-2 py-1 text-[11px] text-white opacity-0 transition-opacity group-hover:opacity-100 z-10">강좌 상세</span>
    </a>
    <button type="button"
            data-sn="${c.courseSn}" data-nm="${escHtml(c.courseNm)}"
            onclick="openRemoveCourseModal(Number(this.dataset.sn), this.dataset.nm)"
            class="shrink-0 p-1.5 rounded-lg text-slate-300 hover:text-red-400 hover:bg-red-50 transition-colors">
      <i class="fa-solid fa-xmark"></i>
    </button>
  </div>`;
}

/* ══════════════════════════════════════════════
   순서 저장
══════════════════════════════════════════════ */
async function saveOrder() {
  if (reorderInProgress) return;
  reorderInProgress = true;

  const items = document.querySelectorAll("#mapped-course-list > [data-sn]");
  const courseSnList = Array.from(items).map((el) => Number(el.dataset.sn));

  items.forEach((el, i) => {
    const badge = el.querySelector(".sort-ord");
    if (badge) badge.textContent = i + 1;
  });

  try {
    const res = await fetch(
      `/instructor/curriculum/${selectedCurriculumId}/courses/reorder`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ courseSnList }),
      },
    );
    const data = await res.json();
    if (!data.success) {
      alert(data.message || "순서 저장에 실패했습니다. 원래 순서로 복원합니다.");
      loadMappedCourses();
    }
  } catch (e) {
    alert("서버 오류로 순서 저장에 실패했습니다.");
    loadMappedCourses();
  } finally {
    reorderInProgress = false;
  }
}

/* ══════════════════════════════════════════════
   강좌 추가 패널 토글
══════════════════════════════════════════════ */
async function toggleAddCoursePanel() {
  const panel = document.getElementById("add-course-panel");
  if (!panel.classList.contains("hidden")) {
    panel.classList.add("hidden");
    return;
  }
  panel.classList.remove("hidden");
  document.getElementById("course-search").value = "";

  const listEl = document.getElementById("available-course-list");
  listEl.innerHTML =
    '<p class="text-sm text-slate-400 text-center py-4">불러오는 중...</p>';

  try {
    const res = await fetch("/instructor/curriculum/available-courses");
    allAvailableCourses = await res.json();
    renderAvailableCourses(allAvailableCourses);
  } catch (e) {
    listEl.innerHTML =
      '<p class="text-sm text-red-400 text-center py-4">목록을 불러오지 못했습니다.</p>';
  }
}

function filterAvailableCourses(query) {
  const filtered = allAvailableCourses.filter(
    (c) =>
      c.courseNm && c.courseNm.toLowerCase().includes(query.toLowerCase()),
  );
  renderAvailableCourses(filtered);
}

function renderAvailableCourses(courses) {
  const listEl = document.getElementById("available-course-list");
  if (courses.length === 0) {
    listEl.innerHTML =
      '<p class="text-sm text-slate-400 text-center py-4">추가 가능한 강좌가 없습니다.</p>';
    return;
  }
  listEl.innerHTML = courses
    .map((c) => {
      const opnnBadge =
        c.opnnYn === "Y"
          ? '<span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-emerald-50 text-emerald-600">공개</span>'
          : '<span class="px-2 py-0.5 rounded-full text-xs font-semibold bg-slate-100 text-slate-400">비공개</span>';
      const dur = formatDuration(c.totLrnTimeCnt);
      const duration = dur
        ? `<span class="text-xs text-slate-400">${escHtml(dur)}</span>`
        : "";
      const nm = escHtml(c.courseNm) || "-";
      return `
      <div class="flex items-center gap-3 px-4 py-3 rounded-xl border border-slate-100 hover:border-sky-200 hover:bg-sky-50/40 transition-all">
        <div class="flex-1 min-w-0">
          <p class="text-sm font-medium text-slate-800 truncate">${nm}</p>
          ${duration}
        </div>
        ${opnnBadge}
        <button type="button"
                data-sn="${c.courseSn}"
                onclick="submitAddCourse(Number(this.dataset.sn), this)"
                class="shrink-0 hm-btn-primary hm-btn-sky" style="padding:0.3rem 0.85rem;font-size:0.75rem;">
          <i class="fa-solid fa-plus mr-1"></i>추가
        </button>
      </div>`;
    })
    .join("");
}

async function submitAddCourse(courseSn, btn) {
  btn.disabled = true;
  try {
    const res = await fetch(
      `/instructor/curriculum/${selectedCurriculumId}/courses/add`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ courseSn }),
      },
    );
    const data = await res.json();
    if (data.success) {
      document.getElementById("add-course-panel").classList.add("hidden");
      loadMappedCourses();
    } else {
      alert(data.message || "추가에 실패했습니다.");
      btn.disabled = false;
    }
  } catch (e) {
    alert("서버 오류가 발생했습니다.");
    btn.disabled = false;
  }
}

/* ══════════════════════════════════════════════
   강좌 제거 모달
══════════════════════════════════════════════ */
function openRemoveCourseModal(courseSn, courseNm) {
  removingCourseSn = courseSn;
  document.getElementById("remove-course-name").textContent = courseNm;
  document.getElementById("remove-course-modal").classList.replace("hidden", "flex");
}
function closeRemoveCourseModal() {
  removingCourseSn = null;
  document.getElementById("remove-course-modal").classList.replace("flex", "hidden");
}
async function submitRemoveCourse() {
  if (!removingCourseSn) return;
  const btn = document.getElementById("remove-course-btn");
  btn.disabled = true;
  try {
    const res = await fetch(
      `/instructor/curriculum/${selectedCurriculumId}/courses/remove`,
      {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({ courseSn: removingCourseSn }),
      },
    );
    const data = await res.json();
    if (data.success) {
      btn.disabled = false;
      closeRemoveCourseModal();
      loadMappedCourses();
    } else {
      alert(data.message || "제거에 실패했습니다.");
      btn.disabled = false;
    }
  } catch (e) {
    alert("서버 오류가 발생했습니다.");
    btn.disabled = false;
  }
}

/* ══════════════════════════════════════════════
   커리큘럼 등록 폼
══════════════════════════════════════════════ */
function openCreateForm() {
  document.getElementById("create-form-card").classList.remove("hidden");
  document.getElementById("header-create-btn").classList.add("hidden");
  document.getElementById("create-title").focus();
}
function closeCreateForm() {
  document.getElementById("create-form-card").classList.add("hidden");
  document.getElementById("header-create-btn").classList.remove("hidden");
  ["create-title", "create-strtDt", "create-endDt", "create-explnCn"].forEach((id) => {
    document.getElementById(id).value = "";
  });
  document.getElementById("create-error").classList.add("hidden");
}
function validateCurriculumForm(title, strtDt, endDt, explnCn) {
  if (!title) return "커리큘럼명은 필수 입력 항목입니다.";
  if (title.length > 200) return "커리큘럼명은 200자 이내로 입력해 주세요.";
  if (explnCn && explnCn.length > 4000) return "설명은 4000자 이내로 입력해 주세요.";
  if (strtDt && endDt && new Date(endDt) < new Date(strtDt)) return "종료일은 시작일 이후여야 합니다.";
  return null;
}

async function submitCreate() {
  const title = document.getElementById("create-title").value.trim();
  const strtDt = document.getElementById("create-strtDt").value;
  const endDt = document.getElementById("create-endDt").value;
  const explnCn = document.getElementById("create-explnCn").value;
  const errorEl = document.getElementById("create-error");
  const validationError = validateCurriculumForm(title, strtDt, endDt, explnCn);
  if (validationError) {
    errorEl.textContent = validationError;
    errorEl.classList.remove("hidden");
    return;
  }
  const btn = document.getElementById("create-submit-btn");
  btn.disabled = true;

  const params = new URLSearchParams();
  params.append("title", title);
  if (strtDt) params.append("strtDt", strtDt);
  if (endDt) params.append("endDt", endDt);
  params.append("explnCn", document.getElementById("create-explnCn").value);

  try {
    const res = await fetch("/instructor/curriculum/create", {
      method: "POST",
      body: params,
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
   커리큘럼 수정 폼
══════════════════════════════════════════════ */
function openEditForm(btn) {
  const card = btn.closest(".curriculum-card");
  const editDiv = card.querySelector(".edit-form");

  document.querySelectorAll(".edit-form").forEach((f) => {
    if (f !== editDiv) f.classList.add("hidden");
  });

  editDiv.querySelector(".edit-title").value = btn.dataset.title ?? "";
  editDiv.querySelector(".edit-strtDt").value = btn.dataset.strt ?? "";
  editDiv.querySelector(".edit-endDt").value = btn.dataset.end ?? "";
  editDiv.querySelector(".edit-explnCn").value = btn.dataset.expln ?? "";
  editDiv.querySelector(".edit-error").classList.add("hidden");

  editDiv.classList.remove("hidden");
  editDiv.querySelector(".edit-title").focus();
}
function closeEditForm(btn) {
  btn.closest(".edit-form").classList.add("hidden");
}
async function submitEdit(btn) {
  const editDiv = btn.closest(".edit-form");
  const card = editDiv.closest(".curriculum-card");
  const cardId = card.id.replace("card-", "");
  const title = editDiv.querySelector(".edit-title").value.trim();
  const strtDt = editDiv.querySelector(".edit-strtDt").value;
  const endDt = editDiv.querySelector(".edit-endDt").value;
  const explnCn = editDiv.querySelector(".edit-explnCn").value;
  const errorEl = editDiv.querySelector(".edit-error");

  const validationError = validateCurriculumForm(title, strtDt, endDt, explnCn);
  if (validationError) {
    errorEl.textContent = validationError;
    errorEl.classList.remove("hidden");
    return;
  }
  btn.disabled = true;

  const params = new URLSearchParams();
  params.append("title", title);
  if (strtDt) params.append("strtDt", strtDt);
  if (endDt) params.append("endDt", endDt);
  params.append("explnCn", editDiv.querySelector(".edit-explnCn").value);

  try {
    const res = await fetch(`/instructor/curriculum/${cardId}/update`, {
      method: "POST",
      body: params,
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
   커리큘럼 삭제 모달
══════════════════════════════════════════════ */
function openDeleteModal(btn) {
  deletingId = btn.dataset.id;
  document.getElementById("delete-title").textContent = btn.dataset.title;
  document.getElementById("delete-modal").classList.replace("hidden", "flex");
}
function closeDeleteModal() {
  deletingId = null;
  document.getElementById("delete-modal").classList.replace("flex", "hidden");
}
async function submitDelete() {
  if (!deletingId) return;
  const btn = document.getElementById("delete-confirm-btn");
  btn.disabled = true;
  try {
    const res = await fetch(`/instructor/curriculum/${deletingId}/delete`, {
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
