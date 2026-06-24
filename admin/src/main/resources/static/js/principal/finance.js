/* 매출 및 재무 분석 (원장 · 수입 중심)
   · 원비 수납(TUITION_PAYMENT) + 온라인 결제(ORDERS) 합산
   · 페이징/검색은 서버(컨트롤러)에서 처리, select 옵션은 공통코드(218 연도 / 223 결제수단) */

const FIN_BASE = '/admin/finance';
const TX_SIZE = 10;
const TX_BLOCK = 5;
let txPage = 1;

const CATS = [
  { key: 'tuition',  name: '원비',  dot: 'bg-violet-500', color: 'rgba(124,58,237,0.85)' },
  { key: 'course',   name: '강의',  dot: 'bg-sky-400',    color: 'rgba(56,189,248,0.85)' },
  { key: 'textbook', name: '교재',  dot: 'bg-amber-400',  color: 'rgba(251,191,36,0.85)' },
  { key: 'etc',      name: '기타',  dot: 'bg-emerald-400', color: 'rgba(52,211,153,0.85)' },
];

let monthlyChart = null;
let ratioChart = null;

/* ── 유틸 ── */
const won = n => Number(n || 0).toLocaleString('ko-KR');
function p2(n) { return String(n).padStart(2, '0'); }
function fmtDate(v) {
  if (!v) return '-';
  if (Array.isArray(v)) return `${v[0]}-${p2(v[1])}-${p2(v[2])}`;
  return String(v).substring(0, 10);
}
function pct(part, total) { return total > 0 ? Math.round(part / total * 1000) / 10 : 0; }

/* ── 공통코드 select 채움 (값 선택 후 CustomSelect 초기화) ── */
async function fillCommonSelect(id, clCode, { allLabel = null, selected = null } = {}) {
  const el = document.getElementById(id);
  if (!el) return;
  try {
    const res = await fetch('/admin/common-codes/options/' + encodeURIComponent(clCode));
    const codes = await res.json();
    el.innerHTML = '';
    if (allLabel !== null) el.add(new Option(allLabel, ''));
    codes.forEach(c => el.add(new Option(c.comCdNm, c.comCd)));
    if (selected !== null) el.value = selected;
  } catch (e) {
    console.warn('[finance] 공통코드 로딩 실패', clCode, e);
  }
  if (window.initCustomSelect) window.initCustomSelect(el);
}

/* ── 탭 전환 ── */
function switchFinTab(tabId, btn) {
  document.querySelectorAll('.fin-tab-panel').forEach(p => p.classList.add('hidden'));
  document.querySelectorAll('.fin-tab-btn').forEach(b => {
    b.classList.remove('text-violet-600', 'border-violet-500', 'bg-violet-50/60');
    b.classList.add('text-slate-500', 'border-transparent');
  });
  document.getElementById(tabId).classList.remove('hidden');
  btn.classList.remove('text-slate-500', 'border-transparent');
  btn.classList.add('text-violet-600', 'border-violet-500', 'bg-violet-50/60');
  if (tabId === 'tab-trend' && monthlyChart) monthlyChart.resize();
  if (tabId === 'tab-ratio' && ratioChart) ratioChart.resize();
}

/* ── 차트 초기화 ── */
function initCharts() {
  const mc = document.getElementById('monthlyChart').getContext('2d');
  monthlyChart = new Chart(mc, {
    type: 'bar',
    data: {
      labels: Array.from({ length: 12 }, (_, i) => (i + 1) + '월'),
      datasets: CATS.map(c => ({
        label: c.name, data: Array(12).fill(0),
        backgroundColor: c.color, borderRadius: 6, borderSkipped: false,
      })),
    },
    options: {
      responsive: true, maintainAspectRatio: false,
      plugins: {
        legend: { position: 'top', labels: { font: { size: 11 }, boxWidth: 12 } },
        tooltip: { callbacks: { label: ctx => ` ${ctx.dataset.label}: ${won(ctx.parsed.y)}원` } },
      },
      scales: {
        x: { stacked: true, grid: { display: false }, ticks: { font: { size: 12 } } },
        y: {
          stacked: true, grid: { color: 'rgba(148,163,184,0.1)' },
          ticks: { font: { size: 11 }, callback: v => (v / 10000) + '만' },
        },
      },
    },
  });

  const rc = document.getElementById('ratioChart').getContext('2d');
  ratioChart = new Chart(rc, {
    type: 'doughnut',
    data: {
      labels: CATS.map(c => c.name),
      datasets: [{ data: [0, 0, 0, 0], backgroundColor: CATS.map(c => c.color), borderWidth: 0, hoverOffset: 8 }],
    },
    options: {
      responsive: true, cutout: '68%',
      plugins: {
        legend: { display: false },
        tooltip: { callbacks: { label: ctx => ` ${ctx.label}: ${won(ctx.parsed)}원` } },
      },
    },
  });
}

