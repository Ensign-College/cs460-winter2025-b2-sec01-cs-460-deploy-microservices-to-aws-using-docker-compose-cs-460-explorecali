#!/usr/bin/env bash
set -euo pipefail

# ──────────────────────────────────────────────────────────────────────────────
# Environment selection: dev | prod  (default: dev)
# You can run: SMOKE_ENV=prod bash api_smoke.sh
# ──────────────────────────────────────────────────────────────────────────────
SMOKE_ENV="${SMOKE_ENV:-dev}"
case "$SMOKE_ENV" in
  dev)  ENV_FILE=".smoke.dev" ;;
  prod) ENV_FILE=".smoke.prod" ;;
  *)    echo "Unknown SMOKE_ENV='$SMOKE_ENV' (use 'dev' or 'prod')"; exit 1 ;;
esac

# Load environment file if present (recommended), else fall back to sensible defaults.
if [[ -f "$ENV_FILE" ]]; then
  set -a
  # shellcheck disable=SC1090
  source "$ENV_FILE"
  set +a
else
  echo "[$SMOKE_ENV] ENV file '$ENV_FILE' not found. Using built-in defaults."
fi

# ──────────────────────────────────────────────────────────────────────────────
# Defaults (can be overridden by env file or CLI, e.g., BASE_URL=...)
# ──────────────────────────────────────────────────────────────────────────────
# Prefer BASE_URL if provided; otherwise compute from host/port by environment.
PORT="${PORT:-8080}"

if [[ "${SMOKE_ENV}" == "prod" ]]; then
  PUBLIC_HOST="${PUBLIC_HOST:-54.162.92.234}"
else
  # Codespaces terminal: talk to the service directly on localhost.
  PUBLIC_HOST="${PUBLIC_HOST:-localhost}"
fi

BASE_URL="${BASE_URL:-http://${PUBLIC_HOST}:${PORT}}"

# Optional: auth from env with sane defaults
USER_USER="${USER_USER:-user}"
USER_PASS="${USER_PASS:-password}"
ADMIN_USER="${ADMIN_USER:-admin}"
ADMIN_PASS="${ADMIN_PASS:-admin123}"

echo "Using SMOKE_ENV=${SMOKE_ENV}"
echo "Using BASE_URL=${BASE_URL}"

# ──────────────────────────────────────────────────────────────────────────────
# Styling helpers
# ──────────────────────────────────────────────────────────────────────────────
GREEN="\033[32m"; RED="\033[31m"; CYAN="\033[36m"; BOLD="\033[1m"; DIM="\033[2m"; RESET="\033[0m"
hr() { echo -e "${DIM}--------------------------------------------------------------------------------${RESET}"; }

# run_test METHOD PATH EXPECTED_CODES BODY AUTH_KIND
# AUTH_KIND in: none | user | admin
run_test() {
  local method="$1"
  local path="$2"
  local expected_codes="$3" # e.g. "200" or "200|201|204"
  local body="${4:-}"
  local auth="${5:-none}"

  local url="${BASE_URL}${path}"
  local auth_flag=()
  case "$auth" in
    user)  auth_flag=(-u "${USER_USER}:${USER_PASS}") ;;
    admin) auth_flag=(-u "${ADMIN_USER}:${ADMIN_PASS}") ;;
    none)  auth_flag=() ;;
    *)     echo "Unknown auth '$auth'"; exit 1 ;;
  esac

  local method_flag=(-X "$method")
  local content_flag=()
  if [[ -n "$body" ]]; then
    content_flag=(-H "Content-Type: application/json" -d "$body")
  fi

  hr
  echo -e "${BOLD}${CYAN}TEST:${RESET} $method $path   ${DIM}(auth: $auth | expect: $expected_codes)${RESET}"
  hr

  # Single request: print full -i output for screenshots
  response="$(curl -s -i "${auth_flag[@]}" "${method_flag[@]}" "${content_flag[@]}" "$url")"
  echo "$response"

  # Parse HTTP status from first line: "HTTP/1.1 200 OK"
  status="$(printf '%s\n' "$response" | head -n1 | awk '{print $2}')"

  if [[ "$status" =~ ^($expected_codes)$ ]]; then
    echo -e "${GREEN}PASS${RESET}  expected=$expected_codes  actual=$status"
  else
    echo -e "${RED}FAIL${RESET}  expected=$expected_codes  actual=$status"
  fi
  echo
}

