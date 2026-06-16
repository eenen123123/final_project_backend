let activeUseYnFilter = 'Y';
let activeDiscFilter  = 'all';
let editCouponSn = null;

let receiverData = [];
let receiverPage = 1;
var RECEIVER_PAGE_SIZE = 10;

function openReceiverModal(btn) {
  var couponSn = parseInt(btn.dataset.couponSn);
  document.getElementById('receiverModalTitle').textContent = btn.dataset.couponNm;
  receiverData = (typeof issuedCouponsData !== 'undefined' ? issuedCouponsData : [])
    .filter(function(uc) { return uc.couponSn === couponSn; });
  receiverPage = 1;
  renderReceiverPage();
  document.getElementById('receiverModal').classList.remove('hidden');
}

function renderReceiverPage() {
  var total = receiverData.length;
  var totalPages = Math.ceil(total / RECEIVER_PAGE_SIZE) || 1;
  var start = (receiverPage - 1) * RECEIVER_PAGE_SIZE;
  var pageData = receiverData.slice(start, start + RECEIVER_PAGE_SIZE);
  var tbody = document.getElementById('receiverTableBody');

  if (total === 0) {
    tbody.innerHTML = '<tr><td colspan="3" class="py-10 text-center text-slate-400 text-sm">발급 내역이 없습니다.</td></tr>';
  } else {
    tbody.innerHTML = pageData.map(function(uc) {
      var badge = uc.useYn === 'Y'
        ? '<span class="inline-flex items-center px-2 py-0.5 rounded-lg text-xs font-bold bg-emerald-50 text-emerald-600">사용완료</span>'
        : uc.useYn === 'E'
        ? '<span class="inline-flex items-center px-2 py-0.5 rounded-lg text-xs font-bold bg-slate-100 text-slate-500">소멸</span>'
        : '<span class="inline-flex items-center px-2 py-0.5 rounded-lg text-xs font-bold bg-blue-50 text-blue-600">미사용</span>';
      var expiry = uc.expiryDt ? uc.expiryDt.replace(/-/g, '.') : '-';
      return '<tr class="hover:bg-slate-50/50 transition-colors">'
        + '<td class="py-3 px-4 text-xs font-mono text-slate-700">' + (uc.userId || '-') + '</td>'
        + '<td class="py-3 px-4 text-xs text-slate-700">' + (uc.userName || '-') + '</td>'
        + '<td class="py-3 px-4 text-xs text-slate-500">' + expiry + '</td>'
        + '<td class="py-3 px-4 text-center">' + badge + '</td>'
        + '</tr>';
    }).join('');
  }

  var arrowCls = 'w-8 h-8 flex items-center justify-center rounded-lg border border-slate-200 text-slate-400 hover:bg-slate-50 disabled:opacity-30 disabled:cursor-not-allowed transition-colors text-xs';
  var html = '<button onclick="receiverChangePage(-1)"' + (receiverPage <= 1 ? ' disabled' : '') + ' class="' + arrowCls + '">'
    + '<i class="fa-solid fa-chevron-left"></i></button>';

  var startP = Math.max(1, receiverPage - 2);
  var endP = Math.min(totalPages, startP + 4);
  startP = Math.max(1, endP - 4);
  for (var p = startP; p <= endP; p++) {
    html += '<button onclick="receiverGoToPage(' + p + ')" class="w-8 h-8 flex items-center justify-center rounded-lg text-xs font-medium transition-colors '
      + (p === receiverPage ? 'bg-slate-800 text-white' : 'border border-slate-200 text-slate-500 hover:bg-slate-50') + '">'
      + p + '</button>';
  }

  html += '<button onclick="receiverChangePage(1)"' + (receiverPage >= totalPages ? ' disabled' : '') + ' class="' + arrowCls + '">'
    + '<i class="fa-solid fa-chevron-right"></i></button>';

  document.getElementById('receiverPaging').innerHTML = html;
}

function receiverChangePage(delta) {
  var total = receiverData.length;
  var totalPages = Math.ceil(total / RECEIVER_PAGE_SIZE) || 1;
  receiverPage = Math.max(1, Math.min(totalPages, receiverPage + delta));
  renderReceiverPage();
}

function receiverGoToPage(page) {
  receiverPage = page;
  renderReceiverPage();
}


function closeReceiverModal() {
  document.getElementById('receiverModal').classList.add('hidden');
  receiverData = [];
  receiverPage = 1;
}

function showIssuedSection() {
  document.getElementById('couponListSection').classList.add('hidden');
  document.getElementById('issuedSection').classList.remove('hidden');
  document.getElementById('issuedCard').classList.add('ring-2', 'ring-violet-300');
}

function showCouponList() {
  document.getElementById('issuedSection').classList.add('hidden');
  document.getElementById('couponListSection').classList.remove('hidden');
  document.getElementById('issuedCard').classList.remove('ring-2', 'ring-violet-300');
}

function switchTab(tab) {
  document.getElementById('panelCoupon').classList.toggle('hidden', tab !== 'coupon');
  document.getElementById('panelPoint').classList.toggle('hidden', tab !== 'point');
  document.getElementById('tabCoupon').className = 'px-6 py-3 text-sm font-semibold border-b-2 transition-colors '
    + (tab === 'coupon' ? 'border-slate-900 text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600');
  document.getElementById('tabPoint').className = 'px-6 py-3 text-sm font-semibold border-b-2 transition-colors '
    + (tab === 'point' ? 'border-slate-900 text-slate-900' : 'border-transparent text-slate-400 hover:text-slate-600');
}