/* ── 요약 카드 + 항목 비율 (이번 달) ── */
function loadSummary(ym) {
  fetch(`${FIN_BASE}/summary?ym=${ym}`).then(r => r.json()).then(d => {
    const s = d.sales || {};
    const total = s.total || 0;
    const onlineSum = (s.course || 0) + (s.textbook || 0);

    document.getElementById('card-total').textContent = won(total);
    document.getElementById('card-tuition').textContent = won(s.tuition);
    document.getElementById('card-tuition-pct').textContent = `전체의 ${pct(s.tuition, total)}%`;
    document.getElementById('card-online').textContent = won(onlineSum);
    document.getElementById('card-online-pct').textContent = `전체의 ${pct(onlineSum, total)}%`;

    const growthEl = document.getElementById('card-growth');
    if (d.growthRate == null) {
      growthEl.textContent = total > 0 ? '신규' : '-';
      growthEl.className = 'text-2xl font-bold text-slate-500';
    } else {
      const up = d.growthRate >= 0;
      growthEl.textContent = (up ? '+' : '') + (Math.round(d.growthRate * 10) / 10) + '%';
      growthEl.className = 'text-2xl font-bold ' + (up ? 'text-emerald-600' : 'text-rose-500');
    }
    document.getElementById('card-growth-prev').textContent = `전월 ${won(d.prevTotal)}원`;

    // 도넛 + 항목 상세
    ratioChart.data.datasets[0].data = CATS.map(c => s[c.key] || 0);
    ratioChart.update();

    const rows = CATS.map(c => `
      <div class="flex items-center justify-between p-4 rounded-xl bg-slate-50 border border-slate-100">
        <div class="flex items-center gap-3">
          <span class="w-3 h-3 rounded-full ${c.dot} shrink-0"></span>
          <p class="text-sm font-semibold text-slate-700">${c.name}</p>
        </div>
        <div class="text-right">
          <p class="text-sm font-bold text-slate-800 font-mono">${won(s[c.key])}원</p>
          <p class="text-xs text-slate-500 font-bold mt-0.5">${pct(s[c.key], total)}%</p>
        </div>
      </div>`).join('');
    document.getElementById('ratio-breakdown').innerHTML =
      '<h3 class="text-sm font-bold text-slate-700 mb-4">항목 상세 분석</h3>' + rows + `
      <div class="flex items-center justify-between p-4 rounded-xl bg-violet-600 text-white mt-2">
        <p class="text-sm font-bold">총 수입 합계</p>
        <p class="text-base font-bold font-mono">${won(total)}원</p>
      </div>`;
  });
}

/* ── 월별 매출 추이 (연도) ── */
function loadMonthly(year) {
  fetch(`${FIN_BASE}/monthly?year=${year}`).then(r => r.json()).then(d => {
    const items = d.items || []; // 12개월 정렬
    CATS.forEach((c, ci) => { monthlyChart.data.datasets[ci].data = items.map(m => m[c.key] || 0); });
    monthlyChart.update();

    const now = new Date();
    const curMm = (year === String(now.getFullYear())) ? p2(now.getMonth() + 1) : null;

    // 데이터 있는 달만 최신순, 전월 대비는 직전 달(시계열) 기준
    const rows = [];
    for (let i = items.length - 1; i >= 0; i--) {
      const m = items[i];
      if ((m.total || 0) === 0) continue;
      const prev = i > 0 ? items[i - 1] : null;
      let diff;
      if (!prev || (prev.total || 0) === 0) {
        diff = '<span class="inline-flex items-center gap-1 text-xs font-medium text-slate-400"><i class="fa-solid fa-minus text-[10px]"></i> 기준</span>';
      } else {
        const rate = Math.round((m.total - prev.total) / prev.total * 1000) / 10;
        const up = rate >= 0;
        diff = `<span class="inline-flex items-center gap-1 text-xs font-bold ${up ? 'text-emerald-600' : 'text-rose-500'}">
                  <i class="fa-solid fa-arrow-${up ? 'up' : 'down'} text-[10px]"></i>${up ? '+' : ''}${rate}%</span>`;
      }
      const isCur = m.mm === curMm;
      rows.push(`
        <tr class="hover:bg-slate-50/50 transition-colors ${isCur ? 'bg-violet-50/40 font-semibold' : ''}">
          <td class="py-3.5 px-6 ${isCur ? 'text-violet-700 font-bold' : ''}">${(+m.mm)}월${isCur ? ' <span class="text-xs text-violet-400">(이번 달)</span>' : ''}</td>
          <td class="py-3.5 px-6 text-right font-mono">${won(m.tuition)}</td>
          <td class="py-3.5 px-6 text-right font-mono">${won(m.course)}</td>
          <td class="py-3.5 px-6 text-right font-mono">${won(m.textbook)}</td>
          <td class="py-3.5 px-6 text-right font-mono">${won(m.etc)}</td>
          <td class="py-3.5 px-6 text-right font-mono font-bold text-slate-900">${won(m.total)}</td>
          <td class="py-3.5 px-6 text-center">${diff}</td>
        </tr>`);
    }
    document.getElementById('trend-tbody').innerHTML = rows.length
      ? rows.join('')
      : '<tr><td colspan="7" class="text-center py-10 text-slate-400 text-sm">해당 연도 매출 데이터가 없습니다.</td></tr>';
  });
}

