const socket = new SockJS("/ws");
const stompClient = Stomp.over(socket);
let currentPage = 1;
let isLoadingMore = false;
let hasMoreMessages = true;
let isFileSelected = false;
// 이미지가 비동기로 로드될 때 scrollHeight가 늘어나 스크롤이 위로 밀리는 현상 방지용
// 사용자가 직접 스크롤을 올렸을 때는 재스크롤하지 않기 위해 플래그로 관리
let pinnedToBottom = false;

stompClient.debug = null; // 콘솔 노이즈 제거

const roomSn = new URLSearchParams(window.location.search).get("roomSn");
let currentUserId = null;

function formatTime(isoString) {
  const date = isoString ? new Date(isoString) : new Date();
  return date.toLocaleTimeString("ko-KR", {
    // 연월일
    year: "numeric",
    month: "2-digit",
    day: "2-digit",
    hour: "2-digit",
    minute: "2-digit",
    hour12: false,
  });
}

function sendMessage() {
  const messageInput = document.getElementById("messageInput");
  const content = messageInput.value.trim();
  if (!content || !currentUserId) return;

  stompClient.send(
    "/app/chat/send",
    {},
    JSON.stringify({
      roomSn: roomSn,
      sendrUserId: currentUserId,
      msgTypeCd: "01",
      msgCn: content,
    }),
  );
  messageInput.value = "";
  messageInput.style.height = "auto";
}

// msgTypeCd: 01=텍스트, 02=이미지(미리보기), 03=파일(다운로드 링크)
function buildBubbleContent(message, bubbleEl) {
  const type = message.msgTypeCd;
  if (type === "02") {
    const link = document.createElement("a");
    link.href = message.msgCn;
    link.target = "_blank";
    const img = document.createElement("img");
    img.src = message.msgCn;
    img.alt = message.fileNm || "이미지";
    img.className = "max-w-full rounded-lg block";
    img.loading = "lazy";
    link.appendChild(img);
    bubbleEl.appendChild(link);
  } else if (type === "03") {
    const link = document.createElement("a");
    link.href = message.msgCn;
    link.target = "_blank";
    link.className = "flex items-center gap-2 hover:underline";
    link.innerHTML = `<i class="fa-solid fa-file text-sm shrink-0"></i><span class="truncate">${message.fileNm || message.msgCn}</span>`;
    bubbleEl.appendChild(link);
  } else {
    bubbleEl.textContent = message.msgCn;
  }
}

function buildMessageElement(message) {
  const isMine = message.sendrUserId === currentUserId;
  const time = formatTime(message.sndDt);

  const wrapper = document.createElement("div");
  wrapper.className = "msg-enter";
  wrapper.setAttribute("data-msg-sn", message.msgSn);

  if (isMine) {
    wrapper.classList.add("flex", "flex-col", "items-end", "gap-1");

    const row = document.createElement("div");
    row.className = "flex items-end gap-2";

    const timeSpan = document.createElement("span");
    timeSpan.className = "text-[10px] text-slate-400 mb-0.5";
    timeSpan.textContent = time;

    const bubble = document.createElement("div");
    bubble.className =
      "chat-bubble-mine px-4 py-2.5 text-sm max-w-xs lg:max-w-sm break-words";
    buildBubbleContent(message, bubble);

    row.append(timeSpan, bubble);
    wrapper.append(row);
  } else {
    wrapper.classList.add("flex", "flex-col", "items-start", "gap-1");

    const senderInfo = document.createElement("div");
    senderInfo.className = "flex items-center gap-1.5 pl-1";

    if (message.partDeptNm) {
      const deptBadge = document.createElement("span");
      deptBadge.className =
        "text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-violet-50 text-violet-500";
      deptBadge.textContent = message.partDeptNm;
      senderInfo.appendChild(deptBadge);
    }
    if (message.partJbgrNm) {
      const jbgrBadge = document.createElement("span");
      jbgrBadge.className =
        "text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-slate-100 text-slate-500";
      jbgrBadge.textContent = message.partJbgrNm;
      senderInfo.appendChild(jbgrBadge);
    }

    const nameSpan = document.createElement("span");
    nameSpan.className = "text-xs font-semibold text-slate-700";
    nameSpan.textContent = message.userName || message.sendrUserId;

    const idSpan = document.createElement("span");
    idSpan.className = "text-xs text-slate-400";
    idSpan.textContent = `(${message.sendrUserId})`;

    senderInfo.append(nameSpan, idSpan);

    const row = document.createElement("div");
    row.className = "flex items-end gap-2";

    const bubble = document.createElement("div");
    bubble.className =
      "chat-bubble-other px-4 py-2.5 text-sm max-w-xs lg:max-w-sm break-words";
    buildBubbleContent(message, bubble);

    const timeSpan = document.createElement("span");
    timeSpan.className = "text-[10px] text-slate-400 mb-0.5";
    timeSpan.textContent = time;

    row.append(bubble, timeSpan);
    wrapper.append(senderInfo, row);
  }

  return wrapper;
}

