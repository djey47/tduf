@ECHO OFF
IF [%1]==[] (
	ECHO Missing parameter: database directory.
) ELSE (
	ECHO.
	ECHO ...Unpacking TDU database from %1, please wait...
	CALL DatabaseTool unpack-all -d %1 -j %1\tduf-json > ..\logs\Alpha-DatabaseEditor.log 2>>&1
	if ERRORLEVEL 1 GOTO handleUnpackError
	
	ECHO ...Starting Database Editor...
	CALL DatabaseEditor %1\tduf-json > ..\logs\Alpha-DatabaseEditor.log 2>>&1
	if ERRORLEVEL 1 GOTO handleEditorError

	ECHO ...Database Editor ended, now repacking database to %1, please wait...
	CALL DatabaseTool repack-all -o %1 -j %1\tduf-json > ..\logs\Alpha-DatabaseEditor.log 2>>&1
	if ERRORLEVEL 1 GOTO handleRepackError

	ECHO.
	ECHO All done!	
)
EXIT /B 0

:handleUnpackError
ECHO Unpacking failed, can't continue.
GOTO exitFailure

:handleEditorError
ECHO Editor failed, can't continue. Changes won't be applied.
GOTO exitFailure

:handleRepackError
ECHO Repacking failed. Changes won't be applied.
GOTO exitFailure

:exitFailure
ECHO Please check Alpha-DatabaseEditor.log for details.
EXIT /B 1