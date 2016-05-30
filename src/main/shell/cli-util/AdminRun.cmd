REM Parameter: name of cmd script to be run as administartor
REM EXIT code 0 => Admin rights already got
REM EXIT code 1 => Admin rights to be acquired, cmd script has been reloaded with UAC

@ECHO OFF

REM Is admin?
NET FILE 1>NUL 2>NUL
IF "%ERRORLEVEL%" == "0" ( GOTO gotPrivileges )

REM Invoking UAC for Privilege Escalation
SETLOCAL DisableDelayedExpansion
SET "batchPath=%1"
SETLOCAL EnableDelayedExpansion
ECHO Set UAC = CreateObject^("Shell.Application"^) > "%temp%\OEgetPrivileges.vbs"
ECHO UAC.ShellExecute "!batchPath!", "ELEV", "", "runas", 1 >> "%temp%\OEgetPrivileges.vbs"
CALL "%temp%\OEgetPrivileges.vbs"
EXIT /B 1

REM Having admin rights, already
:gotPrivileges
SETLOCAL
PUSHD .
EXIT /B 0
