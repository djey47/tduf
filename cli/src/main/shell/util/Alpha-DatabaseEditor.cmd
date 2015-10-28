@ECHO OFF
IF [%1]==[] (
	ECHO Missing parameter: database directory.
	ECHO.

	setlocal DisableDelayedExpansion
	set "batchPath=%~0"
	setlocal EnableDelayedExpansion

	SET "script=%temp%\OEgetDatabaseDir.vbs"
	ECHO Dim directory > %script%
	ECHO directory = InputBox^("This tool will unpack and convert TDU database for you. When finished, whole database will be repacked." ^& vbCrLf ^& "Please enter TDU Database Directory for BNK files:", "TDUF Database Editor ALPHA", "C:\Programs\Atari\Test Drive Unlimited"^) >> %script%
	ECHO If ^(directory = ""^) Then WScript.Quit >> %script%
	ECHO Set shell = CreateObject ^("WScript.Shell"^) >> %script%
	ECHO shell.run "!batchPath!" ^& " " ^& directory >> %script%
	CALL %script%
) ELSE (
	ECHO.
	ECHO ...Unpacking TDU database from %1, please wait...
	CALL DatabaseTool unpack-all -d %1 -j %1\tduf-json > ..\logs\Alpha-DatabaseEditor.log 2>>&1
	if ERRORLEVEL 1 GOTO handleUnpackError
	
	ECHO ...Starting Database Editor...
	CALL DatabaseEditor %1\tduf-json >> ..\logs\Alpha-DatabaseEditor.log 2>>&1
	if ERRORLEVEL 1 GOTO handleEditorError

	ECHO ...Database Editor ended, now repacking database to %1, please wait...
	CALL DatabaseTool repack-all -o %1 -j %1\tduf-json >> ..\logs\Alpha-DatabaseEditor.log 2>>&1
	if ERRORLEVEL 1 GOTO handleRepackError

	ECHO.
	ECHO All done!
	PAUSE	
)
GOTO exitSuccess

:handleUnpackError
ECHO Unpacking failed, can't continue.
GOTO exitFailure

:handleEditorError
ECHO Editor failed, can't continue. Changes won't be applied.
GOTO exitFailure

:handleRepackError
ECHO Repacking failed. Changes won't be applied.
GOTO exitFailure

:exitSuccess
EXIT /B 0

:exitFailure
ECHO Please check Alpha-DatabaseEditor.log for details.
ECHO.
PAUSE
EXIT /B 1