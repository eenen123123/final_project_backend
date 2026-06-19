/* 증명서 발급 (직원 셀프서비스) — 신청·발급·조회·출력(1회) */
const CERT_BASE = '/admin/certificate';
const CERT_SIZE = 10;
let ciPage = 1;

/* ── 날짜 포맷 (ISO 문자열/배열 모두 대응) ── */
function fmtDateTime(v) {
  if (!v) return '-';
  if (Array.isArray(v)) {
    const [y, mo, d, h = 0, mi = 0] = v;
    return `${y}-${p2(mo)}-${p2(d)} ${p2(h)}:${p2(mi)}`;
  }
  return String(v).replace('T', ' ').substring(0, 16);
}
const p2 = n => String(n).padStart(2, '0');

/* ── 페이징 버튼 (블록 5) ── */
function renderPaging(containerId, total, page, screenSize, onMove) {
  const totalPage = Math.max(1, Math.ceil(total / screenSize));
  const block = 5;
  const start = Math.floor((page - 1) / block) * block + 1;
  const end = Math.min(start + block - 1, totalPage);
  const box = document.getElementById(containerId);
  const btn = (label, p, disabled, active) =>
    `<button ${disabled ? 'disabled' : ''} onclick="${onMove}(${p})"
      class="w-8 h-8 rounded-lg text-xs flex items-center justify-center ${active ? 'bg-violet-600 text-white font-bold' : 'border border-slate-200 text-slate-400 hover:bg-slate-50'} ${disabled ? 'opacity-40 cursor-not-allowed' : ''}">${label}</button>`;
  let html = btn('<i class="fa-solid fa-chevron-left"></i>', page - 1, page <= 1, false);
  for (let p = start; p <= end; p++) html += btn(p, p, false, p === page);
  html += btn('<i class="fa-solid fa-chevron-right"></i>', page + 1, page >= totalPage, false);
  box.innerHTML = html;
}

/* ── 신청 → 발급 ── */
function issueCertificate() {
  const certTyCd = document.getElementById('ci-type').value;
  if (!certTyCd) { showHermesToast('증명서 종류를 선택하세요.', 'error'); return; }
  const params = new URLSearchParams({
    certTyCd,
    issueRsn: document.getElementById('ci-reason').value.trim(),
    issuePurps: document.getElementById('ci-purpose').value.trim()
  });
  fetch(`${CERT_BASE}/issue`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: params
  }).then(r => r.json().then(d => ({ ok: r.ok, d }))).then(({ ok, d }) => {
    if (!ok) { showHermesToast(d.error || '발급에 실패했습니다.', 'error'); return; }
    document.getElementById('ci-type').value = '';
    document.getElementById('ci-reason').value = '';
    document.getElementById('ci-purpose').value = '';
    showHermesToast('증명서가 발급되었습니다. 발급 내역에서 출력하세요.', 'success');
    loadMyCerts(1);
  }).catch(() => showHermesToast('서버 오류가 발생했습니다.', 'error'));
}

/* ── 내 발급 내역 ── */
function loadMyCerts(page) {
  ciPage = page;
  const params = new URLSearchParams({ page, screenSize: CERT_SIZE });
  const ty = document.getElementById('ci-filter-type').value;
  const prn = document.getElementById('ci-filter-prn').value;
  if (ty) params.set('certTyCd', ty);
  if (prn) params.set('prnYn', prn);

  fetch(`${CERT_BASE}/list?${params}`).then(r => r.json()).then(d => {
    document.getElementById('ci-total').textContent = d.totalCount;
    const tbody = document.getElementById('ci-tbody');
    if (!d.items.length) {
      tbody.innerHTML = '<tr><td colspan="5" class="text-center py-10 text-slate-400 text-sm">발급 내역이 없습니다.</td></tr>';
      document.getElementById('ci-paging').innerHTML = '';
      return;
    }
    tbody.innerHTML = d.items.map(c => {
      const printed = c.prnYn === 'Y';
      const badge = printed
        ? '<span class="text-xs font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">출력완료</span>'
        : '<span class="text-xs font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700">미출력</span>';
      const btn = printed
        ? `<button disabled class="text-xs font-semibold text-slate-300 border border-slate-100 px-3 py-1.5 rounded-lg cursor-not-allowed"><i class="fa-solid fa-print mr-1"></i>출력완료</button>`
        : `<button onclick="printCertificate(${c.certSn})" class="text-xs font-semibold text-violet-600 border border-violet-300 px-3 py-1.5 rounded-lg hover:bg-violet-50"><i class="fa-solid fa-print mr-1"></i>출력</button>`;
      return `
        <tr class="hover:bg-slate-50 transition-colors">
          <td class="py-3 px-4 text-xs text-slate-500">${fmtDateTime(c.issueDt)}</td>
          <td class="py-3 px-4"><span class="text-xs font-bold text-slate-800">${c.certTyNm || '-'}</span></td>
          <td class="py-3 px-4 text-xs text-slate-600">${c.issuePurps || '-'}</td>
          <td class="py-3 px-4">${badge}</td>
          <td class="py-3 px-4 text-right">${btn}</td>
        </tr>`;
    }).join('');
    renderPaging('ci-paging', d.totalCount, page, CERT_SIZE, 'loadMyCerts');
  });
}

