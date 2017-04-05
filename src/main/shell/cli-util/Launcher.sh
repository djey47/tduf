#!/usr/bin/env bash
cd ../..
java -cp ./tools/lib/tduf.jar fr.tduf.gui.launcher.Launcher $*
cd - >/dev/null
