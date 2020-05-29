@ECHO OFF

MKDIR logs 2>NUL
PUSHD tools\cli 2>NUL

ECHO ...Starting Database Editor...
CALL .\SetEnv.cmd

POPD 2>NUL

java -cp .\tools\lib\tduf.jar fr.tduf.gui.database.DatabaseEditor %* >> .\logs\VehicleInstaller.log 2>>&1

ECHO Please check in logs directory for details.
ECHO.
PAUSE
EXIT /B 0