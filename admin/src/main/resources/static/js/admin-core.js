/**
 * HERMES 글로벌 공통 코어 엔진 스크립트
 * (특정 레이아웃 종속 이벤트는 모두 layout.js로 격리)
 */

/**
 * 글로벌 커스텀 토스트 알림 시스템 (어느 화면에서나 자바스크립트 한 줄로 즉시 호출 가능)
 * @param {string} message - 표시할 메시지 내용
 * @param {'success' | 'error'} type - 알림 종류 유형
 */
function showHermesToast(message, type = "success") {
  let container = document.getElementById("hm-toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "hm-toast-container";
    container.className =
      "fixed bottom-6 right-6 z-50 space-y-3 pointer-events-none";
    document.body.appendChild(container);
  }

  const toast = document.createElement("div");
  toast.className = `px-5 py-3.5 rounded-xl shadow-xl text-xs font-bold text-white transition-all duration-300 transform translate-y-4 opacity-0 flex items-center gap-3 pointer-events-auto ${
    type === "success" ? "bg-slate-900" : "bg-rose-600"
  }`;

  const icon =
    type === "success"
      ? '<i class="fa-solid fa-circle-check text-emerald-400 text-sm"></i>'
      : '<i class="fa-solid fa-circle-exclamation text-white text-sm"></i>';

  // 메시지 내 줄바꿈을 <br> 태그로 변환하여 HTML로 표시
  const brokenMessage = message.replaceAll("\n", "<br>");

  toast.innerHTML = `${icon} <span>${brokenMessage}</span>`;
  container.appendChild(toast);

  setTimeout(() => {
    toast.classList.remove("translate-y-4", "opacity-0");
  }, 10);

  setTimeout(() => {
    toast.classList.add("opacity-0", "translate-y-4");
    setTimeout(() => {
      toast.remove();
      if (container.children.length === 0) container.remove();
    }, 300);
  }, 3000);
}

function showHermesLoading(message = '처리 중...') {
  let el = document.getElementById('hm-loading-overlay');
  if (!el) {
    el = document.createElement('div');
    el.id = 'hm-loading-overlay';
    el.className = 'fixed inset-0 z-[9999] flex flex-col items-center justify-center bg-black/40 backdrop-blur-sm';
    el.innerHTML = `
      <div class="bg-white rounded-2xl shadow-2xl px-10 py-8 flex flex-col items-center gap-4 min-w-[200px]">
        <svg class="animate-spin h-10 w-10" style="color:#7c3aed" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"></circle>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8v4a4 4 0 00-4 4H4z"></path>
        </svg>
        <p id="hm-loading-msg" class="text-sm font-semibold text-slate-700 text-center">${message}</p>
      </div>`;
    document.body.appendChild(el);
  } else {
    const msg = el.querySelector('#hm-loading-msg');
    if (msg) msg.textContent = message;
    el.classList.remove('hidden');
  }
}

function hideHermesLoading() {
  const el = document.getElementById('hm-loading-overlay');
  if (el) el.classList.add('hidden');
}

/**
 * 공통코드 API에서 옵션을 로딩하여 select 엘리먼트에 채웁니다.
 * - select에 data-ts-defer 속성이 필요합니다 (Tom Select 자동 초기화 방지).
 * - 로딩 완료 후 Tom Select를 초기화합니다.
 * @param {string} selectId   - select 엘리먼트의 id
 * @param {string} clCode     - 공통코드 분류번호 (예: '200', '201')
 * @param {string} [allLabel] - 첫 번째 빈 옵션 텍스트. null이면 빈 옵션 없음
 */
async function loadCommonCodes(selectId, clCode, allLabel = null) {
  const el = document.getElementById(selectId);
  if (!el) return;
  try {
    const res = await fetch('/admin/common-codes/options/' + encodeURIComponent(clCode));
    const codes = await res.json();
    el.innerHTML = '';
    if (allLabel !== null) {
      const opt = document.createElement('option');
      opt.value = '';
      opt.textContent = allLabel;
      el.appendChild(opt);
    }
    codes.forEach(function (c) {
      const opt = document.createElement('option');
      opt.value = c.comCd;
      opt.textContent = c.comCdNm;
      el.appendChild(opt);
    });
  } catch (e) {
    console.warn('[loadCommonCodes] 로딩 실패:', clCode, e);
  }
  if (window.initTomSelect) window.initTomSelect(el);
}

/**
 * 결재 등록 전 확인 모달
 * @param {{ title: string, type: 'create'|'update'|'delete', fields?: Array, target?: string, onConfirm: Function }} config
 *   - create  : fields = [{ label, value }]
 *   - update  : fields = [{ label, before, after }]  변경된 항목만 전달
 *   - delete  : target = '삭제 대상 설명'
 */
