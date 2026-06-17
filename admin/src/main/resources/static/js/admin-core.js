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
function showHermesToast(message, type = "success") {
  let container = document.getElementById("hm-toast-container");
  if (!container) {
    container = document.createElement("div");
    container.id = "hm-toast-container";
    container.className =
      "fixed bottom-6 right-6 z-50 space-y-3 pointer-events-none";
    document.body.appendChild(container);
  }
  const styles = {
    success: { bg: "bg-slate-900",  icon: '<i class="fa-solid fa-circle-check text-emerald-400 text-sm"></i>' },
    error:   { bg: "bg-rose-600",   icon: '<i class="fa-solid fa-circle-exclamation text-white text-sm"></i>' },
    warning: { bg: "bg-amber-500",  icon: '<i class="fa-solid fa-triangle-exclamation text-sm"></i>' },
  };
  const { bg, icon } = styles[type] || styles.success;
  const toast = document.createElement("div");
  toast.className = `px-5 py-3.5 rounded-xl shadow-xl text-xs font-bold text-white transition-all duration-300 transform translate-y-4 opacity-0 flex items-center gap-3 pointer-events-auto ${bg}`;
  toast.innerHTML = `${icon} <span>${message.replaceAll("\n", "<br>")}</span>`;
  container.appendChild(toast);
  setTimeout(() => toast.classList.remove("translate-y-4", "opacity-0"), 10);
  setTimeout(() => {
    toast.classList.add("opacity-0", "translate-y-4");
    setTimeout(() => {
      toast.remove();
      if (container.children.length === 0) container.remove();
    }, 300);
  }, 3000);
}

/** @deprecated showHermesToast(msg, 'warning') 을 사용하세요 */
function showWarningToast(message) {
  showHermesToast(message, "warning");
}

function showHermesLoading(message = "처리 중...") {
  let el = document.getElementById("hm-loading-overlay");
  if (!el) {
    el = document.createElement("div");
    el.id = "hm-loading-overlay";
    el.className =
      "fixed inset-0 z-[9999] flex flex-col items-center justify-center bg-black/40 backdrop-blur-sm";
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
    const msg = el.querySelector("#hm-loading-msg");
    if (msg) msg.textContent = message;
    el.classList.remove("hidden");
  }
}

function hideHermesLoading() {
  const el = document.getElementById("hm-loading-overlay");
  if (el) el.classList.add("hidden");
}

/* ==========================================================================
   2. 공통코드 & Select 초기화
   ========================================================================== */

/**
 * 공통코드 API 에서 옵션을 로딩하여 select 에 채움.
 * - select 에 data-cs-defer 속성이 있어야 CustomSelect 자동 초기화에서 제외됨.
 * - 로딩 완료 후 initCustomSelect(el) 을 호출하여 CustomSelect 를 초기화.
 * @param {string} selectId
 * @param {string} clCode    - 공통코드 분류번호 (예: '200')
 * @param {string} [allLabel] - 첫 번째 빈 옵션 텍스트. null 이면 빈 옵션 없음
 */
async function loadCommonCodes(selectId, clCode, allLabel = null) {
  const el = document.getElementById(selectId);
  if (!el) return;
  try {
    const res = await fetch(
      "/admin/common-codes/options/" + encodeURIComponent(clCode),
    );
    const codes = await res.json();
    el.innerHTML = "";
    if (allLabel !== null) {
      const opt = document.createElement("option");
      opt.value = "";
      opt.textContent = allLabel;
      el.appendChild(opt);
    }
    codes.forEach(function (c) {
      const opt = document.createElement("option");
      opt.value = c.comCd;
      opt.textContent = c.comCdNm;
      el.appendChild(opt);
    });
  } catch (e) {
    console.warn("[loadCommonCodes] 로딩 실패:", clCode, e);
  }
  if (window.initCustomSelect) window.initCustomSelect(el);
}

/**
 * 결재 등록 전 확인 모달
 * @param {{ title: string, type: 'create'|'update'|'delete', fields?: Array, target?: string, onConfirm: Function }} config
 *   - create  : fields = [{ label, value }]
 *   - update  : fields = [{ label, before, after }]  변경된 항목만 전달
 *   - delete  : target = '삭제 대상 설명'
 */
