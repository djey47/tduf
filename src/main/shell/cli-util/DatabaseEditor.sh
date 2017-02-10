#!/usr/bin/env bash
cd ../..
java -cp ./tools/lib/tduf.jar fr.tduf.gui.database.DatabaseEditor $*
cd - >/dev/null
