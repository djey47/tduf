@ECHO OFF

MKDIR logs 2>NUL
CD cli 2>NUL

CALL .\CheckJava.cmd
CALL .\SetVersion.cmd

ECHO *** Welcome to TDUF: Command Line Interface v%TDUF_VERSION% ***
ECHO.
ECHO Available modules :
DIR *.cmd /B
ECHO.
ECHO Just type and run any of these scripts to get usage details. Enjoy!
ECHO.