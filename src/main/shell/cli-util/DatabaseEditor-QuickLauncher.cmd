@ECHO OFF
ECHO ...Starting Database Editor...
CALL DatabaseEditor %1 >> ..\logs\DatabaseEditor.log 2>>&1
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