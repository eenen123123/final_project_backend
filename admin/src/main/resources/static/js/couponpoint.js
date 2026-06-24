// ────────────────────────────────────────────────────────
// 쿠폰 테이블 비동기 로드 + 페이지네이션
// ────────────────────────────────────────────────────────

const COUPON_PAGE_SIZE = 10;
let allCoupons        = [];
let couponPage        = 1;
let activeUseYnFilter = 'Y';
let activeDiscFilter  = 'all';
let editCouponSn      = null;

function escAttr(v) {
  return String(v == null ? '' : v)
    .replace(/&/g, '&amp;').replace(/"/g, '&quot;');
}

async function loadCoupons() {
  const res  = await fetch('/admin/coupon/list');
  allCoupons = res.ok ? await res.json() : [];
  couponPage = 1;
  renderCouponTable();
}

function getFilteredCoupons() {
  const isArchive = activeUseYnFilter === 'archive';
  return allCoupons.filter(function(c) {
    var passUseYn = isArchive ? c.useYn !== 'Y' : c.useYn === 'Y';
    var passDisc  = activeDiscFilter === 'all' || c.discType === activeDiscFilter;
    return passUseYn && passDisc;
  });
}

function renderCouponTable() {
  var filtered = getFilteredCoupons();
  var total    = filtered.length;
  var start    = (couponPage - 1) * COUPON_PAGE_SIZE;
  var pageData = filtered.slice(start, start + COUPON_PAGE_SIZE);
  var isArchive = activeUseYnFilter === 'archive';

  document.getElementById('visibleCount').textContent = total;

  var filterResetBtn = document.getElementById('filterResetBtn');
  if (filterResetBtn) filterResetBtn.classList.toggle('hidden', !isArchive);

  var tbody = document.getElementById('coupon-table-body');
  if (!tbody) return;

  if (total === 0) {
    tbody.innerHTML = '<tr><td colspan="8" class="py-20 text-center text-slate-400 text-sm">'
      + '<i class="fa-solid fa-ticket text-3xl text-slate-200 mb-3 block"></i>'
      + (isArchive ? '비활성 쿠폰이 없습니다.' : '등록된 쿠폰이 없습니다.')
      + '</td></tr>';
    renderCouponPagination(0);
    return;
  }

  tbody.innerHTML = pageData.map(renderCouponRow).join('');
  renderCouponPagination(total);
}

function renderCouponRow(c) {
  var discBadge = c.discType === 'FIXED'
    ? '<span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold bg-indigo-50 text-indigo-600">정액</span>'
    : '<span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold bg-amber-50 text-amber-600">정률</span>';

  var discVal = c.discType === 'FIXED'
    ? Number(c.discAmt || 0).toLocaleString() + '원'
    : (c.discRate || 0) + '%';

  var scopeBadge = c.useLimitCd === 'COURSE'
    ? '<span class="text-xs text-indigo-500 font-medium">강좌</span>'
    : c.useLimitCd === 'TEXTBOOK'
    ? '<span class="text-xs text-emerald-500 font-medium">교재</span>'
    : '<span class="text-xs text-slate-500 font-medium">전체</span>';

  var stateBadge = c.useYn === 'Y'
    ? '<span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold bg-emerald-50 text-emerald-600">활성</span>'
    : '<span class="inline-flex items-center px-2.5 py-1 rounded-lg text-xs font-bold bg-rose-50 text-rose-500">비활성</span>';

  var regDt = c.regDt ? c.regDt.substring(0, 10).replace(/-/g, '.') : '-';

  var code = c.couponCode
    ? c.couponCode.substring(0,5) + '-' + c.couponCode.substring(5,10) + '-' + c.couponCode.substring(10,15)
    : '';

  var nm = escAttr(c.couponNm);
  var isArchive = activeUseYnFilter === 'archive';

  var actionBtns = c.useYn === 'Y'
    ? '<button type="button" onclick="openIssuePopup(this)" data-sn="' + c.couponSn + '" data-nm="' + nm + '" class="text-xs font-bold text-blue-600 hover:text-blue-800 transition-colors">발급</button>'
      + '<span class="text-slate-200">|</span>'
    : '<button type="button" onclick="deleteArchivedCoupon(this)" data-sn="' + c.couponSn + '" data-nm="' + nm + '" class="archive-delete-btn ' + (isArchive ? '' : 'hidden') + ' text-xs font-bold text-rose-500 hover:text-rose-700 transition-colors">삭제</button>';

  actionBtns += '<button type="button" onclick="openRecipientsPopup(this)" data-sn="' + c.couponSn + '" data-nm="' + nm + '" class="text-xs font-bold text-blue-600 hover:text-blue-800 transition-colors">내역</button>';

  return '<tr class="coupon-row hover:bg-slate-50/50 transition-colors">'
    + '<td class="py-4 px-6">'
    +   '<button type="button" onclick="openEditModal(this)"'
    +   ' data-sn="' + c.couponSn + '"'
    +   ' data-nm="' + nm + '"'
    +   ' data-disc-type="' + escAttr(c.discType) + '"'
    +   ' data-disc-amt="' + escAttr(c.discAmt) + '"'
    +   ' data-disc-rate="' + escAttr(c.discRate) + '"'
    +   ' data-use-limit="' + escAttr(c.useLimitCd) + '"'
    +   ' data-valid-days="' + escAttr(c.validDays) + '"'
    +   ' data-use-yn="' + escAttr(c.useYn) + '"'
    +   ' data-coupon-code="' + escAttr(c.couponCode) + '"'
    +   ' class="text-left group">'
    +     '<p class="font-semibold text-slate-800 truncate group-hover:text-blue-600 transition-colors">' + (c.couponNm || '-') + '</p>'
    +     (code ? '<p class="text-xs font-mono text-slate-400 mt-0.5">' + code + '</p>' : '')
    +   '</button>'
    + '</td>'
    + '<td class="py-4 px-6 text-center">' + discBadge + '</td>'
    + '<td class="py-4 px-6 text-center font-mono text-sm">' + discVal + '</td>'
    + '<td class="py-4 px-6 text-center">' + scopeBadge + '</td>'
    + '<td class="py-4 px-6 text-center font-mono text-sm">' + (c.validDays || '-') + '일</td>'
    + '<td class="py-4 px-6 text-center">' + stateBadge + '</td>'
    + '<td class="py-4 px-6 text-center"><span class="text-xs text-slate-500">' + regDt + '</span></td>'
    + '<td class="py-4 px-6 text-center"><div class="flex items-center justify-center gap-3">' + actionBtns + '</div></td>'
    + '</tr>';
}

function renderCouponPagination(total) {
  var totalPages = Math.ceil(total / COUPON_PAGE_SIZE);
  var info       = document.getElementById('coupon-paging-info');
  var paging     = document.getElementById('coupon-pagination');
  var start      = total === 0 ? 0 : (couponPage - 1) * COUPON_PAGE_SIZE + 1;
  var end        = Math.min(couponPage * COUPON_PAGE_SIZE, total);

  if (info) {
    info.textContent = total === 0
      ? '등록된 쿠폰이 없습니다.'
      : '전체 ' + total + '개 중 ' + start + '–' + end + ' 표시';
  }
  if (!paging) return;
  if (totalPages <= 1) { paging.innerHTML = ''; return; }

  var BLOCK     = 5;
  var endPage   = Math.min(Math.ceil(couponPage / BLOCK) * BLOCK, totalPages);
  var startPage = Math.max(endPage - BLOCK + 1, 1);
  var BASE      = 'w-8 h-8 rounded-lg text-xs flex items-center justify-center cursor-pointer transition-colors';
  var html      = '';

  if (startPage > 1)
    html += '<button onclick="goToCouponPage(' + (startPage - 1) + ')" class="' + BASE + ' text-slate-400 hover:bg-slate-100">‹</button>';
  for (var p = startPage; p <= endPage; p++) {
    var cls = p === couponPage ? 'bg-blue-600 text-white font-bold' : 'text-slate-400 hover:bg-slate-100';
    html += '<button onclick="goToCouponPage(' + p + ')" class="' + BASE + ' ' + cls + '">' + p + '</button>';
  }
  if (endPage < totalPages)
    html += '<button onclick="goToCouponPage(' + (endPage + 1) + ')" class="' + BASE + ' text-slate-400 hover:bg-slate-100">›</button>';

  paging.innerHTML = html;
}

function goToCouponPage(p) {
  couponPage = p;
  renderCouponTable();
  document.getElementById('couponListSection').scrollIntoView({ behavior: 'smooth', block: 'nearest' });
}


// ────────────────────────────────────────────────────────
// 탭 전환
// ────────────────────────────────────────────────────────

function switchTab(tab) {
  document.getElementById('panelCoupon').classList.toggle('hidden', tab !== 'coupon');
  document.getElementById('panelPoint').classList.toggle('hidden', tab !== 'point');
  document.getElementById('tabCoupon').className = 'px-6 py-3 text-sm font-semibold border-b-2 transition-colors '
    + (tab === 'coupon' ? 'border-blue-600 text-blue-600' : 'border-transparent text-slate-400 hover:text-slate-600');
  document.getElementById('tabPoint').className = 'px-6 py-3 text-sm font-semibold border-b-2 transition-colors '
    + (tab === 'point' ? 'border-blue-600 text-blue-600' : 'border-transparent text-slate-400 hover:text-slate-600');
}


// ────────────────────────────────────────────────────────
// 쿠폰 필터
// ────────────────────────────────────────────────────────

function showCouponList() {
  var sec = document.getElementById('couponListSection');
  if (sec) sec.classList.remove('hidden');
}

function filterTable(mode) {
  activeUseYnFilter = mode;
  couponPage = 1;
  renderCouponTable();
}

async function deleteArchivedCoupon(btn) {
  var couponSn = btn.dataset.sn;
  var couponNm = btn.dataset.nm;
  if (!confirm('"' + couponNm + '" 쿠폰을 삭제하시겠습니까?')) return;
  var res = await fetch('/admin/coupon/' + couponSn, { method: 'DELETE' });
  if (res.ok || res.status === 204) {
    await loadCoupons();
  } else {
    var err = await res.json().catch(function() { return null; });
    alert((err && err.message) || '삭제 중 오류가 발생했습니다.');
  }
}

function filterByDiscType(type) {
  activeDiscFilter = type;
  ['all', 'Rate', 'Fixed'].forEach(function(k) {
    var btn    = document.getElementById('discFilter' + (k === 'all' ? 'All' : k));
    var active = (k === 'all' && type === 'all') || ('RATE' === type && k === 'Rate') || ('FIXED' === type && k === 'Fixed');
    btn.className = 'px-3 py-1.5 transition-colors ' + (active ? 'bg-blue-600 text-white' : 'text-slate-500 hover:bg-slate-50');
  });
  couponPage = 1;
  renderCouponTable();
}


// ────────────────────────────────────────────────────────
// 쿠폰 수정 모달
// ────────────────────────────────────────────────────────

function openEditModal(btn) {
  editCouponSn = parseInt(btn.dataset.sn);
  document.getElementById('editCouponNm').value     = btn.dataset.nm;
  document.getElementById('editDiscAmt').value      = btn.dataset.discAmt || '';
  document.getElementById('editDiscRate').value     = btn.dataset.discRate || '';
  document.getElementById('editUseLimitCd').value   = btn.dataset.useLimit || 'ALL';
  document.getElementById('editValidDays').value    = btn.dataset.validDays || '';

  var code = btn.dataset.couponCode;
  document.getElementById('editCouponCode').value = code
    ? code.substring(0,5) + '-' + code.substring(5,10) + '-' + code.substring(10,15)
    : '';

  var discType = btn.dataset.discType;
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
  var isFixed = document.getElementById('editDiscTypeFixed').checked;
  document.getElementById('editFieldFixed').classList.toggle('hidden', !isFixed);
  document.getElementById('editFieldRate').classList.toggle('hidden', isFixed);
}

async function submitEdit() {
  var couponNm   = document.getElementById('editCouponNm').value.trim();
  var discType   = document.querySelector('input[name="editDiscType"]:checked').value;
  var useLimitCd = document.getElementById('editUseLimitCd').value;
  var validDays  = parseInt(document.getElementById('editValidDays').value);
  var useYn      = document.querySelector('input[name="editUseYn"]:checked').value;

  if (!couponNm) { alert('쿠폰명을 입력하세요.'); return; }
  if (!validDays || validDays < 1) { alert('유효일수를 1일 이상 입력하세요.'); return; }

  var body = { couponNm: couponNm, discType: discType, useLimitCd: useLimitCd, validDays: validDays, useYn: useYn };
  if (discType === 'FIXED') {
    var discAmt = parseInt(document.getElementById('editDiscAmt').value);
    if (!discAmt || discAmt < 1) { alert('할인 금액을 입력하세요.'); return; }
    body.discAmt = discAmt;
  } else {
    var discRate = parseInt(document.getElementById('editDiscRate').value);
    if (!discRate || discRate < 1 || discRate > 100) { alert('할인율을 1~100 사이로 입력하세요.'); return; }
    body.discRate = discRate;
  }

  var res = await fetch('/admin/coupon/' + editCouponSn, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(body)
  });

  if (res.ok) {
    closeEditModal();
    await loadCoupons();
  } else {
    var err = await res.json().catch(function() { return null; });
    alert((err && err.message) || '수정 중 오류가 발생했습니다.');
  }
}

