/**
 * 어드민 사이드바 동작 초기화
 *
 * 이 파일은 두 가지 기능을 담당한다.
 *  1. 햄버거 버튼으로 사이드바 전체를 숨기고 보이는 토글
 *  2. 섹션 타이틀 클릭으로 메뉴 그룹을 접고 펼치는 아코디언
 *
 * FOUC(스타일 없는 콘텐츠 노출) 방지 전략:
 *  - 초기 상태 적용은 admin-layout.html의 <head> 인라인 스크립트가 담당한다.
 *    (이 파일은 defer로 로드되어 DOM 파싱 완료 후 실행되므로 초기화 역할 불가)
 *  - 이 파일은 인라인 스크립트가 설정한 임시 상태를 인계받아
 *    이벤트 리스너 등록 및 localStorage 동기화를 처리한다.
 */

initSidebarToggle();
initSectionToggle();

/**
 * 햄버거 버튼(#sidebarToggle) 클릭 시 사이드바를 숨기거나 보이게 한다.
 * 상태는 localStorage("hermesSidebarState")에 "hidden" | "visible"로 저장된다.
 */
function initSidebarToggle() {
  const sidebarToggle = document.getElementById("sidebarToggle");
  const sidebar = document.getElementById("adminSidebar");
  if (!sidebarToggle || !sidebar) return;

  const savedState = localStorage.getItem("hermesSidebarState");

  // 이전 버전에서 "collapsed"로 저장된 값을 "visible"로 마이그레이션
  if (savedState === "collapsed") {
    localStorage.setItem("hermesSidebarState", "visible");
  } else if (savedState === "hidden") {
    sidebar.classList.add("sidebar-hidden");
  }

  /*
    <head> 인라인 스크립트가 FOUC 방지를 위해 붙인 html[data-sidebar="hidden"] 속성을 제거한다.
    이 시점부터는 .sidebar-hidden 클래스가 사이드바 숨김 상태를 관리한다.
  */
  document.documentElement.removeAttribute("data-sidebar");

  sidebarToggle.addEventListener("click", () => {
    sidebar.classList.toggle("sidebar-hidden");
    const isHidden = sidebar.classList.contains("sidebar-hidden");
    localStorage.setItem("hermesSidebarState", isHidden ? "hidden" : "visible");
  });
}

/**
 * 섹션 타이틀(.sidebar-section-toggle) 클릭 시 해당 섹션을 접거나 펼친다.
 * 상태는 localStorage("hermesSectionState")에 { [섹션키]: "collapsed" | "expanded" } 형태로 저장된다.
 * 섹션 키는 각 메뉴 HTML 파일의 data-section-key 속성값과 일치한다.
 */
function initSectionToggle() {
  const sidebar = document.getElementById("adminSidebar");
  if (!sidebar) return;

  const saved = JSON.parse(localStorage.getItem("hermesSectionState") || "{}");

  sidebar.querySelectorAll(".sidebar-section[data-section-key]").forEach((section) => {
    const key = section.dataset.sectionKey;
    const title = section.querySelector(".sidebar-section-toggle");
    if (!title) return;

    if (saved[key] === "collapsed") {
      section.classList.add("section-collapsed");
    }

    title.addEventListener("click", () => {
      section.classList.toggle("section-collapsed");
      saved[key] = section.classList.contains("section-collapsed") ? "collapsed" : "expanded";
      localStorage.setItem("hermesSectionState", JSON.stringify(saved));
    });
  });

  /*
    <head> 인라인 스크립트가 주입한 임시 스타일(sectionInitStyle)을 제거한다.
    이 시점에는 section-collapsed 클래스가 이미 적용되어 있으므로
    임시 스타일 없이도 CSS가 올바르게 동작한다.
    requestAnimationFrame으로 한 프레임 뒤에 제거해 transition이 활성화되기 전에 처리한다.
  */
  requestAnimationFrame(() => {
    const initStyle = document.getElementById("sectionInitStyle");
    if (initStyle) initStyle.remove();
  });
}
