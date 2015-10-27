@ECHO OFF

MKDIR logs 2>NUL
CD cli 2>NUL

CALL .\CheckJava.cmd
CALL .\SetVersion.cmd

java -cp ..\lib\tduf-gui-database-all-%TDUF_VERSION%.jar fr.tduf.gui.database.DatabaseEditor %* > ..\logs\Alpha-DatabaseEditor.log 2>>&1