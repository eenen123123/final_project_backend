/**
 * HERMES 글로벌 공통 코어 엔진 스크립트
 * (특정 레이아웃 종속 이벤트는 모두 layout.js로 격리)
 */

/**
 * 글로벌 커스텀 토스트 알림 시스템 (어느 화면에서나 자바스크립트 한 줄로 즉시 호출 가능)
 * @param {string} message - 표시할 메시지 내용
 * @param {'success' | 'error'} type - 알림 종류 유형
 */
function showHermesToast(message, type = "success") {
  let container = document.getElementById("hm-toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "hm-toast-container";
    container.className =
      "fixed bottom-6 right-6 z-50 space-y-3 pointer-events-none";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `px-5 py-3.5 rounded-xl shadow-xl text-xs font-bold text-white transition-all duration-300 transform translate-y-4 opacity-0 flex items-center gap-3 pointer-events-auto ${
    type === "success" ? "bg-slate-900" : "bg-rose-600"
  }`;

  const icon =
    type === "success"
      ? '<i class="fa-solid fa-circle-check text-emerald-400 text-sm"></i>'
      : '<i class="fa-solid fa-circle-exclamation text-white text-sm"></i>';

  // 메시지 내 줄바꿈을 <br> 태그로 변환하여 HTML로 표시
  const brokenMessage = message.replaceAll("\n", "<br>");

  toast.innerHTML = `${icon} <span>${brokenMessage}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.remove("translate-y-4", "opacity-0");
  }, 10);

  setTimeout(() => {
    toast.classList.add("opacity-0", "translate-y-4");
    setTimeout(() => {
      toast.remove();
      if (container.children.length === 0) container.remove();
    }, 300);
  }, 3000);
}