function showHermesApprovalConfirm({ title, type, fields, target, onConfirm }) {
  const existing = document.getElementById('hm-approval-confirm-overlay');
  if (existing) existing.remove();

  let bodyHtml = '';
  if (type === 'create') {
    bodyHtml = `<table class="w-full text-xs border-collapse">
      <tbody>${(fields || []).map(f => `
        <tr>
          <th class="text-left py-1.5 px-3 bg-slate-50 text-slate-500 font-semibold w-1/3 border border-slate-200 whitespace-nowrap">${f.label}</th>
          <td class="py-1.5 px-3 border border-slate-200 text-slate-700">${f.value ?? '-'}</td>
        </tr>`).join('')}
      </tbody>
    </table>`;
  } else if (type === 'update') {
    const changed = (fields || []).filter(f => String(f.before) !== String(f.after));
    if (changed.length === 0) {
      showHermesToast('변경된 내용이 없습니다.', 'error');
      return;
    }
    const rows = changed;
    bodyHtml = `<table class="w-full text-xs border-collapse">
      <thead><tr>
        <th class="py-1.5 px-3 bg-slate-50 border border-slate-200 text-slate-400 font-semibold w-1/4"></th>
        <th class="py-1.5 px-3 bg-slate-100 border border-slate-200 text-slate-500 font-semibold text-center">변경 전</th>
        <th class="py-1.5 px-3 bg-violet-50 border border-slate-200 text-violet-600 font-semibold text-center">변경 후</th>
      </tr></thead>
      <tbody>${rows.map(f => `
        <tr>
          <th class="text-left py-1.5 px-3 bg-slate-50 border border-slate-200 text-slate-500 font-semibold whitespace-nowrap">${f.label}</th>
          <td class="py-1.5 px-3 border border-slate-200 text-slate-400 line-through">${f.before ?? '-'}</td>
          <td class="py-1.5 px-3 border border-slate-200 text-violet-700 font-semibold">${f.after ?? '-'}</td>
        </tr>`).join('')}
      </tbody>
    </table>`;
  } else if (type === 'delete') {
    bodyHtml = `<div class="bg-red-50 border border-red-200 rounded-xl p-4 text-center">
      <i class="fa-solid fa-triangle-exclamation text-red-500 text-2xl mb-2 block"></i>
      <p class="text-sm font-semibold text-red-700">"${target}"을(를) 삭제합니다.</p>
      <p class="text-xs text-red-500 mt-1">결재 승인 후 영구 삭제됩니다.</p>
    </div>`;
  }

  const overlay = document.createElement('div');
  overlay.id = 'hm-approval-confirm-overlay';
  overlay.className = 'fixed inset-0 z-[9998] flex items-center justify-center bg-black/40 backdrop-blur-sm';
  overlay.innerHTML = `
    <div class="bg-white rounded-2xl shadow-2xl p-6 max-w-md w-full mx-4">
      <h3 class="text-sm font-bold text-slate-700 mb-4 flex items-center gap-2">
        <i class="fa-solid fa-shield-check text-violet-500"></i>${title}
      </h3>
      ${bodyHtml}
      <p class="text-xs text-slate-400 mt-3">
        <i class="fa-solid fa-circle-info mr-1 text-violet-300"></i>
        결재 등록 후 승인자의 승인 시 반영됩니다.
      </p>
      <div class="flex justify-end gap-3 mt-5">
        <button onclick="closeHermesApprovalConfirm()"
          class="cursor-pointer px-4 py-2 text-sm font-semibold text-slate-500 bg-slate-100 hover:bg-slate-200 rounded-xl transition-colors">
          취소
        </button>
        <button id="hm-approval-confirm-btn"
          class="cursor-pointer px-4 py-2 text-sm font-semibold text-white bg-violet-600 hover:bg-violet-700 rounded-xl transition-colors">
          <i class="fa-solid fa-paper-plane mr-1"></i>결재 등록
        </button>
      </div>
    </div>`;
  document.body.appendChild(overlay);
  document.getElementById('hm-approval-confirm-btn').onclick = onConfirm;
}

function closeHermesApprovalConfirm() {
  const el = document.getElementById('hm-approval-confirm-overlay');
  if (el) el.remove();
}

/**
 * root 내 [data-ts-defer][data-cl-code] select를 모두 찾아 loadCommonCodes를 병렬 실행합니다.
 * - data-cl-code  : 공통코드 분류번호 (예: "200")
 * - data-all-label: 첫 번째 빈 옵션 텍스트. 없으면 빈 옵션 없음
 * - data-ts-dropup: 속성이 있으면 Tom Select 드롭다운을 위로 펼침
 * @param {Element} [root=document] - 탐색 범위 (탭 이동 시 새 main 요소 전달)
 */
async function initDeferredSelects(root) {
  const selects = Array.from(
    (root || document).querySelectorAll('select[data-ts-defer][data-cl-code]')
  );
  await Promise.all(selects.map(function (el) {
    const clCode   = el.getAttribute('data-cl-code');
    const allLabel = el.hasAttribute('data-all-label') ? el.getAttribute('data-all-label') : null;
    return loadCommonCodes(el.id, clCode, allLabel).then(function () {
      if (el.hasAttribute('data-ts-dropup') && el.tomselect) {
        el.tomselect.wrapper.classList.add('ts-dropup');
      }
    });
  }));
}
