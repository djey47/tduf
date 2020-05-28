#!/usr/bin/env bash

# 1-Java check
java -version

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

# 2-TDUF version as env var
TDUF_VERSION=`cat ../lib/version.info`
