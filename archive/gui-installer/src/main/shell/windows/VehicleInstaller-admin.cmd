@ECHO OFF

REM *** Admin mode ***
CD /D %~dp0
CALL .\tools\cli\AdminRun.cmd %0
IF "%ERRORLEVEL%" == "1" (EXIT /B)
REM *** Admin mode ***

MKDIR logs 2>NUL
PUSHD tools\cli 2>NUL

ECHO ...Starting Vehicle Installer...
CALL .\SetEnv.cmd

POPD 2>NUL

java -cp .\tools\lib\tduf.jar fr.tduf.gui.installer.Installer %* >> .\logs\TDUF-Installer.log 2>>&1

ECHO Please check in logs directory for details.
ECHO.
PAUSE
EXIT /B 0