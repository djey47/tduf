@ECHO OFF

REM 1-Java check
java -version
ECHO.
IF %ERRORLEVEL% EQU 0 (
	ECHO.
) ELSE (
	ECHO ! Java does not seem to be installed properly. Please refer to README file for details.
	ECHO.
	PAUSE
	EXIT 1
)

REM 2-TDUF version as env var
SET /P TDUF_VERSION=<..\lib\version.info