#!/bin/bash

set -euo pipefail

log() {
  local level="$1"; shift
  printf '[%s] [%s] %s\n' "$(date '+%Y-%m-%d %H:%M:%S')" "$level" "$*"
}

die() {
  log ERROR "$*"
  exit 1
}

RELEASE_ID="${1:-}"
if [[ -z "$RELEASE_ID" ]]; then
  die "Usage: $0 <release_id>"
fi

DEPLOY_BASE_DIR="${DEPLOY_BASE_DIR:-/var/www/smart-dining}"
RELEASES_DIR="$DEPLOY_BASE_DIR/releases"
RELEASE_DIR="$RELEASES_DIR/$RELEASE_ID"
CURRENT_DIR="$DEPLOY_BASE_DIR/current"
SHARED_DIR="$DEPLOY_BASE_DIR/shared"

ENV_FILE="${ENV_FILE:-$SHARED_DIR/.env.prod}"
COMPOSE_FILE_REL="${COMPOSE_FILE_REL:-docker-compose.prod.yml}"

HEALTHCHECK_URL="${HEALTHCHECK_URL:-http://localhost:8081/api/health}"
HEALTHCHECK_WAIT_SECONDS="${HEALTHCHECK_WAIT_SECONDS:-10}"
KEEP_RELEASES="${KEEP_RELEASES:-5}"

remove_container_if_exists() {
  local name="$1"
  if docker ps -a --format '{{.Names}}' | grep -Fxq "$name"; then
    log WARN "Removing conflicting container: $name"
    docker rm -f "$name" >/dev/null
  fi
}

compose_down_quietly() {
  local compose_file="$1"
  if [[ -f "$compose_file" ]]; then
    docker compose -f "$compose_file" down --remove-orphans >/dev/null 2>&1 || true
  fi
}

compose_up() {
  local compose_file="$1"
  docker compose -f "$compose_file" up -d --build --force-recreate --remove-orphans
}

if [[ ! -d "$RELEASE_DIR" ]]; then
  die "Release directory not found: $RELEASE_DIR"
fi

if [[ ! -f "$RELEASE_DIR/$COMPOSE_FILE_REL" ]]; then
  if [[ "$COMPOSE_FILE_REL" == "docker-compose.prod.yml" && -f "$RELEASE_DIR/docker-compose.yml" ]]; then
    log WARN "Compose file not found: $RELEASE_DIR/$COMPOSE_FILE_REL, falling back to docker-compose.yml"
    COMPOSE_FILE_REL="docker-compose.yml"
  else
    die "Compose file not found: $RELEASE_DIR/$COMPOSE_FILE_REL"
  fi
fi

mkdir -p "$SHARED_DIR" "$SHARED_DIR/uploads" "$SHARED_DIR/img_data" "$RELEASES_DIR"

PREV_RELEASE_DIR=""
if [[ -L "$CURRENT_DIR" ]]; then
  PREV_RELEASE_DIR="$(readlink "$CURRENT_DIR" || true)"
elif [[ -e "$CURRENT_DIR" ]]; then
  die "Current path exists but is not a symlink: $CURRENT_DIR"
fi

rollback() {
  local exit_code="$?"
  if [[ $exit_code -eq 0 ]]; then
    return 0
  fi

  log ERROR "Deploy failed (exit=$exit_code). Starting rollback."

  if [[ -n "$PREV_RELEASE_DIR" && -d "$PREV_RELEASE_DIR" ]]; then
    log INFO "Rollback: switching current back to $PREV_RELEASE_DIR"
    ln -sfn "$PREV_RELEASE_DIR" "$CURRENT_DIR"
    if [[ -f "$ENV_FILE" ]]; then
      ln -sfn "$ENV_FILE" "$CURRENT_DIR/.env"
    fi
    local prev_compose="docker-compose.prod.yml"
    if [[ ! -f "$CURRENT_DIR/$prev_compose" && -f "$CURRENT_DIR/docker-compose.yml" ]]; then
      prev_compose="docker-compose.yml"
    fi
    if [[ -f "$CURRENT_DIR/$prev_compose" ]]; then
      log INFO "Rollback: restarting stack"
      compose_down_quietly "$CURRENT_DIR/$prev_compose"
      remove_container_if_exists "sky-redis"
      remove_container_if_exists "sky-takeout-app"
      (cd "$CURRENT_DIR" && compose_up "$prev_compose") || true
    fi
  else
    log WARN "Rollback skipped: previous current target not found"
  fi
  exit "$exit_code"
}

