// ── 페이지 캐시 (한 번 불러온 페이지는 메모리에 저장 → 재클릭 시 즉시 렌더) ──
const _cache = { faq: {}, notice: {}, qna: {}, dataroom: {} };

function cacheGet(ns, key) { return _cache[ns][key] || null; }
function cacheSet(ns, key, data) { _cache[ns][key] = data; }
function cacheClear(ns) { _cache[ns] = {}; }

// ── 탭 전환 ──────────────────────────────────────────────
function switchTab(tabId, btn) {
  document.querySelectorAll('.cs-tab-btn').forEach(b => b.classList.remove('active'));
  document.querySelectorAll('.cs-tab-panel').forEach(p => p.classList.remove('active'));
  btn.classList.add('active');
  document.getElementById(tabId).classList.add('active');
  history.replaceState(null, '', '?tab=' + tabId);
  loadTabData(tabId, 1);
}

function loadTabData(tabId, page) {
  if (tabId === 'tab-faq') loadFaq(page);
  else if (tabId === 'tab-notice') loadNotice(page);
  else if (tabId === 'tab-qna') loadQna(page);
  else if (tabId === 'tab-dataroom') loadDataRoom(page);
}

// ── 페이지네이션 렌더링 ────────────────────────────────────
function renderPagination(containerId, totalCount, currentPage, size, loadFn) {
  const totalPages = Math.ceil(totalCount / size);
  const container = document.getElementById(containerId);
  if (!container || totalPages <= 1) { if (container) container.innerHTML = ''; return; }

  const blockSize = 5;
  const endPage = Math.ceil(currentPage / blockSize) * blockSize;
  const startPage = endPage - blockSize + 1;

  let html = '<div class="flex items-center justify-center gap-1 mt-4">';
  if (startPage > 1) {
    html += `<button onclick="${loadFn}(${startPage - 1})" class="px-2.5 py-1.5 text-xs text-slate-500 hover:text-violet-600 rounded-lg hover:bg-violet-50 transition-colors">&lt;</button>`;
  }
  for (let p = startPage; p <= Math.min(endPage, totalPages); p++) {
    if (p === currentPage) {
      html += `<button class="px-2.5 py-1.5 text-xs font-bold text-white bg-violet-600 rounded-lg">${p}</button>`;
    } else {
      html += `<button onclick="${loadFn}(${p})" class="px-2.5 py-1.5 text-xs text-slate-500 hover:text-violet-600 rounded-lg hover:bg-violet-50 transition-colors">${p}</button>`;
    }
  }
  if (endPage < totalPages) {
    html += `<button onclick="${loadFn}(${endPage + 1})" class="px-2.5 py-1.5 text-xs text-slate-500 hover:text-violet-600 rounded-lg hover:bg-violet-50 transition-colors">&gt;</button>`;
  }
  html += '</div>';
  container.innerHTML = html;
}

function showSkeleton(tbodyId, cols) {
  const widths = ['w-8', 'w-20', '', 'w-12', 'w-16', 'w-20', 'w-16'];
  const cells = Array.from({ length: cols }, (_, i) =>
    `<td class="py-3 px-4"><span class="cs-skeleton ${widths[i] || 'w-full'}" style="width:${i === 2 ? '70%' : ''}">&nbsp;</span></td>`
  ).join('');
  document.getElementById(tbodyId).innerHTML = Array(8).fill(`<tr>${cells}</tr>`).join('');
}

function showEmpty(tbodyId, colspan, msg) {
  document.getElementById(tbodyId).innerHTML =
    `<tr><td colspan="${colspan}" class="py-10 text-center text-sm text-slate-400">${msg}</td></tr>`;
}

// ── FAQ ──────────────────────────────────────────────────
const FAQ_SIZE = 10;

function loadFaq(page, bustCache) {
  const keyword = document.getElementById('faq-search').value.trim();
  const faqCtgCd = document.getElementById('faq-filter-ctg').value;
  const cacheKey = `${page}|${keyword}|${faqCtgCd}`;

  if (bustCache) cacheClear('faq');
  const cached = cacheGet('faq', cacheKey);
  if (cached) {
    renderFaqTable(cached.items);
    renderPagination('faq-pagination', cached.totalCount, page, FAQ_SIZE, 'loadFaq');
    return;
  }

  const params = new URLSearchParams({ page, size: FAQ_SIZE });
  if (keyword) params.append('keyword', keyword);
  if (faqCtgCd) params.append('faqCtgCd', faqCtgCd);

  const savedScroll = window.scrollY;
  showSkeleton('faq-table-body', 7);

  fetch('/admin/board/faq/paged?' + params)
    .then(r => { if (!r.ok) throw new Error('FAQ API ' + r.status); return r.json(); })
    .then(data => {
      cacheSet('faq', cacheKey, data);
      renderFaqTable(data.items);
      renderPagination('faq-pagination', data.totalCount, page, FAQ_SIZE, 'loadFaq');
      window.scrollTo({ top: savedScroll, behavior: 'instant' });
    })
    .catch(e => { console.error(e); showEmpty('faq-table-body', 7, '오류: ' + e.message); });
}

