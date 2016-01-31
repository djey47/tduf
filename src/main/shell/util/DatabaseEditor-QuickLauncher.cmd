@ECHO OFF
IF [%1]==[] ( GOTO askForParam ) ELSE ( GOTO startGui )

:askForParam
ECHO Missing parameter: database directory.
ECHO.
SETLOCAL DisableDelayedExpansion
SET "batchPath=%~0"
SETLOCAL EnableDelayedExpansion
SET "script=%temp%\OEgetDatabaseDir.vbs"
ECHO Dim directory > %script%
ECHO directory = InputBox^("This tool will unpack and convert TDU database for you. When finished, whole database will be repacked." ^& vbCrLf ^& "Please enter TDU Database Directory for BNK files:", "TDUF Database Editor: Quick Launcher", "C:\Programs\Atari\Test Drive Unlimited"^) >> %script%
ECHO If ^(directory = ""^) Then WScript.Quit >> %script%
ECHO Set shell = CreateObject ^("WScript.Shell"^) >> %script%
ECHO shell.run "!batchPath!" ^& " " ^& directory >> %script%
CALL %script%
GOTO exitSuccess

:startGui
ECHO *** TDUF DATABASE EDITOR QUICK LAUNCHER ***
ECHO.
ECHO ...Unpacking TDU database from %1, please wait...
CALL DatabaseTool unpack-all -d %1 -j %1\tduf-json > ..\logs\DatabaseEditor.log 2>>&1
if ERRORLEVEL 1 GOTO handleUnpackError
	
ECHO ...Starting Database Editor...
CALL DatabaseEditor %1\tduf-json >> ..\logs\DatabaseEditor.log 2>>&1
if ERRORLEVEL 1 GOTO handleEditorError

ECHO ...Database Editor ended, now repacking database to %1, please wait...
CALL DatabaseTool repack-all -o %1 -j %1\tduf-json >> ..\logs\DatabaseEditor.log 2>>&1
if ERRORLEVEL 1 GOTO handleRepackError

ECHO.
ECHO All done!
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
PAUSE
EXIT /B 0

:exitFailure
ECHO Please check DatabaseEditor.log in logs directory for details.
ECHO.
PAUSE
EXIT /B 1