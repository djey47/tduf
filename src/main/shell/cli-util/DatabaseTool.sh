#!/usr/bin/env bash
cd ../..
java -cp ./tools/lib/tduf.jar fr.tduf.cli.tools.DatabaseTool $*
cd - >/dev/null
