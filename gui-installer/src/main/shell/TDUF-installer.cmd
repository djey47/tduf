@echo off

SET START_DIR=%~dp0

:checkPrivileges
NET FILE 1>NUL 2>NUL
if '%errorlevel%' == '0' ( goto gotPrivileges ) else ( goto getPrivileges )

:getPrivileges
if '%1'=='ELEV' (shift & goto gotPrivileges)

REM Invoking UAC for Privilege Escalation

setlocal DisableDelayedExpansion
set "batchPath=%~0"
setlocal EnableDelayedExpansion
ECHO Set UAC = CreateObject^("Shell.Application"^) > "%temp%\OEgetPrivileges.vbs"
ECHO UAC.ShellExecute "!batchPath!", "ELEV", "", "runas", 1 >> "%temp%\OEgetPrivileges.vbs"
"%temp%\OEgetPrivileges.vbs"
exit /B

:gotPrivileges

REM Running Admin shell

setlocal & pushd .

CD /D %START_DIR%

REM SET VERSION TO LOAD RIGHT JARS
SET /P TDUF_VERSION=<.\lib\version.info

java -cp lib\tduf-gui-installer-all-%TDUF_VERSION%.jar fr.tduf.gui.installer.Installer %* >> Alpha-Installer.log 2>>&1