#!/usr/bin/env bash
function prevent_codeship_timeout() {
  (
    while true; do
      echo "Preventing Codeship timeout by echoing every 300 seconds"
      sleep 300
    done
  ) &
  local pid=$!
  trap "kill ${pid}" SIGINT SIGTERM EXIT
}