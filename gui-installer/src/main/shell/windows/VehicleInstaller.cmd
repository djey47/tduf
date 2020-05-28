@ECHO OFF

MKDIR logs 2>NUL
PUSHD tools\cli 2>NUL

ECHO ...Starting Vehicle Installer...
CALL .\SetEnv.cmd

POPD 2>NUL

java -cp .\tools\lib\tduf-gui-installer-all.jar fr.tduf.gui.installer.Installer %* >> .\logs\VehicleInstaller.log 2>>&1

ECHO Please check in logs directory for details.
ECHO.
PAUSE
EXIT /B 0