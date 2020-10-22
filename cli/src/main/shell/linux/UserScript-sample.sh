#!/usr/bin/env bash
shopt -s expand_aliases && source ./tools/cli/linux-aliases

# TDUF USER SCRIPT SAMPLE
echo "*** Initializing... ***"
echo

cd ./tools/cli
source ./SetEnv.sh
cd - >/dev/null

echo "*** User script will run with TDUF v${TDUF_VERSION} ***"
echo

# Your commands below...
# examples: DatabaseTool <arguments>, FileTool <arguments>
# check CLI reference at https://github.com/djey47/tduf/wiki/CLI


