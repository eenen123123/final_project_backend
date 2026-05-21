/**
 * HERMES 어드민 사이드바 인터랙션 스크립트
 */

// 페이지 로드 상태에 따라 안전하게 사이드바 토글 바인딩 실행
if (document.readyState === "loading") {
  document.addEventListener("DOMContentLoaded", initSidebarToggle);
} else {
  initSidebarToggle();
}

/**
 * 햄버거 버튼 클릭 시 사이드바 접힘/열림 상태 제어 및 로컬스토리지 상태 유지
 */
function initSidebarToggle() {
  const sidebarToggle = document.getElementById("sidebarToggle");
  const sidebar = document.getElementById("adminSidebar");

  // 레이아웃이 없는 페이지(예: 로그인)일 경우 예외 처리 후 즉시 종료
  if (!sidebarToggle || !sidebar) return;

  // 초기 로드 시 브라우저에 저장된 이전 설정 상태 복원
  if (localStorage.getItem("hermesSidebarState") === "collapsed") {
    sidebar.classList.add("sidebar-collapsed");
  }

  // 토글 버튼 클릭 이벤트 바인딩
  sidebarToggle.addEventListener("click", () => {
    sidebar.classList.toggle("sidebar-collapsed");
    const isCollapsed = sidebar.classList.contains("sidebar-collapsed");
    localStorage.setItem(
      "hermesSidebarState",
      isCollapsed ? "collapsed" : "expanded",
    );
  });
}
