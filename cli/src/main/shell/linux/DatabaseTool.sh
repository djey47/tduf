#!/usr/bin/env bash
shopt -s expand_aliases && source ./tools/cli/linux-aliases

jre -cp ./tools/lib/tduf.jar fr.tduf.cli.tools.DatabaseTool "$@"