async function submitDelete() {
  if (!confirm('이 쿠폰을 삭제하시겠습니까?\n발급 이력이 있는 경우 삭제되지 않습니다.')) return;

  var res = await fetch('/admin/coupon/' + editCouponSn, { method: 'DELETE' });

  if (res.ok || res.status === 204) {
    closeEditModal();
    await loadCoupons();
  } else {
    var err = await res.json().catch(function() { return null; });
    alert((err && err.message) || '삭제 중 오류가 발생했습니다.');
  }
}

function openIssuePopup(btn) {
  var couponSn = btn.dataset.sn;
  var couponNm = encodeURIComponent(btn.dataset.nm);
  popupCenter('/admin/coupon/popup/users?couponSn=' + couponSn + '&couponNm=' + couponNm, 'couponIssue', 600, 640);
}

function openRecipientsPopup(btn) {
  var couponSn = btn.dataset.sn;
  var couponNm = encodeURIComponent(btn.dataset.nm);
  popupCenter('/admin/coupon/recipients-popup?couponSn=' + couponSn + '&couponNm=' + couponNm, 'couponRecipients', 780, 540);
}


// ────────────────────────────────────────────────────────
// 스터디포인트 정의 관리
// ────────────────────────────────────────────────────────

var editStudyDefSn = null;

