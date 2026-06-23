/* 학사 운영 모니터링 (원장)
   · 탭 전환, 진도율 추이/성적 분포/반별 평균 차트
   · 탭 2~4 차트는 목업 데이터 사용 */

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

/* 진도율 추이 라인 차트 (목업) */
var progressChart = new Chart(
  document.getElementById('progressChart').getContext('2d'), {
  type: 'line',
  data: {
    labels: ['3월', '4월', '5월', '6월'],
    datasets: [
      { label: '고등 수학 A', data: [10, 27, 42, 55], borderColor: '#7c3aed', backgroundColor: 'rgba(124,58,237,0.08)', tension: 0.4, fill: true, borderWidth: 2, pointRadius: 4 },
      { label: '고등 수학 B', data: [12, 33, 55, 68], borderColor: '#38bdf8', backgroundColor: 'rgba(56,189,248,0.08)', tension: 0.4, fill: true, borderWidth: 2, pointRadius: 4 },
      { label: '고등 영어', data: [15, 45, 78, 85], borderColor: '#34d399', backgroundColor: 'rgba(52,211,153,0.08)', tension: 0.4, fill: true, borderWidth: 2, pointRadius: 4 },
      { label: '중등 영어', data: [20, 55, 91, 95], borderColor: '#fbbf24', backgroundColor: 'rgba(251,191,36,0.08)', tension: 0.4, fill: true, borderWidth: 2, pointRadius: 4 }
    ]
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

/* 점수 분포 막대 차트 (목업) */
var gradeDistChart = new Chart(
  document.getElementById('gradeDistChart').getContext('2d'), {
  type: 'bar',
  data: {
    labels: ['0~39', '40~49', '50~59', '60~69', '70~79', '80~89', '90~100'],
    datasets: [{
      label: '학생 수',
      data: [5, 12, 38, 72, 98, 71, 28],
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

/* 반별 평균 수평 막대 차트 (목업) */
var classAvgChart = new Chart(
  document.getElementById('classAvgChart').getContext('2d'), {
  type: 'bar',
  data: {
    labels: ['고등수학A', '고등수학B', '중등수학A', '중등수학B', '고등영어', '중등영어'],
    datasets: [{
      label: '평균 점수',
      data: [79.4, 74.1, 72.8, 69.5, 76.3, 68.5],
      backgroundColor: 'rgba(124,58,237,0.8)',
      borderRadius: 6, borderSkipped: false
    }]
  },
  options: {
    indexAxis: 'y',
    responsive: true, maintainAspectRatio: false,
    plugins: { legend: { display: false } },
    scales: {
      x: { min: 50, max: 100, grid: { color: 'rgba(148,163,184,0.1)' }, ticks: { font: { size: 11 }, callback: function (v) { return v + '점'; } } },
      y: { grid: { display: false }, ticks: { font: { size: 11 } } }
    }
  }
});

/* 탭 초기화 */
(function () {
  var firstBtn = document.querySelector('.mon-tab-btn[data-tab="tab-lecture"]');
  if (firstBtn) switchMonTab('tab-lecture', firstBtn);
})();
