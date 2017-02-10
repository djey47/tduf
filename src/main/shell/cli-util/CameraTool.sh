#!/usr/bin/env bash
cd ../..
java -cp ./tools/lib/tduf.jar fr.tduf.cli.tools.CameraTool $*
cd - >/dev/null
