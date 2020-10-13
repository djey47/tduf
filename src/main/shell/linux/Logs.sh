#!/usr/bin/env bash
DATABASE_EDITOR_LOGS=./logs/DatabaseEditor.log

view_logs () {
  if [ -f "$1" ]; then
    xdg-open "$1" || less "$1"
  else
    echo "No logs: $1"
  fi
}

view_logs $DATABASE_EDITOR_LOGS