/**
 * 현재 페이지(부서)의 대표 색상 토큰을 DOM 의 hm-*-{color} 클래스로 판별.
 * sky=강사, emerald=실장, orange=PD, blue=행정. 테마 클래스가 없으면 기본 브랜드 violet.
 */
function hmDetectDeptColor() {
  const tokens = ["sky", "emerald", "orange", "blue"];
  for (const t of tokens) {
    if (document.querySelector(`.hm-input-${t}, .hm-btn-${t}, .hm-switch-${t}`)) return t;
  }
  return "violet";
}

function showHermesApprovalConfirm({ title, type, fields, target, onConfirm, color }) {
  const existing = document.getElementById("hm-approval-confirm-overlay");
  if (existing) existing.remove();

  // color 파라미터가 있으면 우선 사용, 없으면 DOM에서 감지
  const c = color || hmDetectDeptColor();

  let bodyHtml = "";
  if (type === "create") {
    bodyHtml = `<table class="w-full text-xs border-collapse">
      <tbody>${(fields || [])
        .map(
          (f) => `
        <tr>
          <th class="text-left py-1.5 px-3 bg-slate-50 text-slate-500 font-semibold w-1/3 border border-slate-200 whitespace-nowrap">${f.label}</th>
          <td class="py-1.5 px-3 border border-slate-200 text-slate-700">${f.value ?? "-"}</td>
        </tr>`,
        )
        .join("")}
      </tbody>
    </table>`;
  } else if (type === "update") {
    const changed = (fields || []).filter(
      (f) => String(f.before) !== String(f.after),
    );
    if (changed.length === 0) {
      showHermesToast("변경된 내용이 없습니다.", "error");
      return;
    }
    const rows = changed;
    bodyHtml = `<table class="w-full text-xs border-collapse">
      <thead><tr>
        <th class="py-1.5 px-3 bg-slate-50 border border-slate-200 text-slate-400 font-semibold w-1/4"></th>
        <th class="py-1.5 px-3 bg-slate-100 border border-slate-200 text-slate-500 font-semibold text-center">변경 전</th>
        <th class="py-1.5 px-3 bg-${c}-50 border border-slate-200 text-${c}-600 font-semibold text-center">변경 후</th>
      </tr></thead>
      <tbody>${rows
        .map(
          (f) => `
        <tr>
          <th class="text-left py-1.5 px-3 bg-slate-50 border border-slate-200 text-slate-500 font-semibold whitespace-nowrap">${f.label}</th>
          <td class="py-1.5 px-3 border border-slate-200 text-slate-400 line-through">${f.before ?? "-"}</td>
          <td class="py-1.5 px-3 border border-slate-200 text-${c}-700 font-semibold">${f.after ?? "-"}</td>
        </tr>`,
        )
        .join("")}
      </tbody>
    </table>`;
  } else if (type === "delete") {
    bodyHtml = `<div class="bg-red-50 border border-red-200 rounded-xl p-4 text-center">
      <i class="fa-solid fa-triangle-exclamation text-red-500 text-2xl mb-2 block"></i>
      <p class="text-sm font-semibold text-red-700">"${target}"을(를) 삭제합니다.</p>
      <p class="text-xs text-red-500 mt-1">결재 승인 후 영구 삭제됩니다.</p>
    </div>`;
  }

  const overlay = document.createElement("div");
  overlay.id = "hm-approval-confirm-overlay";
  overlay.className =
    "fixed inset-0 z-[9998] flex items-center justify-center bg-black/40 backdrop-blur-sm";
  overlay.innerHTML = `
    <div class="bg-white rounded-2xl shadow-2xl p-6 max-w-md w-full mx-4">
      <h3 class="text-sm font-bold text-slate-700 mb-4 flex items-center gap-2">
        <i class="fa-solid fa-shield-check text-${c}-500"></i>${title}
      </h3>
      ${bodyHtml}
      <p class="text-xs text-slate-400 mt-3">
        <i class="fa-solid fa-circle-info mr-1 text-${c}-300"></i>
        결재 등록 후 승인자의 승인 시 반영됩니다.
      </p>
      <div class="flex justify-end gap-3 mt-5">
        <button onclick="closeHermesApprovalConfirm()"
          class="cursor-pointer px-4 py-2 text-sm font-semibold text-slate-500 bg-slate-100 hover:bg-slate-200 rounded-xl transition-colors">
          취소
        </button>
        <button id="hm-approval-confirm-btn"
          class="cursor-pointer px-4 py-2 text-sm font-semibold text-white bg-${c}-600 hover:bg-${c}-700 rounded-xl transition-colors">
          <i class="fa-solid fa-paper-plane mr-1"></i>결재 등록
        </button>
      </div>
    </div>`;
  document.body.appendChild(overlay);
  document.getElementById("hm-approval-confirm-btn").onclick = onConfirm;
}

