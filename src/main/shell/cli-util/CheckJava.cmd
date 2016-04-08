@ECHO OFF

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