function scrollToBottomIfPinned(chatMessages) {
  if (pinnedToBottom) chatMessages.scrollTop = chatMessages.scrollHeight;
}

function attachImageScrollListeners(el, chatMessages) {
  el.querySelectorAll("img").forEach((img) => {
    if (!img.complete) {
      img.addEventListener("load", () => scrollToBottomIfPinned(chatMessages));
      img.addEventListener("error", () => scrollToBottomIfPinned(chatMessages));
    }
  });
}

function addMessageToChat(message) {
  const chatMessages = document.getElementById("chatMessages");

  // 첫 메시지면 빈 상태 안내 제거
  const empty = chatMessages.querySelector("[data-empty]");
  if (empty) empty.remove();

  const el = buildMessageElement(message);
  chatMessages.appendChild(el);
  scrollToBottomIfPinned(chatMessages);
  attachImageScrollListeners(el, chatMessages);
}

async function loadMoreMessages() {
  if (isLoadingMore || !hasMoreMessages) return;
  isLoadingMore = true;

  const chatMessages = document.getElementById("chatMessages");

  // 로딩 인디케이터
  const indicator = document.createElement("div");
  indicator.className = "text-center text-xs text-slate-400 py-2";
  indicator.textContent = "불러오는 중...";
  chatMessages.prepend(indicator);

  const prevScrollHeight = chatMessages.scrollHeight;

  try {
    const res = await fetch(
      `/chat/more?roomSn=${roomSn}&size=20&page=${currentPage + 1}`,
    );
    const messages = await res.json();

    indicator.remove();

    if (messages.length === 0) {
      hasMoreMessages = false;
      const end = document.createElement("div");
      end.className = "text-center text-xs text-slate-300 py-2";
      end.textContent = "이전 메시지가 없습니다.";
      chatMessages.prepend(end);
      return;
    }

    currentPage++;

    // DESC로 받은 메시지를 ASC로 역순 정렬 후 Fragment로 한 번에 prepend
    const frag = document.createDocumentFragment();
    messages
      .reverse()
      .forEach((msg) => frag.appendChild(buildMessageElement(msg)));
    chatMessages.prepend(frag);

    // 스크롤 위치 복원 (화면 튀지 않게)
    chatMessages.scrollTop = chatMessages.scrollHeight - prevScrollHeight;
  } catch (e) {
    indicator.remove();
  } finally {
    isLoadingMore = false;
  }
}
function clearFileSelection() {
  document.getElementById("fileInput").value = "";
  const preview = document.getElementById("filePreview");
  preview.classList.add("hidden");
  preview.classList.remove("flex");
  const messageInput = document.getElementById("messageInput");
  messageInput.value = "";
  messageInput.disabled = false;
  isFileSelected = false;
}

function formatFileSize(bytes) {
  if (bytes < 1024) return bytes + " B";
  if (bytes < 1024 * 1024) return (bytes / 1024).toFixed(1) + " KB";
  return (bytes / (1024 * 1024)).toFixed(1) + " MB";
}

const ALLOWED_MIME_TYPES = new Set([
  "image/jpeg",
  "image/png",
  "image/gif",
  "image/webp",
  "application/pdf",
  "video/mp4",
  "video/webm",
  "video/ogg",
  "application/zip",
  "application/x-zip-compressed",
]);

