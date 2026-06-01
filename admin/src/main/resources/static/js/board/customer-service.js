// ── 탭 전환 ──────────────────────────────────────────────
function switchTab(tabId, btn) {
  document
    .querySelectorAll(".cs-tab-btn")
    .forEach((b) => b.classList.remove("active"));
  document
    .querySelectorAll(".cs-tab-panel")
    .forEach((p) => p.classList.remove("active"));
  btn.classList.add("active");
  document.getElementById(tabId).classList.add("active");
  history.replaceState(null, "", "?tab=" + tabId);
}

// 페이지 로드 시 URL에서 탭 복원
document.addEventListener("DOMContentLoaded", function () {
  const params = new URLSearchParams(window.location.search);
  const tab = params.get("tab");
  if (tab) {
    const panel = document.getElementById(tab);
    const idx = ["tab-faq", "tab-notice", "tab-qna"].indexOf(tab);
    const btn = document.querySelectorAll(".cs-tab-btn")[idx];
    if (panel && btn) {
      document
        .querySelectorAll(".cs-tab-btn")
        .forEach((b) => b.classList.remove("active"));
      document
        .querySelectorAll(".cs-tab-panel")
        .forEach((p) => p.classList.remove("active"));
      btn.classList.add("active");
      panel.classList.add("active");
    }
  }
});

// ── FAQ 필터 ──────────────────────────────────────────────
function filterFaq() {
  const ctg = document.getElementById("faq-filter-ctg").value;
  const query = document
    .getElementById("faq-search")
    .value.trim()
    .toLowerCase();
  document.querySelectorAll("#faq-table-body tr[data-ctg]").forEach((row) => {
    const matchCtg = !ctg || row.dataset.ctg === ctg;
    const matchTitle =
      !query || row.dataset.title.toLowerCase().includes(query);
    row.style.display = matchCtg && matchTitle ? "" : "none";
  });
}

// ── 공지사항 필터 ─────────────────────────────────────────
function filterNotice() {
  const type = document.getElementById("notice-filter-type").value;
  const query = document
    .getElementById("notice-search")
    .value.trim()
    .toLowerCase();
  document
    .querySelectorAll("#notice-table-body tr[data-type]")
    .forEach((row) => {
      const matchType = !type || row.dataset.type === type;
      const matchTitle =
        !query || row.dataset.title.toLowerCase().includes(query);
      row.style.display = matchType && matchTitle ? "" : "none";
    });
}

// ── QnA 필터 ─────────────────────────────────────────────
function filterQna() {
  const stat = document.getElementById("qna-filter-stat").value;
  const ctg = document.getElementById("qna-filter-ctg").value;
  const query = document
    .getElementById("qna-search")
    .value.trim()
    .toLowerCase();
  document.querySelectorAll("#qna-table-body tr[data-stat]").forEach((row) => {
    const matchStat = !stat || row.dataset.stat === stat;
    const matchCtg = !ctg || row.dataset.ctg === ctg;
    const matchTitle =
      !query || row.dataset.title.toLowerCase().includes(query);
    row.style.display = matchStat && matchCtg && matchTitle ? "" : "none";
  });
}

// ── FAQ 모달 ──────────────────────────────────────────────
function openFaqModal(tr) {
  const d = tr.dataset;
  document.getElementById("modal-ctgnm").textContent = d.ctgnm;
  document.getElementById("modal-subctgnm").textContent = d.subctgnm;
  document.getElementById("modal-title").textContent = d.title;
  document.getElementById("modal-content").textContent = d.postcn;
  document.getElementById("modal-writer").textContent = d.wrtruserid;
  document.getElementById("modal-regdt").textContent = d.regdt;

  const best = document.getElementById("modal-best");
  best.classList.toggle("hidden", d.topfixyn !== "Y");

  document.getElementById("modal-edit-btn").href =
    "/admin/board/faq/edit/" + d.postsn;
  document.getElementById("modal-delete-form").action =
    "/admin/board/faq/delete/" + d.postsn;

  const modal = document.getElementById("faq-modal");
  modal.classList.remove("hidden");
  modal.classList.add("flex");
}

function closeFaqModal() {
  const modal = document.getElementById("faq-modal");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
}

// ── 공지사항 모달 ─────────────────────────────────────────
function openNoticeModal(tr) {
  const d = tr.dataset;
  document.getElementById("notice-modal-typenm").textContent = d.typenm;
  document.getElementById("notice-modal-title").textContent = d.title;
  document.getElementById("notice-modal-content").textContent = d.postcn;
  document.getElementById("notice-modal-writer").textContent = d.wrtruserid;
  document.getElementById("notice-modal-regdt").textContent = d.regdt;

  const popup = document.getElementById("notice-modal-popup");
  popup.classList.toggle("hidden", d.popupexpsyn !== "Y");

  document.getElementById("notice-modal-edit-btn").href =
    "/admin/board/notice/edit/" + d.postsn;
  document.getElementById("notice-modal-delete-form").action =
    "/admin/board/notice/delete/" + d.postsn;

  const modal = document.getElementById("notice-modal");
  modal.classList.remove("hidden");
  modal.classList.add("flex");
}

function closeNoticeModal() {
  const modal = document.getElementById("notice-modal");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
}
// ── QnA 모달 ──────────────────────────────────────────────
function openQnaModal(tr) {
  const d = tr.dataset;
  document.getElementById("qna-modal-ctgnm").textContent = d.ctgnm;
  document.getElementById("qna-modal-title").textContent = d.title;
  document.getElementById("qna-modal-content").textContent = d.postcn;
  document.getElementById("qna-modal-writer").textContent = d.wrtruserid;
  document.getElementById("qna-modal-regdt").textContent = d.regdt;

  // 비공개 여부
  document
    .getElementById("qna-modal-secr")
    .classList.toggle("hidden", d.secr !== "Y");

  // 답변 상태
  document
    .getElementById("qna-modal-stat-wait")
    .classList.toggle("hidden", d.answstatcd !== "01");
  document
    .getElementById("qna-modal-stat-done")
    .classList.toggle("hidden", d.answstatcd !== "02");

  // 답변 완료 - 내용 표시
  const answerArea = document.getElementById("qna-modal-answer-area");
  if (d.answstatcd === "02" && d.answcn) {
    answerArea.classList.remove("hidden");
    document.getElementById("qna-modal-answcn").textContent = d.answcn;
    document.getElementById("qna-modal-answdt").textContent =
      d.answdt?.slice(0, 10) ?? "";
  } else {
    answerArea.classList.add("hidden");
  }

  // 답변 폼 처리
  const answerForm = document.getElementById("qna-modal-answer-form");
  const answerFormEl = document.getElementById("qna-answer-form");
  const answerSubmit = document.getElementById("qna-modal-answer-submit");

  answerForm.classList.remove("hidden");
  answerSubmit.classList.remove("hidden");
  answerFormEl.action = "/admin/board/qna/" + d.postsn + "/answer";

  if (d.answstatcd === "01") {
    answerSubmit.textContent = "답변 등록";
    document.getElementById("qna-modal-answer-input").value = "";
  } else {
    answerSubmit.textContent = "답변 수정";
    document.getElementById("qna-modal-answer-input").value = d.answcn ?? "";
  }

  document.getElementById("qna-modal-delete-form").action =
    "/admin/board/qna/delete/" + d.postsn;

  const modal = document.getElementById("qna-modal");
  modal.classList.remove("hidden");
  modal.classList.add("flex");
}

function closeQnaModal() {
  const modal = document.getElementById("qna-modal");
  modal.classList.add("hidden");
  modal.classList.remove("flex");
}
