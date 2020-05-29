@ECHO OFF

MKDIR logs 2>NUL

java -cp .\tools\lib\tduf.jar fr.tduf.gui.launcher.Launcher %* >> .\logs\Launcher.log 2>>&1

ECHO Please check in logs directory for details.
ECHO.
PAUSE
EXIT /B 0