function showToast(msg) {
  const toast = document.createElement("div");
  toast.className =
    "fixed bottom-24 left-1/2 -translate-x-1/2 bg-rose-500 text-white text-sm px-4 py-2 rounded-xl shadow-lg z-50";
  toast.textContent = msg;
  document.body.appendChild(toast);
  setTimeout(() => toast.remove(), 3000);
}

async function uploadAndSendFile() {
  const fileInput = document.getElementById("fileInput");
  const file = fileInput.files[0];
  if (!file) return;

  if (!ALLOWED_MIME_TYPES.has(file.type)) {
    showToast("PDF, 이미지, 동영상, ZIP 파일만 업로드할 수 있습니다.");
    return;
  }

  const sendBtn = document.querySelector("button[data-send]");
  if (sendBtn) sendBtn.disabled = true;

  const formData = new FormData();
  formData.append("file", file);
  formData.append("roomSn", roomSn);

  try {
    const res = await fetch("/chat/file", { method: "POST", body: formData });
    if (!res.ok) {
      const data = await res.json().catch(() => null);
      showToast(data?.message || "파일 업로드에 실패했습니다.");
      return;
    }
    clearFileSelection();
  } catch (e) {
    showToast("파일 업로드 중 오류가 발생했습니다.");
  } finally {
    if (sendBtn) sendBtn.disabled = false;
  }
}

document.addEventListener("DOMContentLoaded", () => {
  function getMaxMsgSn() {
    const snList = [...document.querySelectorAll("[data-msg-sn]")]
      .map((el) => Number(el.dataset.msgSn))
      .filter(Boolean);

    return snList.length ? Math.max(...snList) : null;
  }
  const chatMessages = document.getElementById("chatMessages");
  const messageInput = document.getElementById("messageInput");

  chatMessages.scrollTop = chatMessages.scrollHeight;
  pinnedToBottom = true;

  // 페이지 로드 시 이미지가 늦게 로드되면 다시 하단 고정
  attachImageScrollListeners(chatMessages, chatMessages);

  chatMessages.addEventListener("scroll", () => {
    pinnedToBottom =
      chatMessages.scrollHeight -
        chatMessages.scrollTop -
        chatMessages.clientHeight <
      20;
    if (chatMessages.scrollTop === 0) {
      loadMoreMessages();
    }
  });
  document.getElementById("fileInput").addEventListener("change", function () {
    const file = this.files[0];
    const preview = document.getElementById("filePreview");
    if (!file) {
      preview.classList.add("hidden");
      preview.classList.remove("flex");
      messageInput.value = "";
      messageInput.disabled = false;
      return;
    }
    document.getElementById("filePreviewName").textContent = file.name;
    document.getElementById("filePreviewSize").textContent = formatFileSize(
      file.size,
    );
    preview.classList.remove("hidden");
    preview.classList.add("flex");
    messageInput.value = file.name;
    messageInput.disabled = true;
    isFileSelected = true;
  });

  // textarea 자동 높이 조절
  let isComposing = false;
  messageInput.addEventListener("compositionstart", () => {
    isComposing = true;
  });
  messageInput.addEventListener("compositionend", () => {
    isComposing = false;
    messageInput.style.height = "auto";
    messageInput.style.height = messageInput.scrollHeight + "px";
  });
  messageInput.addEventListener("input", () => {
    if (isComposing) return;
    messageInput.style.height = "auto";
    messageInput.style.height = messageInput.scrollHeight + "px";
  });

  messageInput.addEventListener("keydown", (e) => {
    if (e.key === "Enter" && !e.shiftKey) {
      e.preventDefault();
      if (isFileSelected) {
        uploadAndSendFile();
      } else {
        sendMessage();
      }
    }
  });

  stompClient.connect({}, (frame) => {
    currentUserId = frame.headers["user-name"];

    stompClient.subscribe("/topic/messages/" + roomSn, (message) => {
      const body = JSON.parse(message.body);
      addMessageToChat(body);

      if (body.msgSn) {
        fetch(`/chat/read?roomSn=${roomSn}&msgSn=${getMaxMsgSn()}`, {
          method: "POST",
        });
      }
    });
  });
});
