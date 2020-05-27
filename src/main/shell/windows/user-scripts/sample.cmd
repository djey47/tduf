@ECHO OFF

REM TDUF USER SCRIPT SAMPLE
ECHO *** Initializing... ***
ECHO.
CD ..\tools\cli
CALL .\CheckJava.cmd
CALL .\SetVersion.cmd
ECHO *** User script will run with TDUF v%TDUF_VERSION% ***
ECHO.

REM Your commands below...
REM examples: CALL [DatabaseTool <arguments>, FileTool <arguments>, ...]
REM check CLI reference at https://github.com/djey47/tdu-cp/wiki/Tools-reference

CALL MappingTool

REM Let these instructions at end of file to see what's been going on.
ECHO.
PAUSE
