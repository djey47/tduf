#!/usr/bin/env bash
SETTINGS_FILE=~/.tduf/tduf.properties

edit_settings () {
  if [ -f "$1" ]; then
    xdg-open "$1" || vi "$1"
  else
    echo "No settings: $1"
  fi
}

edit_settings $SETTINGS_FILE
