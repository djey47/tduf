#!/usr/bin/env bash
mkdir logs 2>/dev/null
LOGS_PATH=./logs/DatabaseEditor.log

cd tools/cli
source ./SetEnv.sh
cd - >/dev/null

echo "...Starting Database Editor..."

jre -cp ./tools/lib/tduf.jar fr.tduf.gui.database.DatabaseEditor "$@" >> ${LOGS_PATH} 2>&1
echo
echo "Please check in logs directory for details."
echo

