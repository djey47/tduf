@ECHO OFF
REM SET VERSION TO LOAD RIGHT JARS
SET /P TDUF_VERSION=<.\lib\version.info
ECHO Welcome to TDUF: Command Line Interface v%TDUF_VERSION%.
ECHO.
ECHO Available modules :
DIR *Tool.cmd /B
ECHO DatabaseEditor.cmd
ECHO Version.cmd
ECHO.
ECHO Just type and run any of these scripts to get usage details. Enjoy!