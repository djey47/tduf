#!/usr/bin/env bash
shopt -s expand_aliases

cd ./tools/cli
source ./SetEnv.sh
cd - >/dev/null

echo "*** Welcome to TDUF: Command Line Interface v$TDUF_VERSION ***"
echo
echo "--- Visual tools ---"
ls -1 *Editor.sh
echo "Launcher.sh"
echo "--- (Advanced) CLI tools ---"
ls -1 *Tool.sh
echo
echo "Just type and run any of these scripts to get usage details. Enjoy!"
echo
