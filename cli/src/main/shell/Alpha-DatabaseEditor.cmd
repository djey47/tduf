@ECHO OFF
IF [%1]==[] (
	ECHO Missing parameter: database directory.
	EXIT 1
) ELSE (
	CALL DatabaseTool unpack-all -d %1 -j %1\tduf-json
	CALL DatabaseEditor %1\tduf-json
	CALL DatabaseTool repack-all -o %1 -j %1\tduf-json
)