main() {
  # ---- Packages ----
  run_test POST "/packages" "201|200|204" '{"code":"BB","name":"Beekeepers you Betcha"}' none
  run_test POST "/packages" "403"        '{"code":"BB","name":"Beekeepers you Betcha"}' user
  run_test POST "/packages" "201|200|204" '{"code":"BB","name":"Beekeepers you Betcha"}' admin

  run_test GET "/packages" "401" "" none
  run_test GET "/packages" "200" "" user
  run_test GET "/packages" "200" "" admin

  run_test DELETE "/packages/BB" "401" "" none
  run_test DELETE "/packages/BB" "403" "" user
  run_test DELETE "/packages/BB" "204|200|404" "" admin

  # ---- Recommendations ----
  run_test GET "/recommendations/top/5" "401" "" none
  run_test GET "/recommendations/top/5" "200" "" user
  run_test GET "/recommendations/top/5" "200" "" admin

  run_test GET "/recommendations/customer/5?limit=5" "401" "" none
  run_test GET "/recommendations/customer/5?limit=5" "200" "" user
  run_test GET "/recommendations/customer/5?limit=5" "200" "" admin

  # ---- Tour Ratings ----
  TOUR_ID=999
  CUSTOMER_ID=1000
  RATING_BODY='{"score":3,"comment":"comment","customerId":1000}'
  BATCH_BODY='[123,456]'

  run_test POST "/tours/${TOUR_ID}/ratings" "401" "$RATING_BODY" none
  run_test POST "/tours/${TOUR_ID}/ratings" "403" "$RATING_BODY" user
  run_test POST "/tours/${TOUR_ID}/ratings" "201|200" "$RATING_BODY" admin

  run_test DELETE "/tours/${TOUR_ID}/ratings/${CUSTOMER_ID}" "401" "" none
  run_test DELETE "/tours/${TOUR_ID}/ratings/${CUSTOMER_ID}" "403" "" user
  run_test DELETE "/tours/${TOUR_ID}/ratings/${CUSTOMER_ID}" "204|200|404" "" admin

  run_test GET "/tours/${TOUR_ID}/ratings" "401" "" none
  run_test GET "/tours/${TOUR_ID}/ratings" "200|404" "" user
  run_test GET "/tours/${TOUR_ID}/ratings" "200|404" "" admin

  run_test GET "/tours/${TOUR_ID}/ratings/average" "401" "" none
  run_test GET "/tours/${TOUR_ID}/ratings/average" "200|404" "" user
  run_test GET "/tours/${TOUR_ID}/ratings/average" "200|404" "" admin

  run_test PATCH "/tours/${TOUR_ID}/ratings" "401" "$RATING_BODY" none
  run_test PATCH "/tours/${TOUR_ID}/ratings" "403" "$RATING_BODY" user
  run_test PATCH "/tours/${TOUR_ID}/ratings" "200" "$RATING_BODY" admin
  # Example minimal partial:
  # run_test PATCH "/tours/${TOUR_ID}/ratings" "200" '{"comment":"updated comment"}' admin

  run_test PUT "/tours/${TOUR_ID}/ratings" "401" "$RATING_BODY" none
  run_test PUT "/tours/${TOUR_ID}/ratings" "403" "$RATING_BODY" user
  run_test PUT "/tours/${TOUR_ID}/ratings" "200" "$RATING_BODY" admin

  run_test POST "/tours/${TOUR_ID}/ratings/batch?score=3" "401" "$BATCH_BODY" none
  run_test POST "/tours/${TOUR_ID}/ratings/batch?score=3" "403" "$BATCH_BODY" user
  run_test POST "/tours/${TOUR_ID}/ratings/batch?score=3" "200|201" "$BATCH_BODY" admin
}

main "$@"