function renderFaqTable(items) {
  if (!items || items.length === 0) { showEmpty('faq-table-body', 7, '등록된 FAQ가 없습니다.'); return; }
  document.getElementById('faq-table-body').innerHTML = items.map(faq => `
    <tr class="hover:bg-slate-50 transition-colors">
      <td class="py-3 px-4 text-center text-xs text-slate-400">${faq.postSn}</td>
      <td class="py-3 px-4 text-center">
        <span class="text-xs bg-blue-50 text-blue-600 font-semibold px-2 py-0.5 rounded-full">${faq.faqCtgNm || ''}</span>
      </td>
      <td class="py-3 px-4 text-center text-xs text-slate-500">${faq.faqSubCtgNm || ''}</td>
      <td class="py-3 px-4 text-slate-800 font-medium">
        <a href="/admin/board/faq/${faq.postSn}" class="hover:text-violet-600 transition-colors">${faq.postSj}</a>
      </td>
      <td class="py-3 px-4 text-center">
        ${faq.topFixYn === 'Y'
          ? '<span class="inline-block bg-blue-600 text-white text-[10px] font-bold px-1.5 py-0.5 rounded">BEST</span>'
          : '<span class="text-xs text-slate-300">-</span>'}
      </td>
      <td class="py-3 px-4 text-center text-xs text-slate-400">${(faq.regDt || '').slice(0, 10)}</td>
      <td class="py-3 px-4 text-center">
        <div class="flex items-center justify-center gap-2">
          <a href="/admin/board/faq/edit/${faq.postSn}" class="text-xs text-violet-600 font-semibold hover:underline">수정</a>
          <span class="text-slate-300 text-xs">|</span>
          <form action="/admin/board/faq/delete/${faq.postSn}" method="post" onsubmit="return confirm('삭제하시겠습니까?')" class="m-0 p-0 flex">
            <button type="submit" class="text-xs text-red-500 font-semibold hover:underline cursor-pointer">삭제</button>
          </form>
        </div>
      </td>
    </tr>
  `).join('');
}

// ── 공지사항 ─────────────────────────────────────────────
const NOTICE_SIZE = 10;

function loadNotice(page, bustCache) {
  const keyword = document.getElementById('notice-search').value.trim();
  const noticeTypeCd = document.getElementById('notice-filter-type').value;
  const cacheKey = `${page}|${keyword}|${noticeTypeCd}`;

  if (bustCache) cacheClear('notice');
  const cached = cacheGet('notice', cacheKey);
  if (cached) {
    renderNoticeTable(cached.items);
    renderPagination('notice-pagination', cached.totalCount, page, NOTICE_SIZE, 'loadNotice');
    return;
  }

  const params = new URLSearchParams({ page, size: NOTICE_SIZE });
  if (keyword) params.append('keyword', keyword);
  if (noticeTypeCd) params.append('noticeTypeCd', noticeTypeCd);

  const savedScroll = window.scrollY;
  showSkeleton('notice-table-body', 6);

  fetch('/admin/board/notice/paged?' + params)
    .then(r => { if (!r.ok) throw new Error('Notice API ' + r.status); return r.json(); })
    .then(data => {
      cacheSet('notice', cacheKey, data);
      renderNoticeTable(data.items);
      renderPagination('notice-pagination', data.totalCount, page, NOTICE_SIZE, 'loadNotice');
      window.scrollTo({ top: savedScroll, behavior: 'instant' });
    })
    .catch(e => { console.error(e); showEmpty('notice-table-body', 6, '오류: ' + e.message); });
}

