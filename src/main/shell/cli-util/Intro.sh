#!/usr/bin/env bash
shopt -s expand_aliases

clear
source ./linux-aliases
CheckJava
SetVersion

echo "*** Welcome to TDUF: Command Line Interface v$TDUF_VERSION ***"
echo
ls Intro.sh
echo "--- Visual tools ---"
ls -1 *Editor.sh
echo "--- Advanced tools ---"
ls -1 *Tool.sh
echo
echo "Just type and run any of these scripts to get usage details. Enjoy!"
echo
