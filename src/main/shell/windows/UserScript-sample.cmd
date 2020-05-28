@ECHO OFF

REM TDUF USER SCRIPT SAMPLE
ECHO *** Initializing... ***
ECHO.

PUSHD .\tools\cli
CALL SetEnv.cmd
POPD

ECHO *** User script will run with TDUF v%TDUF_VERSION% ***
ECHO.

REM Your commands below...
REM examples: CALL [DatabaseTool <arguments>, FileTool <arguments>, ...]
REM check CLI reference at https://github.com/djey47/tduf/wiki/CLI



REM Let these instructions at end of file to see what's been going on.
ECHO.
PAUSE
