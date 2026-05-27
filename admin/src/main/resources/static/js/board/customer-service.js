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