/* ── 수입 거래 내역 ── */
function renderTxPaging(total, page) {
  const totalPage = Math.max(1, Math.ceil(total / TX_SIZE));
  const startPage = Math.floor((page - 1) / TX_BLOCK) * TX_BLOCK + 1;
  const endPage = Math.min(startPage + TX_BLOCK - 1, totalPage);
  const box = document.getElementById('tx-pagination');
  const BASE = 'w-8 h-8 rounded-lg text-xs flex items-center justify-center';
  const btn = (label, p, disabled, active) =>
    `<button ${disabled ? 'disabled' : ''} onclick="loadTransactions(${p})"
      class="${BASE} ${active ? 'bg-violet-600 text-white font-bold' : 'text-slate-400 hover:bg-slate-100'} ${disabled ? 'opacity-40 cursor-not-allowed' : ''}">${label}</button>`;
  let html = btn('<i class="fa-solid fa-chevron-left"></i>', page - 1, page <= 1, false);
  for (let p = startPage; p <= endPage; p++) html += btn(p, p, false, p === page);
  html += btn('<i class="fa-solid fa-chevron-right"></i>', page + 1, page >= totalPage, false);
  box.innerHTML = html;

  const info = document.getElementById('tx-info');
  const from = total === 0 ? 0 : (page - 1) * TX_SIZE + 1;
  const to = Math.min(page * TX_SIZE, total);
  info.textContent = total === 0 ? '데이터가 없습니다.' : `전체 ${total}건 중 ${from} – ${to} 표시`;
}

function loadTransactions(page) {
  txPage = page;
  const ym = document.getElementById('tx-month').value;
  const mthd = document.getElementById('tx-mthd').value;
  const params = new URLSearchParams({ page, screenSize: TX_SIZE });
  if (ym) params.set('ym', ym);
  if (mthd) params.set('payMthdCd', mthd);

  // 선택 월 총 수입 배너
  if (ym) {
    fetch(`${FIN_BASE}/summary?ym=${ym}`).then(r => r.json())
      .then(d => { document.getElementById('tx-total').textContent = won((d.sales || {}).total); });
  }

  fetch(`${FIN_BASE}/transactions?${params}`).then(r => r.json()).then(d => {
    document.getElementById('tx-count').textContent = d.totalCount;
    const tbody = document.getElementById('tx-tbody');
    if (!d.items.length) {
      tbody.innerHTML = '<tr><td colspan="6" class="text-center py-10 text-slate-400 text-sm">거래 내역이 없습니다.</td></tr>';
      renderTxPaging(0, 1);
      return;
    }
    tbody.innerHTML = d.items.map(t => {
      const online = t.source === '온라인결제';
      const badge = `<span class="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-semibold ${online ? 'bg-sky-50 text-sky-700' : 'bg-violet-50 text-violet-700'}">${t.source}</span>`;
      return `
        <tr class="hover:bg-slate-50/50 transition-colors">
          <td class="py-3.5 px-6 text-xs text-slate-500 font-mono whitespace-nowrap">${fmtDate(t.txDt)}</td>
          <td class="py-3.5 px-6 text-center">${badge}</td>
          <td class="py-3.5 px-6 font-medium">${t.category || '-'}</td>
          <td class="py-3.5 px-6 text-slate-600 text-xs">${t.purchaser || '-'}</td>
          <td class="py-3.5 px-6 text-center text-xs text-slate-600">${t.payMethod || '-'}</td>
          <td class="py-3.5 px-6 text-right font-mono font-semibold text-emerald-600">+${won(t.amount)}</td>
        </tr>`;
    }).join('');
    renderTxPaging(d.totalCount, page);
  });
}

/* ── 초기화 ── */
document.addEventListener('DOMContentLoaded', async function () {
  const now = new Date();
  const curYear = String(now.getFullYear());
  const curYm = `${curYear}-${p2(now.getMonth() + 1)}`;

  initCharts();
  await fillCommonSelect('yearSelect', '218', { selected: curYear });
  await fillCommonSelect('tx-mthd', '223', { allLabel: '전체 결제수단' });
  document.getElementById('tx-month').value = curYm;

  switchFinTab('tab-trend', document.querySelector('.fin-tab-btn[data-tab="tab-trend"]'));
  loadSummary(curYm);
  loadMonthly(document.getElementById('yearSelect').value || curYear);
  loadTransactions(1);
});
