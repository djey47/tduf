#!/usr/bin/env bash

# 1-CLI aliases
shopt -s expand_aliases && source ./linux-aliases

# 2-Java check
./JavaAuto.sh -version

if [ $? -eq 0 ]
then
	echo
else
    echo
	echo "! Java does not seem to be installed properly. Please refer to README file for details."
    echo
    read -p "Press ENTER to continue..."
    exit 1
fi

# 3-TDUF version as env var
TDUF_VERSION=`cat ../lib/version.info`


