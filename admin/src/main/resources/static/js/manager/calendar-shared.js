/* ============================================================
   매니저 공용 캘린더 컴포넌트
   · 학부모 상담 관리 / 퇴원 방어 화면이 동일하게 사용
   · 통합 엔드포인트(/admin/manager/calendar/events)에서 상담+방어를 함께 표시
   · 고정 DOM id 사용 (cal-grid, cal-year, cal-month, cal-month-picker ...)
   · 사용: SharedCalendar.init({ onEventClick: (ev) => {...} })
============================================================ */

const SharedCalendar = (function () {
  const EVENTS_URL = "/admin/manager/calendar/events";

  let year = new Date().getFullYear();
  let month = new Date().getMonth();
  let pickerYear = year;
  let eventsByDate = {};
  let onEventClick = null;

  const pad2 = (n) => String(n).padStart(2, "0");
  const ymd = (y, m, d) => y + "-" + pad2(m + 1) + "-" + pad2(d);
  const esc = (s) =>
    s == null ? "" : String(s).replace(/[&<>"]/g, (c) => ({ "&": "&amp;", "<": "&lt;", ">": "&gt;", '"': "&quot;" }[c]));
  const sourceClass = (src) => (src === "방어" ? "src-retention" : "src-consult");

  function init(opts) {
    onEventClick = opts && typeof opts.onEventClick === "function" ? opts.onEventClick : null;
    document.addEventListener("click", (e) => {
      const picker = document.getElementById("cal-month-picker");
      const btn = document.getElementById("cal-month-picker-btn");
      if (picker && btn && !picker.contains(e.target) && !btn.contains(e.target)) picker.classList.add("hidden");
    });
    load();
  }

  function load() {
    const firstDow = new Date(year, month, 1).getDay();
    const gridStart = new Date(year, month, 1 - firstDow);
    const gridEnd = new Date(gridStart);
    gridEnd.setDate(gridEnd.getDate() + 42);
    const startStr = ymd(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate());
    const endStr = ymd(gridEnd.getFullYear(), gridEnd.getMonth(), gridEnd.getDate());
    fetch(`${EVENTS_URL}?start=${startStr}&end=${endStr}`)
      .then((r) => r.json())
      .then((list) => {
        eventsByDate = {};
        list.forEach((ev) => {
          const d = (ev.dt || "").substring(0, 10);
          if (!d) return;
          (eventsByDate[d] = eventsByDate[d] || []).push(ev);
        });
        render();
      });
  }
  function reload() {
    load();
  }

  function render() {
    document.getElementById("cal-year").textContent = year;
    document.getElementById("cal-month").textContent = pad2(month + 1);

    const today = new Date();
    const todayStr = ymd(today.getFullYear(), today.getMonth(), today.getDate());
    const firstDow = new Date(year, month, 1).getDay();
    const daysInMonth = new Date(year, month + 1, 0).getDate();
    const daysInPrev = new Date(year, month, 0).getDate();

    const cells = [];
    for (let i = firstDow - 1; i >= 0; i--)
      cells.push({ day: daysInPrev - i, year: month === 0 ? year - 1 : year, month: month === 0 ? 11 : month - 1, other: true });
    for (let d = 1; d <= daysInMonth; d++) cells.push({ day: d, year: year, month: month, other: false });
    const rem = 42 - cells.length;
    for (let d = 1; d <= rem; d++)
      cells.push({ day: d, year: month === 11 ? year + 1 : year, month: month === 11 ? 0 : month + 1, other: true });

    const grid = document.getElementById("cal-grid");
    grid.innerHTML = "";
    cells.forEach((cell, idx) => {
      const ds = ymd(cell.year, cell.month, cell.day);
      const isSun = idx % 7 === 0;
      const isSat = idx % 7 === 6;
      const isToday = ds === todayStr;
      const dayEvents = cell.other ? [] : eventsByDate[ds] || [];

      const div = document.createElement("div");
      div.className = "cal-cell border-r border-b border-slate-100 p-1.5 flex flex-col gap-0.5 transition-colors";
      if (cell.other) div.classList.add("other-month");
      else {
        if (isSun) div.classList.add("sun-bg");
        if (isToday) div.classList.add("today-cell");
      }

      const dateDiv = document.createElement("div");
      dateDiv.className = "flex items-center gap-1 mb-0.5";
      const span = document.createElement("span");
      span.className = "text-xs font-medium";
      span.textContent = cell.day;
      if (cell.other) span.classList.add("text-slate-300");
      else if (isSun) span.classList.add("text-red-400");
      else if (isSat) span.classList.add("text-blue-400");
      else span.classList.add("text-slate-700");
      dateDiv.appendChild(span);
      if (isToday && !cell.other) {
        const tb = document.createElement("span");
        tb.className = "text-[10px] font-bold px-1.5 py-0.5 rounded-full bg-violet-500 text-white leading-none";
        tb.textContent = "Today";
        dateDiv.appendChild(tb);
      }
      div.appendChild(dateDiv);

      dayEvents.slice(0, 3).forEach((ev) => {
        const bar = document.createElement("div");
        bar.className = "event-bar " + sourceClass(ev.source);
        const label = `[${ev.source}] ` + (ev.title || "");
        bar.textContent = label.length > 14 ? label.slice(0, 14) + "..." : label;
        bar.title = `[${ev.source}] ${esc(ev.title)}${ev.label ? " · " + esc(ev.label) : ""}`;
        if (onEventClick) {
          bar.onclick = (e) => {
            e.stopPropagation();
            onEventClick(ev);
          };
        }
        div.appendChild(bar);
      });
      if (dayEvents.length > 3) {
        const more = document.createElement("span");
        more.className = "text-[10px] text-slate-400";
        more.textContent = "+" + (dayEvents.length - 3) + "건";
        div.appendChild(more);
      }
      grid.appendChild(div);
    });
  }

  function prev() {
    if (month === 0) {
      year--;
      month = 11;
    } else month--;
    load();
  }
  function next() {
    if (month === 11) {
      year++;
      month = 0;
    } else month++;
    load();
  }
  function today() {
    const t = new Date();
    year = t.getFullYear();
    month = t.getMonth();
    load();
  }
  function togglePicker() {
    const p = document.getElementById("cal-month-picker");
    p.classList.toggle("hidden");
    pickerYear = year;
    renderPicker();
  }
  function renderPicker() {
    document.getElementById("cal-picker-year").textContent = pickerYear;
    const c = document.getElementById("cal-picker-months");
    c.innerHTML = "";
    for (let m = 0; m < 12; m++) {
      const b = document.createElement("button");
      b.textContent = m + 1 + "월";
      b.className =
        "py-1.5 rounded-lg text-xs font-medium transition-colors cursor-pointer " +
        (pickerYear === year && m === month ? "bg-violet-600 text-white" : "hover:bg-slate-100 text-slate-700");
      b.onclick = () => {
        year = pickerYear;
        month = m;
        document.getElementById("cal-month-picker").classList.add("hidden");
        load();
      };
      c.appendChild(b);
    }
  }
  function pickerPrevYear() {
    pickerYear--;
    renderPicker();
  }
  function pickerNextYear() {
    pickerYear++;
    renderPicker();
  }

  return { init, reload, prev, next, today, togglePicker, pickerPrevYear, pickerNextYear };
})();
window.SharedCalendar = SharedCalendar;
