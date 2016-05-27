@echo off

SET START_DIR=%~dp0

:checkPrivileges
NET FILE 1>NUL 2>NUL
if '%errorlevel%' == '0' ( goto gotPrivileges ) else ( goto getPrivileges )

:getPrivileges
if '%1'=='ELEV' (shift & goto gotPrivileges)

REM Invoking UAC for Privilege Escalation

setlocal DisableDelayedExpansion
set "batchPath=%~0"
setlocal EnableDelayedExpansion
ECHO Set UAC = CreateObject^("Shell.Application"^) > "%temp%\OEgetPrivileges.vbs"
ECHO UAC.ShellExecute "!batchPath!", "ELEV", "", "runas", 1 >> "%temp%\OEgetPrivileges.vbs"
"%temp%\OEgetPrivileges.vbs"
exit /B

:gotPrivileges

REM Running Admin shell

setlocal & pushd .

CD /D %START_DIR%

MKDIR logs 2>NUL
CD tools\cli

CALL .\CheckJava.cmd
CALL .\SetVersion.cmd

ECHO ...Starting Database Editor...
CALL DatabaseEditor %1 >> ..\..\logs\DatabaseEditor.log 2>>&1
if ERRORLEVEL 1 GOTO handleEditorError

ECHO.
ECHO All done!
GOTO exitSuccess

:handleEditorError
ECHO Editor failed, can't continue. Changes won't be applied.
GOTO exitFailure

:exitSuccess
PAUSE
EXIT /B 0

:exitFailure
ECHO Please check DatabaseEditor.log in logs directory for details.
ECHO.
PAUSE
EXIT /B 1
