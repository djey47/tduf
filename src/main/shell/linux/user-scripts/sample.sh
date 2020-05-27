#!/usr/bin/env bash
shopt -s expand_aliases

# TDUF USER SCRIPT SAMPLE
echo "*** Initializing... ***"
echo
cd ..
source ./tools/cli/linux-aliases
CheckJava
SetVersion
echo "*** User script will run with TDUF v${TDUF_VERSION} ***"
echo
cd -

# Your commands below...
# examples: DatabaseTool <arguments>, FileTool <arguments>
# check CLI reference at https://github.com/djey47/tduf/wiki/CLI

