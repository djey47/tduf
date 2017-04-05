#!/usr/bin/env bash
shopt -s expand_aliases

mkdir logs 2>/dev/null
cd tools/cli
source ./linux-aliases
CheckJava

echo "...Starting Launcher..."
LOGS_PATH=../../logs/Launcher.log
Launcher >> ${LOGS_PATH} 2>&1

echo "Please check Launcher.log in logs directory for details."
echo
read -p "Press ENTER to continue..."