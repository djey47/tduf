@ECHO OFF

CALL .\tools\cli\CheckJava.cmd
CALL .\tools\cli\SetVersion.cmd

ECHO *** Welcome to TDUF: Command Line Interface v%TDUF_VERSION% ***
ECHO.
ECHO --- Visual tools ---
DIR *Editor.cmd /B
ECHO --- Advanced tools ---
DIR *Tool.cmd /B
ECHO.
ECHO Just type and run any of these scripts to get usage details. Enjoy!
ECHO.