function renderNoticeTable(items) {
  if (!items || items.length === 0) { showEmpty('notice-table-body', 6, '등록된 공지사항이 없습니다.'); return; }
  const typeMap = { '01': ['blue', '일반공지'], '02': ['orange', '이벤트'], '03': ['red', '점검'] };
  document.getElementById('notice-table-body').innerHTML = items.map(notice => {
    const [color, label] = typeMap[notice.noticeTypeCd] || ['slate', notice.noticeTypeNm || ''];
    return `
      <tr class="hover:bg-slate-50 transition-colors">
        <td class="py-3 px-4 text-center text-xs text-slate-400">${notice.postSn}</td>
        <td class="py-3 px-4 text-center">
          <span class="text-xs bg-${color}-50 text-${color}-600 font-semibold px-2 py-0.5 rounded-full">${label}</span>
        </td>
        <td class="py-3 px-4 text-slate-800 font-medium">
          <a href="/admin/board/notice/${notice.postSn}" class="hover:text-violet-600 transition-colors">${notice.postSj}</a>
        </td>
        <td class="py-3 px-4 text-center">
          ${notice.popupExpsYn === 'Y'
            ? '<span class="text-xs bg-violet-50 text-violet-600 font-semibold px-2 py-0.5 rounded-full">Y</span>'
            : '<span class="text-xs text-slate-300">-</span>'}
        </td>
        <td class="py-3 px-4 text-center text-xs text-slate-400">${(notice.regDt || '').slice(0, 10)}</td>
        <td class="py-3 px-4 text-center">
          <div class="flex items-center justify-center gap-2">
            <a href="/admin/board/notice/edit/${notice.postSn}" class="text-xs text-violet-600 font-semibold hover:underline">수정</a>
            <span class="text-slate-300 text-xs">|</span>
            <form action="/admin/board/notice/delete/${notice.postSn}" method="post" onsubmit="return confirm('삭제하시겠습니까?')" class="m-0 p-0 flex">
              <button type="submit" class="text-xs text-red-500 font-semibold hover:underline cursor-pointer">삭제</button>
            </form>
          </div>
        </td>
      </tr>
    `;
  }).join('');
}

// ── QnA ─────────────────────────────────────────────────
const QNA_SIZE = 10;

function loadQna(page, bustCache) {
  const keyword = document.getElementById('qna-search').value.trim();
  const answStatCd = document.getElementById('qna-filter-stat').value;
  const qnaCtgCd = document.getElementById('qna-filter-ctg').value;
  const cacheKey = `${page}|${keyword}|${answStatCd}|${qnaCtgCd}`;

  if (bustCache) cacheClear('qna');
  const cached = cacheGet('qna', cacheKey);
  if (cached) {
    renderQnaTable(cached.items);
    renderPagination('qna-pagination', cached.totalCount, page, QNA_SIZE, 'loadQna');
    return;
  }

  const params = new URLSearchParams({ page, size: QNA_SIZE });
  if (keyword) params.append('keyword', keyword);
  if (answStatCd) params.append('answStatCd', answStatCd);
  if (qnaCtgCd) params.append('qnaCtgCd', qnaCtgCd);

  const savedScroll = window.scrollY;
  showSkeleton('qna-table-body', 6);

  fetch('/admin/board/qna/paged?' + params)
    .then(r => { if (!r.ok) throw new Error('QnA API ' + r.status); return r.json(); })
    .then(data => {
      cacheSet('qna', cacheKey, data);
      renderQnaTable(data.items);
      renderPagination('qna-pagination', data.totalCount, page, QNA_SIZE, 'loadQna');
      window.scrollTo({ top: savedScroll, behavior: 'instant' });
    })
    .catch(e => { console.error(e); showEmpty('qna-table-body', 6, '오류: ' + e.message); });
}

function renderQnaTable(items) {
  if (!items || items.length === 0) { showEmpty('qna-table-body', 6, '등록된 QnA가 없습니다.'); return; }
  document.getElementById('qna-table-body').innerHTML = items.map(qna => `
    <tr class="hover:bg-slate-50 transition-colors">
      <td class="py-3 px-4 text-center text-xs text-slate-400">${qna.postSn}</td>
      <td class="py-3 px-4 text-center">
        <span class="text-xs bg-blue-50 text-blue-600 font-semibold px-2 py-0.5 rounded-full">${qna.qnaCtgNm || ''}</span>
      </td>
      <td class="py-3 px-4 text-slate-800 font-medium">
        <a href="/admin/board/qna/${qna.postSn}" class="hover:text-violet-600 transition-colors">${qna.postSj}</a>
      </td>
      <td class="py-3 px-4 text-center">
        ${qna.secrYn === 'Y'
          ? '<span class="text-xs bg-slate-100 text-slate-500 font-semibold px-2 py-0.5 rounded-full"><i class="fa-solid fa-lock text-[10px]"></i></span>'
          : '<span class="text-xs text-slate-300">-</span>'}
      </td>
      <td class="py-3 px-4 text-center">
        ${qna.answStatCd === '01'
          ? '<span class="text-xs bg-amber-50 text-amber-600 font-semibold px-2 py-0.5 rounded-full">답변대기</span>'
          : '<span class="text-xs bg-emerald-50 text-emerald-600 font-semibold px-2 py-0.5 rounded-full">답변완료</span>'}
      </td>
      <td class="py-3 px-4 text-center text-xs text-slate-400">${(qna.regDt || '').slice(0, 10)}</td>
    </tr>
  `).join('');
}

