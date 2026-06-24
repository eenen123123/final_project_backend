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

# 4) dangling 이미지 정리
docker image prune -f

echo "[deploy] backend 배포 완료 (rest:8081, admin:8080)"
