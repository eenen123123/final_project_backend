/* 학사 운영 모니터링 (원장) — 탭 전환, 진도율 추이/성적 분포/반별 평균 차트 */

function switchMonTab(tabId, btn) {
  document.querySelectorAll('.mon-tab-panel').forEach(function (p) { p.classList.add('hidden'); });
  document.querySelectorAll('.mon-tab-btn').forEach(function (b) {
    b.classList.remove('text-violet-600', 'border-violet-500', 'bg-violet-50/60');
    b.classList.add('text-slate-500', 'border-transparent');
  });
  document.getElementById(tabId).classList.remove('hidden');
  btn.classList.remove('text-slate-500', 'border-transparent');
  btn.classList.add('text-violet-600', 'border-violet-500', 'bg-violet-50/60');

  if (tabId === 'tab-progress' && progressChart) progressChart.resize();
  if (tabId === 'tab-grade') {
    if (gradeDistChart) gradeDistChart.resize();
    if (classAvgChart) classAvgChart.resize();
  }
}

/*
 * 월별 진도율 추이 라인 차트 (실데이터).
 *
 * [데이터 한계 — LAST_UPDATE 근삿값]
 * LECTURE_PROGRESS.LAST_UPDATE 는 해당 수강 레코드의 '마지막 갱신 시각'이다.
 * 학생이 동일 강의를 재수강하면 이전 달의 기록이 덮어써지므로,
 * 이 차트는 "해당 월에 마지막으로 활동한 레코드" 기준의 월별 진도율이며
 * 엄밀한 누적 스냅샷이 아니다. 그래프가 감소하거나 특정 달에 값이 없을 수 있다.
 *
 * [개선 방향]
 * 1) LECTURE_PROGRESS에 FIRST_WATCHED_DT(최초 시청일) 컬럼을 추가 →
 *    최초 완료 시점이 보존되어 월별 누적 추이를 정확히 계산할 수 있다.
 * 2) 월말 배치 스케줄러로 CLASS_PROGRESS_SNAPSHOT 테이블에 스냅샷 적재 →
 *    이력이 온전히 유지되어 어느 시점 진도율도 조회 가능하다.
 */
var _progressPalette = [
  { b: '#7c3aed', bg: 'rgba(124,58,237,0.08)' },
  { b: '#38bdf8', bg: 'rgba(56,189,248,0.08)'  },
  { b: '#34d399', bg: 'rgba(52,211,153,0.08)'  },
  { b: '#fbbf24', bg: 'rgba(251,191,36,0.08)'  },
  { b: '#f97316', bg: 'rgba(249,115,22,0.08)'  },
  { b: '#ec4899', bg: 'rgba(236,72,153,0.08)'  }
];
var _progressDs = (typeof _progressDatasets !== 'undefined' ? _progressDatasets : []).map(function (ds, i) {
  var c = _progressPalette[i % _progressPalette.length];
  return { label: ds.label, data: ds.data, borderColor: c.b, backgroundColor: c.bg,
           tension: 0.4, fill: true, borderWidth: 2, pointRadius: 4, spanGaps: true };
});
var progressChart = new Chart(
  document.getElementById('progressChart').getContext('2d'), {
  type: 'line',
  data: {
    labels: typeof _progressMonths !== 'undefined' ? _progressMonths : [],
    datasets: _progressDs
  },
  options: {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { position: 'top', labels: { font: { size: 11 }, boxWidth: 12 } } },
    scales: {
      x: { grid: { display: false }, ticks: { font: { size: 12 } } },
      y: { min: 0, max: 100, grid: { color: 'rgba(148,163,184,0.1)' }, ticks: { font: { size: 11 }, callback: function (v) { return v + '%'; } } }
    }
  }
});

/* 점수 분포 막대 차트 */
var gradeDistChart = new Chart(
  document.getElementById('gradeDistChart').getContext('2d'), {
  type: 'bar',
  data: {
    labels: ['0~39', '40~49', '50~59', '60~69', '70~79', '80~89', '90~100'],
    datasets: [{
      label: '학생 수',
      data: typeof _gradeDistData !== 'undefined' ? _gradeDistData : [0,0,0,0,0,0,0],
      backgroundColor: [
        'rgba(239,68,68,0.8)', 'rgba(249,115,22,0.8)', 'rgba(234,179,8,0.8)',
        'rgba(34,197,94,0.7)', 'rgba(124,58,237,0.8)', 'rgba(56,189,248,0.8)', 'rgba(20,184,166,0.8)'
      ],
      borderRadius: 6, borderSkipped: false
    }]
  },
  options: {
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { grid: { display: false }, ticks: { font: { size: 11 } } },
      y: { grid: { color: 'rgba(148,163,184,0.1)' }, ticks: { font: { size: 11 }, callback: function (v) { return v + '명'; } } }
    }
  }
});

/* 반별 평균 수평 막대 차트 */
var classAvgChart = new Chart(
  document.getElementById('classAvgChart').getContext('2d'), {
  type: 'bar',
  data: {
    labels: typeof _classAvgLabels !== 'undefined' ? _classAvgLabels : [],
    datasets: [{
      label: '평균 점수',
      data: typeof _classAvgData !== 'undefined' ? _classAvgData : [],
      backgroundColor: 'rgba(124,58,237,0.8)',
      borderRadius: 6, borderSkipped: false
    }]
  },
  options: {
    indexAxis: 'y',
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { min: 0, max: 100, grid: { color: 'rgba(148,163,184,0.1)' }, ticks: { font: { size: 11 }, callback: function (v) { return v + '점'; } } },
      y: { grid: { display: false }, ticks: { font: { size: 11 } } }
    }
  }
});

/* 탭 초기화 */
(function () {
  var firstBtn = document.querySelector('.mon-tab-btn[data-tab="tab-lecture"]');
  if (firstBtn) switchMonTab('tab-lecture', firstBtn);
})();
