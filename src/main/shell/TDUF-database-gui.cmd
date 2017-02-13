@ECHO OFF

REM *** Admin mode ***
CD /D %~dp0
CALL .\tools\cli\AdminRun.cmd %0
IF "%ERRORLEVEL%" == "1" (EXIT /B)
REM *** Admin mode ***

MKDIR logs 2>NUL
CD tools\cli

ECHO ...Starting Database Editor...
CALL .\CheckJava.cmd
CALL .\SetVersion.cmd
CALL DatabaseEditor >> ..\..\logs\DatabaseEditor.log 2>>&1

IF ERRORLEVEL 1 GOTO handleEditorError

ECHO All done!
ECHO Please check DatabaseEditor.log in logs directory for details.
ECHO.
PAUSE
EXIT /B 0

:handleEditorError
ECHO Editor failed, can't continue. Changes won't be applied.
ECHO Please check DatabaseEditor.log in logs directory for details.
ECHO.
PAUSE
EXIT /B 1
