/* 증명서 관리 (행정직원 모니터링) — 전 직원 발급 현황 조회 */
const CM_BASE = '/admin/certificates';
const CM_SIZE = 10;
const CM_BLOCK = 5;
let cmPage = 1;

function p2(n) { return String(n).padStart(2, '0'); }
function fmtDateTime(v) {
  if (!v) return '-';
  if (Array.isArray(v)) { const [y, mo, d, h = 0, mi = 0] = v; return `${y}-${p2(mo)}-${p2(d)} ${p2(h)}:${p2(mi)}`; }
  return String(v).replace('T', ' ').substring(0, 16);
}

/* ─── 페이징 (employees.html 동일 스타일) ─── */
function renderCmPagination(totalCount) {
  const container = document.getElementById('cm-pagination');
  const info = document.getElementById('cm-pagination-info');
  if (!container) return;

  const totalPage = Math.ceil(totalCount / CM_SIZE);
  const start = totalCount === 0 ? 0 : (cmPage - 1) * CM_SIZE + 1;
  const end = Math.min(cmPage * CM_SIZE, totalCount);
  if (info) {
    info.textContent = totalCount === 0
      ? '데이터가 없습니다.'
      : `전체 ${totalCount}건 중 ${start} – ${end} 표시`;
  }

  if (totalPage <= 1) { container.innerHTML = ''; return; }

  const endPage = Math.min(Math.ceil(cmPage / CM_BLOCK) * CM_BLOCK, totalPage);
  const startPage = Math.max(endPage - CM_BLOCK + 1, 1);

  const BASE = 'w-8 h-8 rounded-lg text-xs flex items-center justify-center';
  let html = '';
  if (startPage > 1) {
    html += `<button onclick="loadCerts(${startPage - 1})" class="${BASE} text-slate-400 hover:bg-slate-100">‹</button>`;
  }
  for (let p = startPage; p <= endPage; p++) {
    const cls = p === cmPage ? 'bg-blue-500 text-white font-bold' : 'text-slate-400 hover:bg-slate-100';
    html += `<button onclick="loadCerts(${p})" class="${BASE} ${cls}">${p}</button>`;
  }
  if (endPage < totalPage) {
    html += `<button onclick="loadCerts(${endPage + 1})" class="${BASE} text-slate-400 hover:bg-slate-100">›</button>`;
  }
  container.innerHTML = html;
}

function loadCerts(page) {
  cmPage = page;
  const params = new URLSearchParams({ page, screenSize: CM_SIZE });
  const kw = document.getElementById('cm-keyword').value.trim();
  const ty = document.getElementById('cm-type').value;
  const prn = document.getElementById('cm-prn').value;
  const from = document.getElementById('cm-from').value;
  const to = document.getElementById('cm-to').value;
  if (kw) params.set('keyword', kw);
  if (ty) params.set('certTyCd', ty);
  if (prn) params.set('prnYn', prn);
  if (from) params.set('fromDt', from);
  if (to) params.set('toDt', to);

  fetch(`${CM_BASE}/list?${params}`).then(r => r.json()).then(d => {
    document.getElementById('cm-total').textContent = d.totalCount;
    const tbody = document.getElementById('cm-tbody');
    if (!d.items.length) {
      tbody.innerHTML = '<tr><td colspan="7" class="text-center py-10 text-slate-400 text-sm">발급 이력이 없습니다.</td></tr>';
      renderCmPagination(0);
      return;
    }
    tbody.innerHTML = d.items.map(c => {
      const printed = c.prnYn === 'Y';
      const badge = printed
        ? '<span class="text-xs font-bold px-2 py-0.5 rounded-full bg-slate-100 text-slate-500">출력완료</span>'
        : '<span class="text-xs font-bold px-2 py-0.5 rounded-full bg-emerald-100 text-emerald-700">미출력</span>';
      return `
        <tr class="hover:bg-slate-50 transition-colors">
          <td class="py-3 px-4 text-xs text-slate-500">${fmtDateTime(c.issueDt)}</td>
          <td class="py-3 px-4">
            <div class="flex items-center gap-2">
              <div class="w-6 h-6 rounded-md bg-violet-100 flex items-center justify-center text-xs font-bold text-violet-700">${(c.userName || '?')[0]}</div>
              <span class="text-xs font-semibold text-slate-800">${c.userName || '-'}</span>
            </div>
          </td>
          <td class="py-3 px-4 text-xs text-slate-600">${c.deptNm || '-'} / ${c.jbgrNm || '-'}</td>
          <td class="py-3 px-4"><span class="text-xs font-bold text-slate-800">${c.certTyNm || '-'}</span></td>
          <td class="py-3 px-4 text-xs text-slate-600">${c.issuePurps || '-'}</td>
          <td class="py-3 px-4 text-xs text-slate-500 max-w-xs truncate" title="${c.issueRsn || ''}">${c.issueRsn || '-'}</td>
          <td class="py-3 px-4">${badge}</td>
        </tr>`;
    }).join('');
    renderCmPagination(d.totalCount);
  });
}

document.addEventListener('DOMContentLoaded', () => loadCerts(1));