trap rollback EXIT

log INFO "Step 1/6: Switching current symlink"
log INFO "Target release: $RELEASE_DIR"
ln -sfn "$RELEASE_DIR" "$CURRENT_DIR"
log INFO "Current now points to: $(readlink "$CURRENT_DIR")"

log INFO "Step 2/6: Linking env file into current"
if [[ -f "$ENV_FILE" ]]; then
  ln -sfn "$ENV_FILE" "$CURRENT_DIR/.env"
  log INFO "Env linked: $CURRENT_DIR/.env -> $ENV_FILE"
else
  die "Env file not found: $ENV_FILE"
fi

log INFO "Step 3/6: Deploying with docker compose"
cd "$CURRENT_DIR"

log INFO "Cleaning existing stack (containers only, keep volumes)"
compose_down_quietly "$CURRENT_DIR/$COMPOSE_FILE_REL"

remove_container_if_exists "sky-redis"
remove_container_if_exists "sky-takeout-app"

compose_up "$COMPOSE_FILE_REL"

log INFO "Step 4/6: Syncing seed images to upload directory"
SEED_IMG_DIR="$SHARED_DIR/img_data"
if [[ -n "${UPLOAD_PATH_HOST:-}" && -d "$SEED_IMG_DIR" ]]; then
  TARGET_IMG_DIR="$UPLOAD_PATH_HOST"
  mkdir -p "$TARGET_IMG_DIR"
  # 使用 cp -n 避免覆盖服务器上新上传的用户图片
  copied=$(cp -nv "$SEED_IMG_DIR"/* "$TARGET_IMG_DIR"/ 2>/dev/null | wc -l | tr -d ' ')
  total=$(ls -1 "$SEED_IMG_DIR"/ 2>/dev/null | wc -l | tr -d ' ')
  log INFO "Seed images: ${copied:-0} new copied, ${total} total in seed, target=$TARGET_IMG_DIR"
elif [[ -z "${UPLOAD_PATH_HOST:-}" ]]; then
  log WARN "UPLOAD_PATH_HOST not set, skipping seed image sync"
else
  log WARN "Seed image directory not found: $SEED_IMG_DIR"
fi

log INFO "Step 5/6: Health check"
log INFO "Waiting ${HEALTHCHECK_WAIT_SECONDS}s before checking: $HEALTHCHECK_URL"
sleep "$HEALTHCHECK_WAIT_SECONDS"
if curl -fsS --max-time 5 "$HEALTHCHECK_URL" >/dev/null; then
  log INFO "Health check passed"
else
  die "Health check failed: $HEALTHCHECK_URL"
fi

log INFO "Step 6/6: Cleaning old releases (keep=$KEEP_RELEASES)"
cd "$RELEASES_DIR"

mapfile -t releases < <(ls -1dt -- */ 2>/dev/null | sed 's:/$::')

if (( ${#releases[@]} <= KEEP_RELEASES )); then
  log INFO "Nothing to clean. Total releases: ${#releases[@]}"
else
  for old in "${releases[@]:$KEEP_RELEASES}"; do
    if [[ "$RELEASES_DIR/$old" == "$RELEASE_DIR" ]]; then
      continue
    fi
    log INFO "Removing old release: $RELEASES_DIR/$old"
    rm -rf "$RELEASES_DIR/$old"
  done
fi

docker image prune -f || true
log INFO "Deploy completed successfully"

trap - EXIT