function applyFilters() {
  const rows = document.querySelectorAll('.coupon-row');
  const isArchive = activeUseYnFilter === 'archive';
  let visible = 0;
  rows.forEach(function(row) {
    const yn   = row.dataset.useYn;
    const disc = row.dataset.discType;
    const passUseYn = isArchive ? yn !== 'Y' : yn === 'Y';
    const passDisc  = activeDiscFilter === 'all' || disc === activeDiscFilter;
    const show = passUseYn && passDisc;
    row.style.display = show ? '' : 'none';
    if (show) visible++;
  });
  document.getElementById('visibleCount').textContent = visible;
  document.querySelectorAll('.archive-delete-btn').forEach(function(btn) {
    btn.classList.toggle('hidden', !isArchive);
  });
  document.getElementById('filterResetBtn').classList.toggle('hidden', !isArchive);
}

function filterTable(mode) {
  activeUseYnFilter = mode;
  applyFilters();
}

async function deleteArchivedCoupon(btn) {
  const couponSn = btn.dataset.sn;
  const couponNm = btn.dataset.nm;
  if (!confirm(`"${couponNm}" 쿠폰을 삭제하시겠습니까?`)) return;
  const res = await fetch(`/admin/coupon/${couponSn}`, { method: 'DELETE' });
  if (res.ok || res.status === 204) {
    location.reload();
  } else {
    const err = await res.json().catch(function() { return null; });
    alert(err?.message || '삭제 중 오류가 발생했습니다.');
  }
}

function filterByDiscType(type) {
  activeDiscFilter = type;
  ['all', 'Rate', 'Fixed'].forEach(function(k) {
    const btn = document.getElementById('discFilter' + (k === 'all' ? 'All' : k));
    const active = (k === 'all' && type === 'all') || ('RATE' === type && k === 'Rate') || ('FIXED' === type && k === 'Fixed');
    btn.className = 'px-3 py-1.5 transition-colors ' + (active ? 'bg-slate-800 text-white' : 'text-slate-500 hover:bg-slate-50');
  });
  applyFilters();
}

function openEditModal(btn) {
  editCouponSn = parseInt(btn.dataset.sn);
  document.getElementById('editCouponNm').value     = btn.dataset.nm;
  document.getElementById('editDiscAmt').value      = btn.dataset.discAmt || '';
  document.getElementById('editDiscRate').value     = btn.dataset.discRate || '';
  document.getElementById('editUseLimitCd').value   = btn.dataset.useLimit || 'ALL';
  document.getElementById('editValidDays').value    = btn.dataset.validDays || '';

  const code = btn.dataset.couponCode;
  document.getElementById('editCouponCode').value = code
    ? code.substring(0,5) + '-' + code.substring(5,10) + '-' + code.substring(10,15)
    : '';

  const discType = btn.dataset.discType;
  document.getElementById('editDiscTypeFixed').checked = discType === 'FIXED';
  document.getElementById('editDiscTypeRate').checked  = discType === 'RATE';
  onEditDiscTypeChange();

  document.querySelectorAll('input[name="editUseYn"]').forEach(function(r) {
    r.checked = r.value === btn.dataset.useYn;
  });

  document.getElementById('editModal').classList.remove('hidden');
}

function closeEditModal() {
  document.getElementById('editModal').classList.add('hidden');
  editCouponSn = null;
}

function onEditDiscTypeChange() {
  const isFixed = document.getElementById('editDiscTypeFixed').checked;
  document.getElementById('editFieldFixed').classList.toggle('hidden', !isFixed);
  document.getElementById('editFieldRate').classList.toggle('hidden', isFixed);
}

async function submitEdit() {
  const couponNm   = document.getElementById('editCouponNm').value.trim();
  const discType   = document.querySelector('input[name="editDiscType"]:checked').value;
  const useLimitCd = document.getElementById('editUseLimitCd').value;
  const validDays  = parseInt(document.getElementById('editValidDays').value);
  const useYn      = document.querySelector('input[name="editUseYn"]:checked').value;

  if (!couponNm) { alert('쿠폰명을 입력하세요.'); return; }
  if (!validDays || validDays < 1) { alert('유효일수를 1일 이상 입력하세요.'); return; }

  const body = { couponNm, discType, useLimitCd, validDays, useYn };
  if (discType === 'FIXED') {
    const discAmt = parseInt(document.getElementById('editDiscAmt').value);
    if (!discAmt || discAmt < 1) { alert('할인 금액을 입력하세요.'); return; }
    body.discAmt = discAmt;
  } else {
    const discRate = parseInt(document.getElementById('editDiscRate').value);
    if (!discRate || discRate < 1 || discRate > 100) { alert('할인율을 1~100 사이로 입력하세요.'); return; }
    body.discRate = discRate;
  }

  const res = await fetch(`/admin/coupon/${editCouponSn}`, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });

  if (res.ok) {
    location.reload();
  } else {
    const err = await res.json().catch(function() { return null; });
    alert(err?.message || '수정 중 오류가 발생했습니다.');
  }
}

async function submitDelete() {
  if (!confirm('이 쿠폰을 삭제하시겠습니까?\n발급 이력이 있는 경우 삭제되지 않습니다.')) return;

  const res = await fetch(`/admin/coupon/${editCouponSn}`, { method: 'DELETE' });

  if (res.ok || res.status === 204) {
    location.reload();
  } else {
    const err = await res.json().catch(function() { return null; });
    alert(err?.message || '삭제 중 오류가 발생했습니다.');
  }
}

function openIssuePopup(btn) {
  const couponSn = btn.dataset.sn;
  const couponNm = encodeURIComponent(btn.dataset.nm);
  window.open(
    `/admin/coupon/popup/users?couponSn=${couponSn}&couponNm=${couponNm}`,
    'couponIssue',
    'width=600,height=640,resizable=yes,scrollbars=yes'
  );
}

document.addEventListener('DOMContentLoaded', applyFilters);
