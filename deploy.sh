#!/usr/bin/env bash
# 이 repo(백엔드)의 빌드·기동 스크립트.
# 상위 deploy.sh(오케스트레이터)가 이 파일을 호출.
set -euo pipefail
cd "$(dirname "$0")"

# 1) 최신 코드 반영
git pull --ff-only

# 2) .env 존재 확인 (gitignore 대상이라 인스턴스에 직접 둬야 함)
if [[ ! -f .env ]]; then
  echo "[deploy] .env 가 없습니다. 인스턴스에 .env 를 먼저 배치하세요." >&2
  exit 1
fi

# 3) 빌드 + 기동 (rest 가 이미지 1회 빌드 → admin 재사용)
DOCKER_BUILDKIT=1 docker compose up -d --build

# 4) 헬스 체크 — 컨테이너만 뜬 게 아니라 앱이 실제로 HTTP 응답하는지 확인.
#    (crash-loop 시 포트가 안 열려 connection refused → 실패로 잡힘)
healthcheck() {
  local name=$1 url=$2 tries=40   # 40 x 3s = 최대 120초 대기
  for ((i = 1; i <= tries; i++)); do
    # -f 안 씀: 401/404 같은 응답도 "앱이 살아서 응답함"으로 인정 (연결 거부만 실패)
    if curl -s -o /dev/null -m 3 "$url"; then
      echo "[deploy] $name 기동 확인 ($url)"
      return 0
    fi
    sleep 3
  done
  echo "[deploy] $name 기동 실패: ${tries}회 시도 동안 $url 응답 없음" >&2
  echo "[deploy] 로그 확인: docker compose logs --tail=80 $name" >&2
  return 1
}

fail=0
healthcheck rest  http://localhost:8081/ || fail=1
healthcheck admin http://localhost:8080/ || fail=1

if [[ $fail -ne 0 ]]; then
  echo "[deploy] 배포 실패 — 컨테이너는 떴으나 앱이 정상 기동하지 않았습니다." >&2
  docker compose ps >&2
  exit 1
fi

# 5) dangling 이미지 정리
docker image prune -f

echo "[deploy] backend 배포 완료 (rest:8081, admin:8080)"
