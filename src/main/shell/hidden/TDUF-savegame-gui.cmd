REM *** Admin mode ***
CD /D %~dp0
CALL .\tools\cli\AdminRun.cmd %~0
IF "%ERRORLEVEL%" == "1" (EXIT /B)
REM *** Admin mode ***

MKDIR logs 2>NUL
CD tools\cli

ECHO ...Starting SaveGame Editor...
CALL .\CheckJava.cmd
CALL .\SetVersion.cmd
CALL SaveGameEditor >> ..\..\logs\SaveGameEditor.log 2>>&1

if ERRORLEVEL 1 GOTO handleEditorError

ECHO All done!
ECHO.
PAUSE
EXIT /B 0

:handleEditorError
ECHO Editor failed, can't continue. Changes won't be applied.
ECHO Please check SaveGameEditor.log in logs directory for details.
ECHO.
PAUSE
EXIT /B 1