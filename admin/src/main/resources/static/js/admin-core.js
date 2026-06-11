/**
 * HERMES 관리자 공통 코어
 */

/* ==========================================================================
   1. 알림 UI
   ========================================================================== */

/**
 * 글로벌 토스트 알림 (어느 페이지에서나 즉시 호출 가능)
 * @param {string} message
 * @param {'success'|'error'} type
 */
function showHermesToast(message, type = 'success') {
  let container = document.getElementById('hm-toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'hm-toast-container';
    container.className = 'fixed bottom-6 right-6 z-50 space-y-3 pointer-events-none';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = `px-5 py-3.5 rounded-xl shadow-xl text-xs font-bold text-white transition-all duration-300 transform translate-y-4 opacity-0 flex items-center gap-3 pointer-events-auto ${
    type === 'success' ? 'bg-slate-900' : 'bg-rose-600'
  }`;
  const icon = type === 'success'
    ? '<i class="fa-solid fa-circle-check text-emerald-400 text-sm"></i>'
    : '<i class="fa-solid fa-circle-exclamation text-white text-sm"></i>';
  toast.innerHTML = `${icon} <span>${message.replaceAll('\n', '<br>')}</span>`;
  container.appendChild(toast);
  setTimeout(() => toast.classList.remove('translate-y-4', 'opacity-0'), 10);
  setTimeout(() => {
    toast.classList.add('opacity-0', 'translate-y-4');
    setTimeout(() => { toast.remove(); if (container.children.length === 0) container.remove(); }, 300);
  }, 3000);
}

/** 경고(amber) 토스트 — 폼 입력 오류 안내용 */
function showWarningToast(message) {
  let container = document.getElementById('hm-toast-container');
  if (!container) {
    container = document.createElement('div');
    container.id = 'hm-toast-container';
    container.className = 'fixed bottom-6 right-6 z-50 space-y-3 pointer-events-none';
    document.body.appendChild(container);
  }
  const toast = document.createElement('div');
  toast.className = 'px-5 py-3.5 rounded-xl shadow-xl text-xs font-bold text-white bg-amber-500 transition-all duration-300 transform translate-y-4 opacity-0 flex items-center gap-3 pointer-events-auto';
  toast.innerHTML = '<i class="fa-solid fa-triangle-exclamation text-sm"></i> <span>' + message + '</span>';
  container.appendChild(toast);
  setTimeout(() => toast.classList.remove('translate-y-4', 'opacity-0'), 10);
  setTimeout(() => {
    toast.classList.add('opacity-0', 'translate-y-4');
    setTimeout(() => toast.remove(), 300);
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

/* ==========================================================================
   2. 공통코드 & Select 초기화
   ========================================================================== */

/**
 * 공통코드 API 에서 옵션을 로딩하여 select 에 채움.
 * - select 에 data-ts-defer 속성이 있어야 CustomSelect 자동 초기화에서 제외됨.
 * - 로딩 완료 후 initCustomSelect(el) 을 호출하여 CustomSelect 를 초기화.
 * @param {string} selectId
 * @param {string} clCode    - 공통코드 분류번호 (예: '200')
 * @param {string} [allLabel] - 첫 번째 빈 옵션 텍스트. null 이면 빈 옵션 없음
 */
async function loadCommonCodes(selectId, clCode, allLabel = null) {
  const el = document.getElementById(selectId);
  if (!el) return;
  try {
    const res   = await fetch('/admin/common-codes/options/' + encodeURIComponent(clCode));
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
      opt.value       = c.comCd;
      opt.textContent = c.comCdNm;
      el.appendChild(opt);
    });
  } catch (e) {
    console.warn('[loadCommonCodes] 로딩 실패:', clCode, e);
  }
  if (window.initCustomSelect) window.initCustomSelect(el);
}

/**
 * root 내 [data-ts-defer][data-cl-code] select 를 모두 찾아 loadCommonCodes 를 병렬 실행.
 * - data-cl-code  : 공통코드 분류번호
 * - data-all-label: 첫 번째 빈 옵션 텍스트 (없으면 생략)
 * - data-ts-dropup: 속성이 있으면 드롭다운을 위로 펼침
 * @param {Element} [root=document]
 */
async function initDeferredSelects(root) {
  const selects = Array.from(
    (root || document).querySelectorAll('select[data-ts-defer][data-cl-code]')
  );
  await Promise.all(selects.map(async (el) => {
    const clCode   = el.getAttribute('data-cl-code');
    const allLabel = el.hasAttribute('data-all-label') ? el.getAttribute('data-all-label') : null;
    await loadCommonCodes(el.id, clCode, allLabel);
    if (el.hasAttribute('data-ts-dropup') && el.customSelect)
      el.customSelect.wrapper.classList.add('ts-dropup');
  }));
}

/* ==========================================================================
   3. CustomSelect — Tom Select 대체 커스텀 드롭다운
   --------------------------------------------------------------------------
   ========================================================================== */
class CustomSelect {
  constructor(el) {
    this._el   = el;
    this._open = false;
    this._build();
    this._bind();
    el.customSelect = this;
  }

  _build() {
    const el = this._el;
    el.style.display = 'none';

    const wrapper  = document.createElement('div');
    wrapper.className = 'cs-wrapper ' + el.className;

    const control = document.createElement('button');
    control.type      = 'button';
    control.className = 'cs-control';

    const dropdown = document.createElement('ul');
    dropdown.className = 'cs-dropdown';
    dropdown.setAttribute('role', 'listbox');

    wrapper.appendChild(control);
    wrapper.appendChild(dropdown);
    el.parentNode.insertBefore(wrapper, el.nextSibling);

    this.wrapper    = wrapper;
    this._control   = control;
    this._dropdown  = dropdown;

    this._syncOptions();
    this._updateControl();
  }

  _syncOptions() {
    const dropdown = this._dropdown;
    dropdown.innerHTML = '';
    Array.from(this._el.options).forEach(opt => {
      if (opt.hidden) return;
      const li = document.createElement('li');
      li.setAttribute('role', 'option');
      li.setAttribute('tabindex', '-1');
      li.dataset.value = opt.value;
      li.textContent   = opt.textContent;
      if (opt.selected) li.classList.add('cs-selected');
      dropdown.appendChild(li);
    });
  }

  _updateControl() {
    const el      = this._el;
    const selected = el.options[el.selectedIndex];
    this._control.textContent = selected ? selected.textContent : '';
    this._control.classList.toggle('cs-placeholder', !el.value);
  }

  _bind() {
    this._control.addEventListener('click', e => {
      e.stopPropagation();
      if (!this._el.disabled) this._toggle();
    });

    this._dropdown.addEventListener('click', e => {
      const li = e.target.closest('li[data-value]');
      if (li) this._pick(li.dataset.value);
    });

    this._control.addEventListener('keydown', e => {
      if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        if (!this._el.disabled) this._toggle();
      } else if (e.key === 'ArrowDown') {
        e.preventDefault();
        if (!this._open) this._openDropdown();
        this._moveFocus(1);
      } else if (e.key === 'ArrowUp') {
        e.preventDefault();
        if (!this._open) this._openDropdown();
        this._moveFocus(-1);
      } else if (e.key === 'Escape') {
        this._closeDropdown();
      }
    });

    this._dropdown.addEventListener('keydown', e => {
      if      (e.key === 'ArrowDown') { e.preventDefault(); this._moveFocus(1); }
      else if (e.key === 'ArrowUp')   { e.preventDefault(); this._moveFocus(-1); }
      else if (e.key === 'Enter' || e.key === ' ') {
        e.preventDefault();
        const li = this._dropdown.querySelector('li:focus');
        if (li) this._pick(li.dataset.value);
      } else if (e.key === 'Escape') {
        this._closeDropdown();
        this._control.focus();
      }
    });

    this._docClick = e => { if (!this.wrapper.contains(e.target)) this._closeDropdown(); };
    document.addEventListener('click', this._docClick);
  }

  _toggle() { this._open ? this._closeDropdown() : this._openDropdown(); }

  _openDropdown() {
    this._open = true;
    this.wrapper.classList.add('cs-open');
    const sel = this._dropdown.querySelector('li.cs-selected') || this._dropdown.querySelector('li');
    if (sel) sel.focus();
  }

  _closeDropdown() {
    this._open = false;
    this.wrapper.classList.remove('cs-open');
  }

  _pick(value) {
    this._el.value = value;
    this._syncOptions();
    this._updateControl();
    this._closeDropdown();
    this._control.focus();
    this._el.dispatchEvent(new Event('change', { bubbles: true }));
  }

  _moveFocus(dir) {
    const items   = Array.from(this._dropdown.querySelectorAll('li'));
    if (!items.length) return;
    const focused = this._dropdown.querySelector('li:focus');
    const idx     = focused ? items.indexOf(focused) + dir : (dir > 0 ? 0 : items.length - 1);
    items[Math.max(0, Math.min(items.length - 1, idx))].focus();
  }

  setValue(value) { this._el.value = value; this._syncOptions(); this._updateControl(); }
  getValue()      { return this._el.value; }
  refresh()       { this._syncOptions(); this._updateControl(); }

  destroy() {
    document.removeEventListener('click', this._docClick);
    this.wrapper.remove();
    this._el.style.display = '';
    delete this._el.customSelect;
  }
}

function initCustomSelect(el) {
  if (el.customSelect) el.customSelect.destroy();
  return new CustomSelect(el);
}

window.initCustomSelect = initCustomSelect;

/* ==========================================================================
   4. 폼 공통 유틸리티
   ========================================================================== */

/* --- 표시 & 포맷 --- */

/** 전화번호를 하이픈 포함 형식으로 변환 (렌더링 전용, 입력 이벤트용은 formatPhone) */
function formatPhoneDisplay(tel) {
  if (!tel) return '-';
  const v = tel.replace(/\D/g, '');
  if (v.length === 11) return v.slice(0, 3) + '-' + v.slice(3, 7) + '-' + v.slice(7);
  if (v.length === 10) return v.slice(0, 3) + '-' + v.slice(3, 6) + '-' + v.slice(6);
  return tel;
}

function escHtml(s) {
  return String(s == null ? '' : s)
    .replace(/&/g, '&amp;').replace(/</g, '&lt;')
    .replace(/>/g, '&gt;').replace(/"/g, '&quot;');
}

/** 휴대폰 input 실시간 포맷 (010-XXXX-XXXX) */
function formatPhone(el) {
  let v = el.value.replace(/\D/g, '');
  if (v.length > 11) v = v.slice(0, 11);
  if (v.length >= 3 && !/^010/.test(v)) {
    showHermesToast('휴대폰 번호는 010으로 시작해야 합니다.', 'error');
    el.value = '010-';
    return;
  }
  if (v.length >= 8)      v = v.slice(0, 3) + '-' + v.slice(3, 7) + '-' + v.slice(7);
  else if (v.length >= 4) v = v.slice(0, 3) + '-' + v.slice(3);
  el.value = v;
}

/** 이름 input 실시간 포맷 (한글·영문 외 즉시 제거, IME 조합 중 자모 허용) */
function formatName(el) {
  const before = el.value;
  el.value = before.replace(/[^가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z]/g, '');
  if (el.value !== before)
    showHermesToast('이름은 한글 또는 영문만 입력 가능합니다.', 'error');
}

/* --- 입력 검증 (blur 이벤트용) --- */

function blurValidateName(el) {
  const v = el.value.trim();
  if (!v) return;
  if (v.length < 2 || v.length > 50) { showHermesToast('이름은 2자 이상 50자 이하로 입력해주세요.', 'error'); return; }
  if (!/^[가-힣a-zA-Z]+$/.test(v))   showHermesToast('이름은 한글 또는 영문만 입력 가능합니다.', 'error');
}

function blurValidatePhone(el) {
  const v = el.value.trim();
  if (!v) return;
  if (!/^010-\d{3,4}-\d{4}$/.test(v))
    showHermesToast('올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)', 'error');
}

function blurValidateEmail(el) {
  const v = el.value.trim();
  if (!v) return;
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v)) showHermesToast('유효한 이메일 형식이 아닙니다.', 'error');
  else if (v.length > 100)                    showHermesToast('이메일은 100자 이하로 입력해주세요.', 'error');
}

