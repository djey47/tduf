#!/usr/bin/env bash
shopt -s expand_aliases

# TDUF USER SCRIPT SAMPLE
echo "*** Initializing... ***"
echo
cd ../tools/cli
chmod +x *.sh
source ./linux-aliases
CheckJava
SetVersion
echo "*** User script will run with TDUF v${TDUF_VERSION} ***"
echo

# Your commands below...
# examples: DatabaseTool <arguments> FileTool.sh <arguments>
# check CLI reference at https://github.com/djey47/tdu-cp/wiki/Tools-reference



# Let this instruction at end of file, please.
echo
read -p "Press ENTER to continue..."
