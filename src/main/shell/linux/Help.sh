#!/usr/bin/env bash
shopt -s expand_aliases

./tools/cli/CheckJava.sh
source ./tools/cli/SetVersion.sh

echo "*** Welcome to TDUF: Command Line Interface v$TDUF_VERSION ***"
echo
echo "--- Visual tools ---"
ls -1 *Editor.sh
echo "--- (Advanced) CLI tools ---"
ls -1 *Tool.sh
echo
echo "Just type and run any of these scripts to get usage details. Enjoy!"
echo
