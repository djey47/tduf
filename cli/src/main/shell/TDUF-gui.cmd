@ECHO OFF

MKDIR logs 2>NUL
CD cli 2>NUL

CALL .\CheckJava.cmd
CALL .\SetVersion.cmd
CALL .\DatabaseEditor.cmd %* > ..\logs\Alpha-DatabaseEditor.log 2>>&1