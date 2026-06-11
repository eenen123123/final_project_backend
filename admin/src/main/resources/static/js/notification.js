(() => {
  const ICONS = {
    CHAT: { icon: "fa-comment", color: "text-violet-400" },
    NOTICE: { icon: "fa-megaphone", color: "text-amber-400" },
    APPROVAL: { icon: "fa-stamp", color: "text-sky-400" },
  };

  let notifications = [];

  function timeAgo(dateStr) {
    if (!dateStr) return "방금 전";
    const diff = Date.now() - new Date(dateStr.replace(" ", "T")).getTime();
    const m = Math.floor(diff / 60000);
    if (m < 1) return "방금 전";
    if (m < 60) return `${m}분 전`;
    const h = Math.floor(m / 60);
    if (h < 24) return `${h}시간 전`;
    return `${Math.floor(h / 24)}일 전`;
  }


  function buildItem(n) {
    const ic = ICONS[n.notiType] || {
      icon: "fa-bell",
      color: "text-slate-400",
    };
    const unread = n.readYn === "N";
    const el = document.createElement("div");
    el.dataset.notiSn = n.notiSn;
    el.className = `flex items-start gap-3 px-4 py-3 cursor-pointer hover:bg-white/5 transition-colors group${unread ? " bg-white/[0.02]" : ""}`;
    el.innerHTML = `
            <div class="mt-0.5 w-7 h-7 rounded-lg bg-white/5 flex items-center justify-center shrink-0">
                <i class="fa-solid ${ic.icon} text-xs ${ic.color}"></i>
            </div>
            <div class="flex-1 min-w-0">
                <p class="text-xs leading-relaxed ${unread ? "font-semibold text-slate-200" : "font-normal text-slate-400"}" style="display:-webkit-box;-webkit-line-clamp:2;-webkit-box-orient:vertical;overflow:hidden">${escHtml(n.notiCn)}</p>
                <p class="text-[10px] text-slate-600 mt-0.5">${timeAgo(n.rcptDt)}</p>
            </div>
            ${unread ? '<span class="w-1.5 h-1.5 bg-rose-500 rounded-full mt-2 shrink-0"></span>' : ""}
            <button class="noti-x opacity-0 group-hover:opacity-100 transition-opacity text-slate-600 hover:text-slate-300 p-1 shrink-0 -mr-1" data-sn="${n.notiSn}">
                <i class="fa-solid fa-xmark text-xs pointer-events-none"></i>
            </button>
        `;
    return el;
  }

  function render() {
    const list = document.getElementById("notiList");
    const empty = document.getElementById("notiEmpty");
    list.innerHTML = "";
    if (notifications.length === 0) {
      list.classList.add("hidden");
      empty.classList.remove("hidden");
    } else {
      list.classList.remove("hidden");
      empty.classList.add("hidden");
      notifications.forEach((n) => list.appendChild(buildItem(n)));
    }
    updateBadge();
  }

  function updateBadge() {
    const count = notifications.filter((n) => n.readYn === "N").length;
    document
      .getElementById("notiBadge")
      .classList.toggle("hidden", count === 0);
    document.getElementById("notiUnreadCount").textContent =
      count > 0 ? `읽지 않은 알림 ${count}개` : "";
  }

  async function markRead(notiSn) {
    await fetch(`/admin/notifications/${notiSn}/read`, { method: "POST" });
    const n = notifications.find((n) => String(n.notiSn) === String(notiSn));
    if (n) n.readYn = "Y";
  }

  function init() {
    const userId = document.getElementById("userId")?.dataset.userId;
    if (!userId) return;

    // 초기 알림 목록 로드
    fetch("/admin/notifications")
      .then((r) => (r.ok ? r.json() : []))
      .then((data) => {
        notifications = data;
        render();
      });

    // 벨 버튼 토글
    const bell = document.getElementById("notificationBell");
    const dropdown = document.getElementById("notiDropdown");
    bell.addEventListener("click", (e) => {
      e.stopPropagation();
      dropdown.classList.toggle("hidden");
    });

    // 외부 클릭 시 닫기
    document.addEventListener("click", (e) => {
      if (!document.getElementById("notification-wrapper").contains(e.target)) {
        dropdown.classList.add("hidden");
      }
    });

    // 알림 아이템 클릭 위임
    document.getElementById("notiList").addEventListener("click", (e) => {
      // X 버튼
      const xBtn = e.target.closest(".noti-x");
      if (xBtn) {
        e.stopPropagation();
        markRead(xBtn.dataset.sn).then(render);
        return;
      }
      // 항목 클릭 → 새 탭 열고 읽음 처리
      const item = e.target.closest("[data-noti-sn]");
      if (!item) return;
      const n = notifications.find(
        (n) => String(n.notiSn) === item.dataset.notiSn,
      );
      if (!n) return;
      if (n.linkUrl) window.open(n.linkUrl, "_blank");
      if (n.readYn === "N") markRead(n.notiSn).then(render);
    });

    // WebSocket 실시간 수신
    const client = Stomp.over(new SockJS("/ws"));
    client.debug = null;
    client.connect({}, () => {
      client.subscribe("/topic/notifications/" + userId, (msg) => {
        const n = JSON.parse(msg.body);
        console.log("Received notification:", n);

        notifications.unshift(n);
        render();
        showHermesToast(n.notiCn, "success");
      });
    });
  }

  document.addEventListener("DOMContentLoaded", init);
})();
