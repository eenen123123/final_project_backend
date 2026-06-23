/* 서비스 품질 관리 (원장 · 강사별 Q&A 처리율 모니터링)
   · 서버 데이터(qaStatsData)는 quality.html 에서 window 전역으로 주입
   · Chart.js 4.x stacked bar 차트 */

function openNotePopup(instrUserId, instrUserNm) {
  var subject = encodeURIComponent('[Q&A 미처리 안내] ' + instrUserNm + ' 강사');
  window.open(
    '/admin/note/write?replyTo=' + instrUserId + '&subject=' + subject,
    '_blank',
    'width=1000,height=600'
  );
}

function filterTable(instrUserId) {
  document.querySelectorAll('tbody tr[data-instructor-id]').forEach(function (tr) {
    tr.style.display = (!instrUserId || tr.dataset.instructorId === instrUserId) ? '' : 'none';
  });
}

(function () {
  var stats      = window.qaStatsData || [];
  var labels     = stats.map(function (s) { return s.instrUserNm; });
  var answered   = stats.map(function (s) { return s.answeredCnt; });
  var unanswered = stats.map(function (s) { return s.unansweredCnt; });

  var canvas = document.getElementById('qaRateChart');
  if (!canvas) return;

  new Chart(canvas.getContext('2d'), {
    type: 'bar',
    data: {
      labels: labels,
      datasets: [
        {
          label: '답변 완료',
          data: answered,
          backgroundColor: 'rgba(124,58,237,0.85)',
          borderRadius: 5,
          borderSkipped: false
        },
        {
          label: '미처리',
          data: unanswered,
          backgroundColor: 'rgba(251,113,133,0.75)',
          borderRadius: 5,
          borderSkipped: false
        }
      ]
    },
    options: {
      responsive: true,
      maintainAspectRatio: false,
      plugins: {
        legend: { position: 'top', labels: { font: { size: 11 }, boxWidth: 12 } },
        tooltip: { callbacks: { label: function (ctx) { return ' ' + ctx.dataset.label + ': ' + ctx.parsed.y + '건'; } } }
      },
      scales: {
        x: { stacked: true, grid: { display: false }, ticks: { font: { size: 12 } } },
        y: { stacked: true, grid: { color: 'rgba(148,163,184,0.1)' }, ticks: { font: { size: 11 }, callback: function (v) { return v + '건'; } } }
      }
    }
  });
})();
