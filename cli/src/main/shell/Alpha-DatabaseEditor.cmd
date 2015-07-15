@ECHO OFF
IF [%1]==[] (
	ECHO Missing parameter: database directory.
) ELSE (
	CALL DatabaseTool unpack-all -d %1 -j %1\tduf-json >> Alpha-DatabaseEditor.log 2>>&1
	CALL DatabaseEditor %1\tduf-json >> Alpha-DatabaseEditor.log 2>>&1
	CALL DatabaseTool repack-all -o %1 -j %1\tduf-json >> Alpha-DatabaseEditor.log 2>>&1
)