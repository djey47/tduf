@ECHO OFF

REM *** Admin mode ***
CD /D %~dp0
CALL .\tools\cli\AdminRun.cmd %0
IF "%ERRORLEVEL%" == "1" (EXIT /B)
REM *** Admin mode ***

MKDIR logs 2>NUL
CD tools\cli

ECHO ...Starting Launcher...
CALL .\CheckJava.cmd
CALL .\SetVersion.cmd
CALL Launcher -v >> ..\..\logs\Launcher.log 2>>&1

ECHO All done!
ECHO Please check Launcher.log in logs directory for details.
ECHO.
PAUSE
