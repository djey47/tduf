@ECHO OFF
REM SET VERSION TO LOAD RIGHT JARS
SET /P TDUF_VERSION=<.\lib\version.info

java -version
ECHO.
IF %ERRORLEVEL% EQU 0 (
	ECHO.
) ELSE (
	ECHO ! Java does not seem to be installed properly. Please refer to README file for details.
	ECHO.
	PAUSE
)

ECHO *** Welcome to TDUF: Command Line Interface v%TDUF_VERSION% ***
ECHO.
ECHO Available modules :
DIR *Tool.cmd /B
ECHO Alpha-DatabaseEditor.cmd
ECHO DatabaseEditor.cmd
ECHO.
ECHO Just type and run any of these scripts to get usage details. Enjoy!
ECHO.