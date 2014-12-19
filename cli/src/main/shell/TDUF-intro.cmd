@ECHO OFF
REM SET VERSION TO LOAD RIGHT JARS
SET /P TDUF_VERSION=<.\lib\version.info
ECHO Welcome to TDUF: Command Line Interface v%TDUF_VERSION%.
ECHO.
ECHO Available modules :
ECHO - DatabaseTool
ECHO - MappingTool
ECHO.
ECHO Enjoy!