/* --- 모달 --- */

function openModal(id)  { document.getElementById(id).classList.remove('hidden'); }
function closeModal(id) { document.getElementById(id).classList.add('hidden'); }

/* --- 주민등록번호 --- */

function validateRrnChecksum(digits) {
  const weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
  const sum = weights.reduce((acc, w, i) => acc + parseInt(digits[i], 10) * w, 0);
  return parseInt(digits[12], 10) === (11 - (sum % 11)) % 10;
}

function maskRrn(formatted) {
  const digits = formatted.replace(/\D/g, '');
  if (digits.length <= 6) return formatted;
  return digits.slice(0, 6) + '-' + digits[6] + '*'.repeat(Math.max(0, digits.length - 7));
}

var rrnRealValue = ''; /* 실제 값 — 페이지별 autoFillTempPw 가 참조 */
var rrnEyeOpen  = false;

function onRrnInput(el) {
  let digits = el.value.replace(/\D/g, '').replace(/\*/g, '');
  if (digits.length > 13) digits = digits.slice(0, 13);
  const formatted = digits.length > 6 ? digits.slice(0, 6) + '-' + digits.slice(6) : digits;
  rrnRealValue = formatted;
  document.getElementById('new-rrn').value = formatted;
  el.value = formatted;
  autoFillTempPw(); /* 페이지별 함수 (employees.js / students.js) */
}