/* ── 출력 (1회 제한) ── */
function printCertificate(certSn) {
  if (!confirm('증명서는 1회만 출력할 수 있습니다. 지금 출력하시겠습니까?')) return;
  fetch(`${CERT_BASE}/${certSn}/print`, { method: 'POST' })
    .then(r => r.json().then(d => ({ ok: r.ok, d })))
    .then(({ ok, d }) => {
      if (!ok) { showHermesToast(d.error || '출력에 실패했습니다.', 'error'); loadMyCerts(ciPage); return; }
      openPrintWindow(d.cert);
      loadMyCerts(ciPage);
    }).catch(() => showHermesToast('서버 오류가 발생했습니다.', 'error'));
}

/* ── 증명서 양식 보조 ── */
function yearOf(v) {
  if (!v) return null;
  return Array.isArray(v) ? v[0] : parseInt(String(v).substring(0, 4), 10);
}
function maskRrn(brdt, gndr) {
  const y = yearOf(brdt);
  if (!y) return '-';
  const yy = String(y % 100).padStart(2, '0');
  let g = '*';
  if (gndr === 'M') g = y < 2000 ? '1' : '3';
  else if (gndr === 'F') g = y < 2000 ? '2' : '4';
  return `${yy}****-${g}******`;
}
function fmtKDate(v) {
  if (!v) return '-';
  let y, mo, d;
  if (Array.isArray(v)) { [y, mo, d] = v; }
  else { const s = String(v); y = +s.substring(0, 4); mo = +s.substring(5, 7); d = +s.substring(8, 10); }
  return `${y}. ${p2(mo)}. ${p2(d)}`;
}
function fmtKLong(date) {
  return `${date.getFullYear()}년 ${p2(date.getMonth() + 1)}월 ${p2(date.getDate())}일`;
}
function fullAddr(c) {
  const a = [c.userAddr, c.userDaddr].filter(Boolean).join(' ').trim();
  return a || '-';
}
/* 입사일~현재 근속기간 → 'X년 Y개월' */
function diffYM(startVal) {
  let sy, sm, sd;
  if (Array.isArray(startVal)) { [sy, sm, sd] = startVal; }
  else if (startVal) { const s = String(startVal); sy = +s.substring(0, 4); sm = +s.substring(5, 7); sd = +s.substring(8, 10); }
  if (!sy) return '';
  const now = new Date();
  let months = (now.getFullYear() - sy) * 12 + (now.getMonth() + 1 - sm);
  if (now.getDate() < sd) months -= 1;
  if (months < 0) months = 0;
  return `${Math.floor(months / 12)}년 ${months % 12}개월`;
}

function openPrintWindow(c) {
  const title = (c.certTyNm || '증명서').split('').join(' ');
  const period = (from, to) => `${fmtKDate(from)} ~ ${to}`;

  // 종류별 분기 (01:재직 02:경력)
  let empLabel, empRows, proveText;
  if (c.certTyCd === '02') {
    empLabel = '재직사항';
    const dur = diffYM(c.joinYmd);
    empRows = [
      ['소 속', c.deptNm || '-'],
      ['직 위', c.jbgrNm || '-'],
      ['기 간', `${period(c.joinYmd, '현재')}${dur ? '  (' + dur + ')' : ''}`],
    ];
    proveText = '위와 같이 경력을 증명합니다.';
  } else {
    empLabel = '재직사항';
    empRows = [
      ['소 속', c.deptNm || '-'],
      ['직 위', c.jbgrNm || '-'],
      ['기 간', period(c.joinYmd, '현재')],
    ];
    proveText = '위와 같이 재직을 증명합니다.';
  }

  const empBody = empRows.map(([k, v], i) =>
    i === 0
      ? `<tr><td class="lbl-group" rowspan="${empRows.length}">${empLabel}</td><td class="lbl">${k}</td><td colspan="3">${v}</td></tr>`
      : `<tr><td class="lbl">${k}</td><td colspan="3">${v}</td></tr>`
  ).join('');

  const today = fmtKLong(new Date());

  const html = `
    <html><head><title>증명서 출력</title>
    <link href="https://fonts.googleapis.com/css2?family=Noto+Sans+KR:wght@400;700;900&display=swap" rel="stylesheet" />
    <link rel="stylesheet" href="/css/staff/certificate_issue.css" />
    </head>
    <body>
      <div class="cert-title">${title}</div>
      <table class="cert">
        <tr>
          <td class="lbl-group" rowspan="3">인적사항</td>
          <td class="lbl">성 명</td><td>${c.userName || '-'}</td>
          <td class="lbl">주민등록번호</td><td>${maskRrn(c.userBrdt, c.userGndrCd)}</td>
        </tr>
        <tr><td class="lbl">연 락 처</td><td colspan="3">${c.userTelno || '-'}</td></tr>
        <tr><td class="lbl">주 소</td><td colspan="3">${fullAddr(c)}</td></tr>
        ${empBody}
        <tr><td class="lbl-group">용 도</td><td colspan="4">${c.issueRsn || '-'}</td></tr>
      </table>

      <p class="prove">${proveText}</p>
      <p class="issue-date">${today}</p>
      <div class="org">
        <span class="org-name">HERMES 아카데미</span>
        <span class="cert-seal">원<br>장<br>인</span>
      </div>
    </body></html>`;

  const w = window.open('', '_blank', 'width=700,height=920');
  w.document.write(html);
  w.document.close();
  w.focus();

  // 외부 CSS·폰트 로드 완료 후 출력 (중복 호출 방지)
  let printed = false;
  const doPrint = () => { if (printed) return; printed = true; try { w.print(); } catch (e) {} };
  w.onload = doPrint;
  setTimeout(doPrint, 800);
}

/* ── 초기 로딩 ── */
document.addEventListener('DOMContentLoaded', () => loadMyCerts(1));
