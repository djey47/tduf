@ECHO OFF

SET DATABASE_EDITOR_LOGS=.\logs\DatabaseEditor.log

CALL :view_logs %DATABASE_EDITOR_LOGS%

EXIT /B 0

:view_logs
IF EXIST "%1" (
  %1
) ELSE (
  ECHO No logs: %1
)
EXIT /B 0