function closeHermesApprovalConfirm() {
  const el = document.getElementById("hm-approval-confirm-overlay");
  if (el) el.remove();
}

/**
 * root 내 [data-cs-defer][data-cl-code] select 를 모두 찾아 loadCommonCodes 를 병렬 실행.
 * - data-cl-code  : 공통코드 분류번호
 * - data-all-label: 첫 번째 빈 옵션 텍스트 (없으면 생략)
 * - data-cs-dropup: 속성이 있으면 드롭다운을 위로 펼침
 * @param {Element} [root=document]
 */
async function initDeferredSelects(root) {
  const selects = Array.from(
    (root || document).querySelectorAll("select[data-cs-defer][data-cl-code]"),
  );
  await Promise.all(
    selects.map(async (el) => {
      const clCode = el.getAttribute("data-cl-code");
      const allLabel = el.hasAttribute("data-all-label")
        ? el.getAttribute("data-all-label")
        : null;
      await loadCommonCodes(el.id, clCode, allLabel);
      if (el.hasAttribute("data-cs-dropup") && el.customSelect)
        el.customSelect.wrapper.classList.add("cs-dropup");
    }),
  );
}

/* ==========================================================================
   3. CustomSelect — Tom Select 대체 커스텀 드롭다운
   --------------------------------------------------------------------------
   ========================================================================== */
class CustomSelect {
  constructor(el) {
    this._el = el;
    this._open = false;
    this._build();
    this._bind();
    el.customSelect = this;
  }

  _build() {
    const el = this._el;
    const inheritedStyle = el.style.cssText;
    el.style.display = "none";

    const wrapper = document.createElement("div");
    wrapper.className = "cs-wrapper " + el.className;
    if (inheritedStyle) wrapper.style.cssText = inheritedStyle;

    const control = document.createElement("button");
    control.type = "button";
    control.className = "cs-control";

    const dropdown = document.createElement("ul");
    dropdown.className = "cs-dropdown";
    dropdown.setAttribute("role", "listbox");

    wrapper.appendChild(control);
    wrapper.appendChild(dropdown);
    el.parentNode.insertBefore(wrapper, el.nextSibling);

    this.wrapper = wrapper;
    this._control = control;
    this._dropdown = dropdown;

    this._syncOptions();
    this._updateControl();
  }

  _syncOptions() {
    const dropdown = this._dropdown;
    dropdown.innerHTML = "";
    Array.from(this._el.options).forEach((opt) => {
      if (opt.hidden) return;
      const li = document.createElement("li");
      li.setAttribute("role", "option");
      li.setAttribute("tabindex", "-1");
      li.dataset.value = opt.value;
      li.textContent = opt.textContent;
      if (opt.selected) li.classList.add("cs-selected");
      dropdown.appendChild(li);
    });
  }

  _updateControl() {
    const el = this._el;
    const selected = el.options[el.selectedIndex];
    this._control.textContent = selected ? selected.textContent : "";
    this._control.classList.toggle("cs-placeholder", !el.value);
  }

  _bind() {
    this._control.addEventListener("click", () => {
      if (!this._el.disabled) this._toggle();
    });

    this._dropdown.addEventListener("click", (e) => {
      const li = e.target.closest("li[data-value]");
      if (li) this._pick(li.dataset.value);
    });

    this._control.addEventListener("keydown", (e) => {
      if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        if (!this._el.disabled) this._toggle();
      } else if (e.key === "ArrowDown") {
        e.preventDefault();
        if (!this._open) this._openDropdown();
        this._moveFocus(1);
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        if (!this._open) this._openDropdown();
        this._moveFocus(-1);
      } else if (e.key === "Escape") {
        this._closeDropdown();
      }
    });

