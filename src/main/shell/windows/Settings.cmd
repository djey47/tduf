@ECHO OFF

SET SETTINGS_FILE=%userprofile%\.tduf\tduf.properties

CALL :edit_settings %SETTINGS_FILE%

EXIT /B 0

:edit_settings
IF EXIST "%1" (
  %1
) ELSE (
  ECHO No settings: %1
)
EXIT /B 0