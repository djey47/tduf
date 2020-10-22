@ECHO OFF

PUSHD .\tools\cli
CALL SetEnv.cmd
POPD

ECHO *** Welcome to TDUF: Command Line Interface v%TDUF_VERSION% ***
ECHO.
ECHO --- Visual tools ---
DIR *Editor.cmd /B
ECHO --- (Advanced) CLI tools ---
DIR *Tool.cmd /B
ECHO --- Help and troubleshooting ---
ECHO help: this page
ECHO logs: open all log files in default editor
ECHO settings: open settings file in default editor
ECHO.
ECHO Just type and run any of these scripts to get usage details. Enjoy!
ECHO.