function showStudyDefList() {
  var sec = document.getElementById('studyDefSection');
  if (sec) sec.classList.remove('hidden');
}


// 쿠폰/포인트 팝업 공통
function popupCenter(url, name, w, h) {
  var left = Math.round((screen.width - w) / 2);
  var top  = Math.round((screen.height - h) / 2);
  window.open(url, name, 'width=' + w + ',height=' + h + ',left=' + left + ',top=' + top + ',resizable=yes,scrollbars=no');
}

function openCouponInsertPopup() {
  popupCenter('/admin/coupon/insert-popup', 'couponInsert', 580, 640);
}

function openStudyDefModal() {
  popupCenter('/admin/coupon/study-point/insert-popup', 'studyPointInsert', 520, 400);
}

async function submitStudyDef() {
  var nm  = document.getElementById('studyDefNm').value.trim();
  var amt = parseInt(document.getElementById('studyDefAmt').value);
  if (!nm) { alert('포인트 항목명을 입력하세요.'); return; }
  if (!amt || amt < 1) { alert('지급량을 1 이상 입력하세요.'); return; }

  var res = await fetch('/admin/coupon/study-point', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ couponNm: nm, discAmt: amt })
  });
  if (res.ok) { location.href = location.pathname + '#point'; location.reload(); }
  else { var e = await res.json().catch(function(){return null;}); alert((e && e.message) || '등록 중 오류가 발생했습니다.'); }
}

