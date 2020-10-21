@ECHO OFF

REM Will find appropriate JRE (embedded or system)

REM Default is system-wide
SET JAVA_EXECUTABLE=java
SET EMBEDDED=false

REM Lookup in tools\jre\bin\java from many source locations
SET JAVA_EMBEDDED=.\tools\jre\bin\java.exe
SET JAVA_EMBEDDED_ALT=..\tools\jre\bin\java.exe
SET JAVA_EMBEDDED_ALT2=..\jre\bin\java.exe
IF EXIST "%JAVA_EMBEDDED%" (
  SET EMBEDDED=true
  SET JAVA_EXECUTABLE=%JAVA_EMBEDDED%
) ELSE (
  IF EXIST "%JAVA_EMBEDDED_ALT%" (
	SET JAVA_EXECUTABLE=%JAVA_EMBEDDED_ALT%
    SET EMBEDDED=true
  ) ELSE (
    IF EXIST "%JAVA_EMBEDDED_ALT2%" (
	  SET JAVA_EXECUTABLE=%JAVA_EMBEDDED_ALT2%
      SET EMBEDDED=true
    )
  )
)

IF "%EMBEDDED%" == "true" (
  ECHO "(i) Will use embedded Java runtime: %JAVA_EXECUTABLE%"
) ELSE (
  ECHO "(i) Will use system-wide Java runtime"
)

%JAVA_EXECUTABLE% %*