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

mkdir -p "$SHARED_DIR" "$SHARED_DIR/uploads" "$RELEASES_DIR"

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
      (cd "$CURRENT_DIR" && docker compose -f "$prev_compose" up -d --build --force-recreate) || true
    fi
  else
    log WARN "Rollback skipped: previous current target not found"
  fi
  exit "$exit_code"
}

trap rollback EXIT

log INFO "Step 1/5: Switching current symlink"
log INFO "Target release: $RELEASE_DIR"
ln -sfn "$RELEASE_DIR" "$CURRENT_DIR"
log INFO "Current now points to: $(readlink "$CURRENT_DIR")"

log INFO "Step 2/5: Linking env file into current"
if [[ -f "$ENV_FILE" ]]; then
  ln -sfn "$ENV_FILE" "$CURRENT_DIR/.env"
  log INFO "Env linked: $CURRENT_DIR/.env -> $ENV_FILE"
else
  die "Env file not found: $ENV_FILE"
fi

log INFO "Step 3/5: Deploying with docker compose"
cd "$CURRENT_DIR"
docker compose -f "$COMPOSE_FILE_REL" up -d --build --force-recreate

log INFO "Step 4/5: Health check"
log INFO "Waiting ${HEALTHCHECK_WAIT_SECONDS}s before checking: $HEALTHCHECK_URL"
sleep "$HEALTHCHECK_WAIT_SECONDS"
if curl -fsS --max-time 5 "$HEALTHCHECK_URL" >/dev/null; then
  log INFO "Health check passed"
else
  die "Health check failed: $HEALTHCHECK_URL"
fi

log INFO "Step 5/5: Cleaning old releases (keep=$KEEP_RELEASES)"
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