// ── 자료실 ────────────────────────────────────────────────
const DATAROOM_SIZE = 10;

function loadDataRoom(page, bustCache) {
  const keyword = document.getElementById('dataroom-search').value.trim();
  const dataCtg = document.getElementById('dataroom-filter-ctg').value;
  const cacheKey = `${page}|${keyword}|${dataCtg}`;

  if (bustCache) cacheClear('dataroom');
  const cached = cacheGet('dataroom', cacheKey);
  if (cached) {
    renderDataRoomTable(cached.items);
    renderPagination('dataroom-pagination', cached.totalCount, page, DATAROOM_SIZE, 'loadDataRoom');
    return;
  }

  const params = new URLSearchParams({ page, size: DATAROOM_SIZE });
  if (keyword) params.append('keyword', keyword);
  if (dataCtg) params.append('dataCtg', dataCtg);

  const savedScroll = window.scrollY;
  showSkeleton('dataroom-table-body', 7);

  fetch('/admin/board/dataroom/paged?' + params)
    .then(r => { if (!r.ok) throw new Error('DataRoom API ' + r.status); return r.json(); })
    .then(data => {
      cacheSet('dataroom', cacheKey, data);
      renderDataRoomTable(data.items);
      renderPagination('dataroom-pagination', data.totalCount, page, DATAROOM_SIZE, 'loadDataRoom');
      window.scrollTo({ top: savedScroll, behavior: 'instant' });
    })
    .catch(e => { console.error(e); showEmpty('dataroom-table-body', 7, '오류: ' + e.message); });
}

function renderDataRoomTable(items) {
  if (!items || items.length === 0) { showEmpty('dataroom-table-body', 7, '등록된 자료가 없습니다.'); return; }
  document.getElementById('dataroom-table-body').innerHTML = items.map(dr => `
    <tr class="hover:bg-slate-50 transition-colors">
      <td class="py-3 px-4 text-center text-xs text-slate-400">${dr.postSn}</td>
      <td class="py-3 px-4 text-center">
        <span class="text-xs bg-blue-50 text-blue-600 font-semibold px-2 py-0.5 rounded-full">${dr.dataCtgNm || ''}</span>
      </td>
      <td class="py-3 px-4 text-slate-800 font-medium">
        <a href="/admin/board/dataroom/${dr.postSn}" class="hover:text-violet-600 transition-colors">${dr.postSj}</a>
      </td>
      <td class="py-3 px-4 text-center">
        ${dr.accsLmtCd === '01'
          ? '<span class="text-xs bg-emerald-50 text-emerald-600 font-semibold px-2 py-0.5 rounded-full">전체공개</span>'
          : '<span class="text-xs bg-amber-50 text-amber-600 font-semibold px-2 py-0.5 rounded-full">회원전용</span>'}
      </td>
      <td class="py-3 px-4 text-center">
        ${dr.orgnFileNm
          ? '<span class="text-xs bg-slate-100 text-slate-600 font-semibold px-2 py-0.5 rounded-full"><i class="fa-solid fa-paperclip text-[10px]"></i></span>'
          : '<span class="text-xs text-slate-300">-</span>'}
      </td>
      <td class="py-3 px-4 text-center text-xs text-slate-400">${(dr.regDt || '').slice(0, 10)}</td>
      <td class="py-3 px-4 text-center">
        <div class="flex items-center justify-center gap-2">
          <a href="/admin/board/dataroom/edit/${dr.postSn}" class="text-xs text-violet-600 font-semibold hover:underline">수정</a>
          <span class="text-slate-300 text-xs">|</span>
          <form action="/admin/board/dataroom/delete/${dr.postSn}" method="post" onsubmit="return confirm('삭제하시겠습니까?')" class="m-0 p-0 flex">
            <button type="submit" class="text-xs text-red-500 font-semibold hover:underline cursor-pointer">삭제</button>
          </form>
        </div>
      </td>
    </tr>
  `).join('');
}

// ── 초기 로드 ─────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', function () {
  const params = new URLSearchParams(window.location.search);
  const tab = params.get('tab') || 'tab-faq';
  const idx = ['tab-faq', 'tab-notice', 'tab-qna', 'tab-dataroom'].indexOf(tab);
  const panel = document.getElementById(tab);
  const btn = document.querySelectorAll('.cs-tab-btn')[idx >= 0 ? idx : 0];

  if (panel && btn) {
    document.querySelectorAll('.cs-tab-btn').forEach(b => b.classList.remove('active'));
    document.querySelectorAll('.cs-tab-panel').forEach(p => p.classList.remove('active'));
    btn.classList.add('active');
    panel.classList.add('active');
  }

  loadTabData(tab, 1);
});