    this._dropdown.addEventListener("keydown", (e) => {
      if (e.key === "ArrowDown") {
        e.preventDefault();
        this._moveFocus(1);
      } else if (e.key === "ArrowUp") {
        e.preventDefault();
        this._moveFocus(-1);
      } else if (e.key === "Enter" || e.key === " ") {
        e.preventDefault();
        const li = this._dropdown.querySelector("li:focus");
        if (li) this._pick(li.dataset.value);
      } else if (e.key === "Escape") {
        this._closeDropdown();
        this._control.focus();
      }
    });

    this._docClick = (e) => {
      if (!this.wrapper.contains(e.target)) this._closeDropdown();
    };
    document.addEventListener("click", this._docClick);
  }

  _toggle() {
    this._open ? this._closeDropdown() : this._openDropdown();
  }

  _openDropdown() {
    this._open = true;
    this.wrapper.classList.add("cs-open");
    const sel =
      this._dropdown.querySelector("li.cs-selected") ||
      this._dropdown.querySelector("li");
    if (sel) sel.focus();
  }

  _closeDropdown() {
    this._open = false;
    this.wrapper.classList.remove("cs-open");
  }

  _pick(value) {
    this._el.value = value;
    this._syncOptions();
    this._updateControl();
    this._closeDropdown();
    this._control.focus();
    this._el.dispatchEvent(new Event("change", { bubbles: true }));
  }

  _moveFocus(dir) {
    const items = Array.from(this._dropdown.querySelectorAll("li"));
    if (!items.length) return;
    const focused = this._dropdown.querySelector("li:focus");
    const idx = focused
      ? items.indexOf(focused) + dir
      : dir > 0
        ? 0
        : items.length - 1;
    items[Math.max(0, Math.min(items.length - 1, idx))].focus();
  }

  setValue(value) {
    this._el.value = value;
    this._syncOptions();
    this._updateControl();
  }
  getValue() {
    return this._el.value;
  }
  refresh() {
    this._syncOptions();
    this._updateControl();
  }

  destroy() {
    document.removeEventListener("click", this._docClick);
    this.wrapper.remove();
    this._el.style.display = "";
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
  if (!tel) return "-";
  const v = tel.replace(/\D/g, "");
  if (v.length === 11)
    return v.slice(0, 3) + "-" + v.slice(3, 7) + "-" + v.slice(7);
  if (v.length === 10)
    return v.slice(0, 3) + "-" + v.slice(3, 6) + "-" + v.slice(6);
  return tel;
}

function escHtml(s) {
  return String(s == null ? "" : s)
    .replace(/&/g, "&amp;")
    .replace(/</g, "&lt;")
    .replace(/>/g, "&gt;")
    .replace(/"/g, "&quot;");
}

/** 휴대폰 input 실시간 포맷 (010-XXXX-XXXX) */
function formatPhone(el) {
  let v = el.value.replace(/\D/g, "");
  if (v.length > 11) v = v.slice(0, 11);
  if (v.length >= 3 && !/^010/.test(v)) {
    showHermesToast("휴대폰 번호는 010으로 시작해야 합니다.", "error");
    el.value = "010-";
    return;
  }
  if (v.length >= 8) v = v.slice(0, 3) + "-" + v.slice(3, 7) + "-" + v.slice(7);
  else if (v.length >= 4) v = v.slice(0, 3) + "-" + v.slice(3);
  el.value = v;
}

/** 이름 input 실시간 포맷 (한글·영문 외 즉시 제거, IME 조합 중 자모 허용) */
function formatName(el) {
  const before = el.value;
  el.value = before.replace(/[^가-힣ㄱ-ㅎㅏ-ㅣa-zA-Z]/g, "");
  if (el.value !== before)
    showHermesToast("이름은 한글 또는 영문만 입력 가능합니다.", "error");
}

/* --- 입력 검증 (blur 이벤트용) --- */

function blurValidateName(el) {
  const v = el.value.trim();
  if (!v) return;
  if (v.length < 2 || v.length > 50) {
    showHermesToast("이름은 2자 이상 50자 이하로 입력해주세요.", "error");
    return;
  }
  if (!/^[가-힣a-zA-Z]+$/.test(v))
    showHermesToast("이름은 한글 또는 영문만 입력 가능합니다.", "error");
}

function blurValidatePhone(el) {
  const v = el.value.trim();
  if (!v) return;
  if (!/^010-\d{3,4}-\d{4}$/.test(v))
    showHermesToast(
      "올바른 휴대폰 번호 형식이 아닙니다. (예: 010-1234-5678)",
      "error",
    );
}

function blurValidateEmail(el) {
  const v = el.value.trim();
  if (!v) return;
  if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(v))
    showHermesToast("유효한 이메일 형식이 아닙니다.", "error");
  else if (v.length > 100)
    showHermesToast("이메일은 100자 이하로 입력해주세요.", "error");
}

