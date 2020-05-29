#!/usr/bin/env bash
mkdir logs 2>/dev/null
LOGS_PATH=./logs/Launcher.log
java -cp ./tools/lib/tduf.jar fr.tduf.gui.launcher.Launcher "$@" >> ${LOGS_PATH} 2>&1
echo
echo "Please check in logs directory for details."
echo
