const socket = new SockJS("/ws");
const stompClient = Stomp.over(socket);
let currentPage = 1;
let isLoadingMore = false;
let hasMoreMessages = true;
stompClient.debug = null; // 콘솔 노이즈 제거

const roomSn = new URLSearchParams(window.location.search).get("roomSn");
let currentUserId = null;

function formatTime(isoString) {
    const date = isoString ? new Date(isoString) : new Date();
    return date.toLocaleTimeString("ko-KR", {
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

function buildMessageElement(message) {
    const isMine = message.sendrUserId === currentUserId;
    const time = formatTime(message.sndDt);

    const wrapper = document.createElement("div");
    wrapper.className = "msg-enter";

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
        bubble.textContent = message.msgCn;

        row.append(timeSpan, bubble);
        wrapper.append(row);
    } else {
        wrapper.classList.add("flex", "flex-col", "items-start", "gap-1");

        const sender = document.createElement("span");
        sender.className = "text-xs font-semibold text-slate-500 pl-1";
        sender.textContent = message.sendrUserId;

        const row = document.createElement("div");
        row.className = "flex items-end gap-2";

        const bubble = document.createElement("div");
        bubble.className =
            "chat-bubble-other px-4 py-2.5 text-sm max-w-xs lg:max-w-sm break-words";
        bubble.textContent = message.msgCn;

        const timeSpan = document.createElement("span");
        timeSpan.className = "text-[10px] text-slate-400 mb-0.5";
        timeSpan.textContent = time;

        row.append(bubble, timeSpan);
        wrapper.append(sender, row);
    }

    return wrapper;
}

function addMessageToChat(message) {
    const chatMessages = document.getElementById("chatMessages");

    // 첫 메시지면 빈 상태 안내 제거
    const empty = chatMessages.querySelector("[data-empty]");
    if (empty) empty.remove();

    chatMessages.appendChild(buildMessageElement(message));
    chatMessages.scrollTop = chatMessages.scrollHeight;
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
        const res = await fetch(`/chat/more?roomSn=${roomSn}&size=20&page=${currentPage + 1}`);
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
        messages.reverse().forEach((msg) => frag.appendChild(buildMessageElement(msg)));
        chatMessages.prepend(frag);

        // 스크롤 위치 복원 (화면 튀지 않게)
        chatMessages.scrollTop = chatMessages.scrollHeight - prevScrollHeight;
    } catch (e) {
        indicator.remove();
    } finally {
        isLoadingMore = false;
    }
}

document.addEventListener("DOMContentLoaded", () => {
    const chatMessages = document.getElementById("chatMessages");
    const messageInput = document.getElementById("messageInput");

    chatMessages.scrollTop = chatMessages.scrollHeight;

    chatMessages.addEventListener("scroll", () => {
        if (chatMessages.scrollTop === 0) {
            loadMoreMessages();
        }
    });

    // textarea 자동 높이 조절
    messageInput.addEventListener("input", () => {
        messageInput.style.height = "auto";
        messageInput.style.height = messageInput.scrollHeight + "px";
    });

    messageInput.addEventListener("keydown", (e) => {
        if (e.key === "Enter" && !e.shiftKey) {
            e.preventDefault();
            sendMessage();
        }
    });

    stompClient.connect({}, (frame) => {
        currentUserId = frame.headers["user-name"];

        stompClient.subscribe("/topic/messages/" + roomSn, (message) => {
            const body = JSON.parse(message.body);
            addMessageToChat(body);
        });
    });
});
