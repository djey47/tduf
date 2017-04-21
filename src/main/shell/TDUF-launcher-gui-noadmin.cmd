@ECHO OFF

MKDIR logs 2>NUL
CD tools\cli

ECHO ...Starting Launcher...
CALL .\CheckJava.cmd
CALL .\SetVersion.cmd
CALL Launcher -v >> ..\..\logs\Launcher.log 2>>&1

ECHO All done!
ECHO Please check Launcher.log in logs directory for details.
ECHO.
PAUSE
