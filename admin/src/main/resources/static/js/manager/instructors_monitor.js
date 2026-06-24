/* 강사 업무 모니터링 - 카드 필터 링크 */

(function () {
  const params = new URLSearchParams(location.search);
  const activeId = params.get('instrId') || '';

  // 카드 클릭 시 해당 instrId로 필터 (이미 선택된 카드면 전체로 복귀)
  document.querySelectorAll('#instructorCards a').forEach(function (card) {
    card.addEventListener('click', function (e) {
      const href = new URL(card.href, location.origin);
      if (activeId && href.searchParams.get('instrId') === activeId) {
        e.preventDefault();
        const reset = new URL('/admin/instructors/monitor', location.origin);
        if (params.get('keyword')) reset.searchParams.set('keyword', params.get('keyword'));
        if (params.get('fromDt'))  reset.searchParams.set('fromDt',  params.get('fromDt'));
        if (params.get('toDt'))    reset.searchParams.set('toDt',    params.get('toDt'));
        location.href = reset.toString();
      }
    });
  });
})();