function openStudyDefEditModal(btn) {
  editStudyDefSn = parseInt(btn.dataset.sn);
  document.getElementById('editStudyDefNm').value  = btn.dataset.nm;
  document.getElementById('editStudyDefAmt').value = btn.dataset.amt;
  document.querySelectorAll('input[name="editStudyDefUseYn"]').forEach(function(r) {
    r.checked = r.value === btn.dataset.useYn;
  });
  document.getElementById('studyDefEditModal').classList.remove('hidden');
}

function closeStudyDefEditModal() {
  document.getElementById('studyDefEditModal').classList.add('hidden');
  editStudyDefSn = null;
}

async function submitStudyDefEdit() {
  var nm    = document.getElementById('editStudyDefNm').value.trim();
  var amt   = parseInt(document.getElementById('editStudyDefAmt').value);
  var useYn = document.querySelector('input[name="editStudyDefUseYn"]:checked').value;
  if (!nm) { alert('포인트 항목명을 입력하세요.'); return; }
  if (!amt || amt < 1) { alert('지급량을 1 이상 입력하세요.'); return; }

  var res = await fetch('/admin/coupon/study-point/' + editStudyDefSn, {
    method: 'PUT',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ couponNm: nm, discAmt: amt, useYn: useYn })
  });
  if (res.ok) { location.href = location.pathname + '#point'; location.reload(); }
  else { var e = await res.json().catch(function(){return null;}); alert((e && e.message) || '수정 중 오류가 발생했습니다.'); }
}

