@ECHO OFF
IF [%1]==[] (
	ECHO Missing parameter: database directory.
) ELSE (
	ECHO Unpacking TDU database from %1, please wait...
	CALL DatabaseTool unpack-all -d %1 -j %1\tduf-json >> Alpha-DatabaseEditor.log 2>>&1
	ECHO Starting Database Editor...
	CALL DatabaseEditor %1\tduf-json >> Alpha-DatabaseEditor.log 2>>&1
	ECHO Database Editor ended, now repacking database to %1, please wait...
	CALL DatabaseTool repack-all -o %1 -j %1\tduf-json >> Alpha-DatabaseEditor.log 2>>&1
	ECHO All done!
)