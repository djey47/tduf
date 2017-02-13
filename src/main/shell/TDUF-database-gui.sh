#!/usr/bin/env bash
shopt -s expand_aliases

mkdir logs 2>/dev/null
cd tools/cli
source ./linux-aliases
CheckJava

echo "...Starting Database Editor..."
LOGS_PATH=../../logs/DatabaseEditor.log
DatabaseEditor >> ${LOGS_PATH} 2>&1

if [ $? -eq 0 ]
then
    echo "All done!"
    echo "Please check DatabaseEditor.log in logs directory for details."
    echo
    read -p "Press ENTER to continue..."
else
    echo "Editor failed, can't continue. Changes won't be applied."
    echo "Please check DatabaseEditor.log in logs directory for details."
    echo
    read -p "Press ENTER to continue..."
    exit 1
fi