async function submitStudyDefDelete() {
  if (!confirm('이 포인트 항목을 삭제하시겠습니까?\n발급 이력이 있으면 삭제되지 않습니다.')) return;
  var res = await fetch('/admin/coupon/study-point/' + editStudyDefSn, { method: 'DELETE' });
  if (res.ok || res.status === 204) { location.href = location.pathname + '#point'; location.reload(); }
  else { var e = await res.json().catch(function(){return null;}); alert((e && e.message) || '삭제 중 오류가 발생했습니다.'); }
}

function openStudyIssuePopup(btn) {
  var couponSn = btn.dataset.sn;
  var couponNm = encodeURIComponent(btn.dataset.nm);
  popupCenter('/admin/coupon/popup/users?couponSn=' + couponSn + '&couponNm=' + couponNm + '&type=study', 'studyPointIssue', 600, 640);
}

function openStudyRecipientsPopup(btn) {
  var couponSn = btn.dataset.sn;
  var couponNm = encodeURIComponent(btn.dataset.nm);
  popupCenter('/admin/coupon/study-point/recipients-popup?couponSn=' + couponSn + '&couponNm=' + couponNm, 'studyRecipients', 820, 560);
}


// ────────────────────────────────────────────────────────
// HM포인트 / HM머니 유저 목록
// ────────────────────────────────────────────────────────

var hmRole     = 'all';
var moneyRole  = 'all';
var hmUsers    = [];
var moneyUsers = [];
var hmPage     = 1;
var moneyPage  = 1;
var POINT_USER_PAGE = 50;

function setPointRoleFilter(prefix, role) {
  if (prefix === 'hm') hmRole = role;
  else moneyRole = role;
  ['All','Student','User'].forEach(function(k) {
    var id  = prefix + 'F' + k;
    var val = k === 'All' ? 'all' : k === 'Student' ? 'ROLE_STUDENT' : 'ROLE_USER';
    var el  = document.getElementById(id);
    if (!el) return;
    el.className = 'px-3 py-2 transition-colors cursor-pointer '
      + (val === role ? 'bg-slate-700 text-white' : 'text-slate-500 bg-white hover:bg-slate-50 border-l border-slate-200');
  });
  var assetType = prefix === 'hm' ? 'HM_POINT' : 'HM_MONEY';
  searchPointUsers(assetType);
}