function onRrnFocus() {
  document.getElementById('new-rrn-display').value = rrnRealValue;
}

function onRrnBlur() {
  if (!rrnEyeOpen)
    document.getElementById('new-rrn-display').value = maskRrn(rrnRealValue);
}

function toggleRrnVisibility() {
  rrnEyeOpen = !rrnEyeOpen;
  const icon    = document.getElementById('rrn-eye-icon');
  const display = document.getElementById('new-rrn-display');
  icon.className = rrnEyeOpen ? 'fa-regular fa-eye text-sm' : 'fa-regular fa-eye-slash text-sm';
  display.value  = rrnEyeOpen ? rrnRealValue : maskRrn(rrnRealValue);
}

/* --- 우편번호 --- */

/** 신규 등록 폼 우편번호 (postcode / address / detailAddress) */
function searchZipCode() {
  new daum.Postcode({
    oncomplete: function (data) {
      const addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
      document.getElementById('postcode').value    = data.zonecode;
      document.getElementById('address').value     = addr;
      document.getElementById('detailAddress').focus();
    },
  }).open();
}

/** 상세 정보 수정 폼 우편번호 (edit-zipcode / edit-addr / edit-addr-detail) */
function searchDetailZipCode() {
  new daum.Postcode({
    oncomplete: function (data) {
      const addr = data.userSelectedType === 'R' ? data.roadAddress : data.jibunAddress;
      document.getElementById('edit-zipcode').value    = data.zonecode;
      document.getElementById('edit-addr').value       = addr;
      document.getElementById('edit-addr-detail').focus();
    },
  }).open();
}

/* ==========================================================================
   5. 자동 초기화 (defer 보장 — DOM 파싱 완료 후 실행)
   ========================================================================== */
document.querySelectorAll('select.hm-input:not([data-ts-defer])').forEach(function (el) {
  initCustomSelect(el);
});
