#!/usr/bin/env bash
set -euo pipefail

# Simple one-click deploy for local Docker Compose
# - Ensures Docker/Compose available
# - Ensures .env exists (copies from .env.example if missing)
# - Brings up (or manages) stack defined in docker-compose.yml

PROJECT_ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
cd "$PROJECT_ROOT_DIR"

BLUE="\033[34m"; GREEN="\033[32m"; YELLOW="\033[33m"; RED="\033[31m"; NC="\033[0m"

say() { printf "%b\n" "$1"; }
info() { say "${BLUE}[INFO]${NC} $1"; }
ok() { say "${GREEN}[OK]${NC} $1"; }
warn() { say "${YELLOW}[WARN]${NC} $1"; }
err() { say "${RED}[ERR]${NC} $1"; }

usage() {
  cat <<EOF
Usage: ./deploy.sh [command]

Commands:
  up         Build and start all services (default)
  down       Stop and remove containers
  restart    Rebuild and restart services
  status     Show running services
  logs       Tail compose logs (Ctrl+C to exit)
  clean      Down and prune volumes/orphans (DANGEROUS)
  help       Show this help

Environment:
  - Reads .env at project root for DB/LLM credentials
  - If .env is missing, copies from .env.example

Examples:
  ./deploy.sh            # one-click up
  ./deploy.sh restart    # rebuild and restart
  ./deploy.sh logs       # view logs
EOF
}

# Detect docker compose
DC="docker compose"
if ! docker compose version >/dev/null 2>&1; then
  if command -v docker-compose >/dev/null 2>&1; then
    DC="docker-compose"
  else
    err "Docker Compose not found. Please install Docker Desktop or docker-compose."
    exit 1
  fi
fi

require_docker() {
  if ! command -v docker >/dev/null 2>&1; then
    err "Docker is not installed. Please install Docker Desktop."
    exit 1
  fi
  if ! docker info >/dev/null 2>&1; then
    err "Docker daemon not running. Start Docker Desktop and retry."
    exit 1
  fi
}

ensure_env() {
  if [ -f .env ]; then
    ok ".env found"
  else
    if [ -f .env.example ]; then
      cp .env.example .env
      warn ".env not found. Created from .env.example. Please review credentials if needed."
    else
      warn ".env and .env.example not found. Proceeding with docker-compose defaults."
    fi
  fi
}

open_urls() {
  local os
  os="$(uname -s)"
  info "Endpoints:"
  say "  Frontend: http://localhost:5173"
  say "  Backend:  http://localhost:8080"
  if [ "$os" = "Darwin" ]; then
    # fire-and-forget; ignore failures
    (open "http://localhost:5173" >/dev/null 2>&1 || true) &
    (open "http://localhost:8080" >/dev/null 2>&1 || true) &
  fi
}

cmd_up() {
  require_docker
  ensure_env
  info "Building and starting services..."
  $DC up -d --build
  ok "Services are up"
  $DC ps
  open_urls
}

cmd_down() {
  require_docker
  info "Stopping and removing containers..."
  $DC down
  ok "Stack is down"
}

cmd_restart() {
  require_docker
  info "Rebuilding and restarting services..."
  $DC down
  $DC up -d --build
  ok "Services restarted"
  $DC ps
  open_urls
}

cmd_status() {
  require_docker
  $DC ps
}

cmd_logs() {
  require_docker
  info "Tailing logs (Ctrl+C to stop)..."
  $DC logs -f --tail=200
}

cmd_clean() {
  require_docker
  warn "This will remove containers and volumes. Continue? [y/N]"
  read -r ans
  if [ "${ans:-N}" = "y" ] || [ "${ans:-N}" = "Y" ]; then
    $DC down -v --remove-orphans
    docker volume prune -f || true
    ok "Cleaned up"
  else
    info "Aborted."
  fi
}

main() {
  local cmd
  cmd="${1:-up}"
  case "$cmd" in
    up) cmd_up ;;
    down) cmd_down ;;
    restart) cmd_restart ;;
    status|ps) cmd_status ;;
    logs) cmd_logs ;;
    clean) cmd_clean ;;
    help|-h|--help) usage ;;
    *) err "Unknown command: $cmd"; usage; exit 1 ;;
  esac
}

main "$@"