/* --- 모달 --- */

function openModal(id, useFlex = false) {
  const el = document.getElementById(id);
  el.classList.remove("hidden");
  if (useFlex) el.classList.add("flex");
}
function closeModal(id, useFlex = false) {
  const el = document.getElementById(id);
  el.classList.add("hidden");
  if (useFlex) el.classList.remove("flex");
}

/* --- 주민등록번호 --- */

function validateRrnChecksum(digits) {
  const weights = [2, 3, 4, 5, 6, 7, 8, 9, 2, 3, 4, 5];
  const sum = weights.reduce(
    (acc, w, i) => acc + parseInt(digits[i], 10) * w,
    0,
  );
  return parseInt(digits[12], 10) === (11 - (sum % 11)) % 10;
}

function maskRrn(formatted) {
  const digits = formatted.replace(/\D/g, "");
  if (digits.length <= 6) return formatted;
  return (
    digits.slice(0, 6) +
    "-" +
    digits[6] +
    "*".repeat(Math.max(0, digits.length - 7))
  );
}

const HermesRrn = {
  value: "",
  eyeOpen: false,
  clear() { this.value = ""; },
};

function onRrnInput(el) {
  let digits = el.value.replace(/\D/g, "").replace(/\*/g, "");
  if (digits.length > 13) digits = digits.slice(0, 13);
  const formatted =
    digits.length > 6 ? digits.slice(0, 6) + "-" + digits.slice(6) : digits;
  HermesRrn.value = formatted;
  document.getElementById("new-rrn").value = formatted;
  el.value = formatted;
  autoFillTempPw(); /* 페이지별 함수 (employees.js / students.js) */
}

function onRrnFocus() {
  document.getElementById("new-rrn-display").value = HermesRrn.value;
}

function onRrnBlur() {
  if (!HermesRrn.eyeOpen)
    document.getElementById("new-rrn-display").value = maskRrn(HermesRrn.value);
}

function toggleRrnVisibility() {
  HermesRrn.eyeOpen = !HermesRrn.eyeOpen;
  const icon = document.getElementById("rrn-eye-icon");
  const display = document.getElementById("new-rrn-display");
  icon.className = HermesRrn.eyeOpen
    ? "fa-regular fa-eye text-sm"
    : "fa-regular fa-eye-slash text-sm";
  display.value = HermesRrn.eyeOpen ? HermesRrn.value : maskRrn(HermesRrn.value);
}

/* --- 우편번호 --- */

/**
 * 카카오 우편번호 검색 팝업을 열고 결과를 지정한 필드에 채움
 * @param {string} zipcodeId  우편번호 input ID
 * @param {string} addressId  주소 input ID
 * @param {string} detailId   상세주소 input ID (포커스 이동)
 */
function openZipCodeSearch(zipcodeId, addressId, detailId) {
  new daum.Postcode({
    oncomplete: function (data) {
      const addr =
        data.userSelectedType === "R" ? data.roadAddress : data.jibunAddress;
      document.getElementById(zipcodeId).value = data.zonecode;
      document.getElementById(addressId).value = addr;
      document.getElementById(detailId).focus();
    },
  }).open();
}

/** @deprecated openZipCodeSearch() 를 사용하세요 */
function searchZipCode() {
  openZipCodeSearch("postcode", "address", "detailAddress");
}
/** @deprecated openZipCodeSearch() 를 사용하세요 */
function searchDetailZipCode() {
  openZipCodeSearch("edit-zipcode", "edit-addr", "edit-addr-detail");
}

/* ==========================================================================
   5. 자동 초기화 (defer 보장 — DOM 파싱 완료 후 실행)
   ========================================================================== */
document
  .querySelectorAll("select.hm-input:not([data-cs-defer])")
  .forEach(function (el) {
    initCustomSelect(el);
  });
