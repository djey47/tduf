@ECHO OFF

REM *** Admin mode ***
CD /D %~dp0
CALL .\tools\cli\AdminRun.cmd %0
IF "%ERRORLEVEL%" == "1" (EXIT /B)
REM *** Admin mode ***

ECHO ...Starting Database Editor...
CALL .\SetEnv.cmd

POPD 2>NUL

java -cp .\tools\lib\tduf.jar fr.tduf.gui.database.DatabaseEditor %* >> .\logs\VehicleInstaller.log 2>>&1

ECHO Please check in logs directory for details.
ECHO.
PAUSE
EXIT /B 0