async function searchPointUsers(assetType) {
  var prefix = assetType === 'HM_POINT' ? 'hm' : 'money';
  var role   = assetType === 'HM_POINT' ? hmRole : moneyRole;
  var q      = document.getElementById(prefix + 'SearchQ').value.trim();
  var tbody  = document.getElementById(prefix + 'UserTableBody');
  var unit   = assetType === 'HM_POINT' ? 'p' : '원';

  tbody.innerHTML = '<tr><td colspan="4" class="py-10 text-center text-slate-400 text-sm"><i class="fa-solid fa-spinner fa-spin mr-1"></i>검색 중...</td></tr>';

  var res  = await fetch('/admin/point/users?q=' + encodeURIComponent(q) + '&assetType=' + assetType + '&role=' + role);
  var data = res.ok ? await res.json() : [];

  if (!data || data.length === 0) {
    tbody.innerHTML = '<tr><td colspan="4" class="py-14 text-center text-slate-400 text-sm">검색 결과가 없습니다.</td></tr>';
    return;
  }

  if (assetType === 'HM_POINT') { hmUsers = data; hmPage = 1; }
  else { moneyUsers = data; moneyPage = 1; }
  renderPointUserPage(assetType);
}

function renderPointUserPage(assetType) {
  var prefix   = assetType === 'HM_POINT' ? 'hm' : 'money';
  var unit     = assetType === 'HM_POINT' ? 'p' : '원';
  var data     = assetType === 'HM_POINT' ? hmUsers : moneyUsers;
  var page     = assetType === 'HM_POINT' ? hmPage  : moneyPage;
  var tbody    = document.getElementById(prefix + 'UserTableBody');
  var paging   = document.getElementById(prefix + 'UserPaging');
  var start    = (page - 1) * POINT_USER_PAGE;
  var pageData = data.slice(start, start + POINT_USER_PAGE);
  var totalPages = Math.ceil(data.length / POINT_USER_PAGE) || 1;

  if (pageData.length === 0) {
    tbody.innerHTML = '<tr><td class="py-14 text-center text-slate-400 text-xs">검색 결과가 없습니다.</td></tr>';
    paging.innerHTML = '';
    return;
  }

  tbody.innerHTML = pageData.map(function(u) {
    var bal      = u.balance != null ? Number(u.balance) : 0;
    var balStr   = bal.toLocaleString() + unit;
    var balCls   = bal > 0 ? 'font-bold text-blue-600' : 'text-slate-300';
    var initial  = (u.userName || u.userId || '?').charAt(0).toUpperCase();
    var isStudent = u.userRole === 'ROLE_STUDENT';
    var avatarCls = isStudent ? 'bg-indigo-100 text-indigo-600' : 'bg-slate-100 text-slate-500';
    var safeId   = (u.userId || '').replace(/'/g, "\\'");
    var safeNm   = (u.userName || '').replace(/'/g, "\\'");
    return '<tr class="hover:bg-slate-50 transition-colors cursor-pointer border-b border-slate-50 last:border-0"'
      + ' onclick="selectPointUser(this,\'' + safeId + '\',\'' + safeNm + '\',\'' + assetType + '\',' + bal + ')">'
      + '<td class="py-3 px-4">'
      + '  <div class="flex items-center gap-2.5">'
      + '    <div class="w-7 h-7 rounded-full ' + avatarCls + ' flex items-center justify-center text-xs font-bold shrink-0">' + initial + '</div>'
      + '    <div class="min-w-0">'
      + '      <p class="text-sm font-semibold text-slate-800 truncate">' + (u.userName || '-') + '</p>'
      + '      <p class="text-[11px] font-mono text-slate-400 truncate">' + (u.userId || '-') + '</p>'
      + '    </div>'
      + '    <span class="ml-auto shrink-0 text-xs font-mono ' + balCls + '">' + balStr + '</span>'
      + '  </div>'
      + '</td>'
      + '</tr>';
  }).join('');

  if (totalPages <= 1) { paging.innerHTML = ''; return; }
  var btnCls = 'w-6 h-6 flex items-center justify-center rounded text-slate-500 hover:bg-slate-100 cursor-pointer transition-colors ';
  var html = '<button onclick="goPointUserPage(\'' + assetType + '\',' + (page - 1) + ')" '
    + (page <= 1 ? 'disabled class="' + btnCls + 'opacity-30 cursor-not-allowed"' : 'class="' + btnCls + '"') + '>&lt;</button>';
  html += '<span class="text-slate-500 font-medium">' + page + ' / ' + totalPages + '</span>';
  html += '<button onclick="goPointUserPage(\'' + assetType + '\',' + (page + 1) + ')" '
    + (page >= totalPages ? 'disabled class="' + btnCls + 'opacity-30 cursor-not-allowed"' : 'class="' + btnCls + '"') + '>&gt;</button>';
  paging.innerHTML = html;
}

function goPointUserPage(assetType, page) {
  var maxPage = Math.ceil((assetType === 'HM_POINT' ? hmUsers : moneyUsers).length / POINT_USER_PAGE);
  page = Math.max(1, Math.min(maxPage, page));
  if (assetType === 'HM_POINT') hmPage = page;
  else moneyPage = page;
  renderPointUserPage(assetType);
}

async function selectPointUser(row, userId, userName, assetType, balance) {
  var prefix  = assetType === 'HM_POINT' ? 'hm' : 'money';
  var unit    = assetType === 'HM_POINT' ? 'p' : '원';
  var balCls  = assetType === 'HM_POINT' ? 'text-blue-600' : 'text-emerald-600';

  var tbody = document.getElementById(prefix + 'UserTableBody');
  tbody.querySelectorAll('tr').forEach(function(r) { r.classList.remove('bg-blue-50/60'); });
  row.classList.add('bg-blue-50/60');

  document.getElementById(prefix + 'HistEmpty').classList.add('hidden');
  var content = document.getElementById(prefix + 'HistContent');
  content.classList.remove('hidden');
  content.classList.add('flex');

  document.getElementById(prefix + 'HistTitle').textContent = userName + ' (' + userId + ')';
  var balEl = document.getElementById(prefix + 'HistBal');
  balEl.textContent = balance.toLocaleString() + unit;
  balEl.className = 'font-bold ' + balCls;

  var histBody = document.getElementById(prefix + 'HistBody');
  histBody.innerHTML = '<tr><td colspan="4" class="py-8 text-center text-slate-300 text-xs"><i class="fa-solid fa-spinner fa-spin"></i></td></tr>';

  var res  = await fetch('/admin/point/user/history?userId=' + encodeURIComponent(userId) + '&assetType=' + assetType);
  var hist = res.ok ? await res.json() : [];

  if (!hist || hist.length === 0) {
    histBody.innerHTML = '<tr><td colspan="4" class="py-10 text-center text-slate-400 text-sm">이력이 없습니다.</td></tr>';
    return;
  }

  var typeLabel = { EARN: '적립', USE: '사용', EXPIRE: '소멸' };
  var typeCls   = { EARN: 'bg-emerald-50 text-emerald-600', USE: 'bg-rose-50 text-rose-500', EXPIRE: 'bg-slate-100 text-slate-400' };
  histBody.innerHTML = hist.map(function(h) {
    var badge  = '<span class="text-[10px] font-bold px-1.5 py-0.5 rounded ' + (typeCls[h.histType] || '') + '">' + (typeLabel[h.histType] || h.histType) + '</span>';
    var amt    = h.changeAmt > 0 ? '+' + Number(h.changeAmt).toLocaleString() : Number(h.changeAmt).toLocaleString();
    var amtCls = h.changeAmt > 0 ? 'text-emerald-600' : 'text-rose-500';
    var dt     = h.regDt ? h.regDt.substring(0,10).replace(/-/g,'.') : '-';
    return '<tr class="hover:bg-slate-50/50 transition-colors">'
      + '<td class="py-3 px-5 text-xs text-slate-500">'    + dt    + '</td>'
      + '<td class="py-3 px-3 text-center">'               + badge + '</td>'
      + '<td class="py-3 px-3 text-center font-mono font-bold text-sm ' + amtCls + '">' + amt + unit + '</td>'
      + '<td class="py-3 px-3 text-xs text-slate-500">'    + (h.memo || '-') + '</td>'
      + '</tr>';
  }).join('');
}


// ────────────────────────────────────────────────────────
// 포인트 서브탭
// ────────────────────────────────────────────────────────

function switchPointTab(tab) {
  ['study','hm','money'].forEach(function(t) {
    document.getElementById('ppanel' + t.charAt(0).toUpperCase() + t.slice(1)).classList.toggle('hidden', t !== tab);
    var btn = document.getElementById('ptab' + t.charAt(0).toUpperCase() + t.slice(1));
    btn.className = 'px-5 py-2.5 text-sm font-semibold border-b-2 transition-colors '
      + (t === tab ? 'border-blue-600 text-blue-600' : 'border-transparent text-slate-400 hover:text-slate-600');
  });
  if (tab === 'hm')    searchPointUsers('HM_POINT');
  if (tab === 'money') searchPointUsers('HM_MONEY');
}

async function searchStudyGrants() {
  var q = document.getElementById('studySearchQ').value.trim();
  var res = await fetch('/admin/point/study/search?q=' + encodeURIComponent(q));
  var data = await res.json();
  var tbody = document.getElementById('studyGrantsBody');
  if (!data || data.length === 0) {
    tbody.innerHTML = '<tr><td colspan="6" class="py-16 text-center text-slate-400 text-sm">내역이 없습니다.</td></tr>';
    return;
  }
  tbody.innerHTML = data.map(function(pg) {
    return '<tr class="hover:bg-slate-50/50 transition-colors">'
      + '<td class="py-4 px-6 font-mono text-xs text-slate-500">' + (pg.issueDt ? pg.issueDt.substring(0,10).replace(/-/g,'.') : '-') + '</td>'
      + '<td class="py-4 px-4 text-xs font-medium text-slate-700">' + (pg.userId || '-') + '</td>'
      + '<td class="py-4 px-4 text-xs text-slate-600">' + (pg.userName || '-') + '</td>'
      + '<td class="py-4 px-4 text-center font-mono text-xs text-slate-500">' + (pg.expiryDt ? pg.expiryDt.substring(0,10).replace(/-/g,'.') : '-') + '</td>'
      + '<td class="py-4 px-4 text-center font-mono text-sm font-bold text-slate-800">' + (pg.pointAmt ? pg.pointAmt.toLocaleString() + 'p' : '-') + '</td>'
      + '<td class="py-4 px-4 text-xs text-slate-500">' + (pg.issuedBy || '시스템') + '</td>'
      + '</tr>';
  }).join('');
}


// ────────────────────────────────────────────────────────
// 포인트 지급 모달
// ────────────────────────────────────────────────────────

function openGrantModal() {
  document.getElementById('grantUserId').value = '';
  document.getElementById('grantAmount').value = '';
  document.getElementById('grantMemo').value   = '';
  document.getElementById('grantModal').classList.remove('hidden');
}

function closeGrantModal() {
  document.getElementById('grantModal').classList.add('hidden');
}

async function submitGrant() {
  var userId = document.getElementById('grantUserId').value.trim();
  var amount = parseInt(document.getElementById('grantAmount').value);
  var memo   = document.getElementById('grantMemo').value.trim();

  if (!userId) { alert('회원 ID를 입력하세요.'); return; }
  if (!amount || amount < 1) { alert('지급량을 1 이상 입력하세요.'); return; }

  var res = await fetch('/admin/point/grant', {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify({ userId: userId, amount: amount, memo: memo })
  });

  if (res.ok) {
    location.reload();
  } else {
    var err = await res.json().catch(function() { return null; });
    alert((err && err.message) || '포인트 지급 중 오류가 발생했습니다.');
  }
}


// ────────────────────────────────────────────────────────
// 초기화
// ────────────────────────────────────────────────────────

document.addEventListener('DOMContentLoaded', function() {
  loadCoupons();

  // URL 해시로 탭 복원
  var hash = location.hash;
  if (hash === '#point') {
    switchTab('point');
  } else if (hash === '#point-hm') {
    switchTab('point'); switchPointTab('hm');
  } else if (hash === '#point-money') {
    switchTab('point'); switchPointTab('money');
  }
});
