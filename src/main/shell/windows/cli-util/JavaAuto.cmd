@ECHO OFF

REM Will find appropriate JRE (embedded or system)
REM Must be run from TDUF root directory

REM Default is system-wide
SET JAVA_EXECUTABLE=java

REM Lookup in tools\jre\bin\java
SET JAVA_EMBEDDED=.\tools\jre\bin\java
SET JAVA_EMBEDDED_INIT=..\tools\jre\bin\java
IF EXIST "%JAVA_EMBEDDED%" (
  ECHO "(i) Will use embedded Java runtime: %JAVA_EMBEDDED%"
  SET JAVA_EXECUTABLE=%JAVA_EMBEDDED%
) ELSE (
  IF EXIST "%JAVA_EMBEDDED_INIT%" (
    ECHO "(i) Will use embedded Java runtime: %JAVA_EMBEDDED_INIT%"
	SET JAVA_EXECUTABLE=%JAVA_EMBEDDED_INIT%
  ) ELSE ( 
    ECHO "(i) Will use system-wide Java runtime"
  )
)    

%JAVA_EXECUTABLE% %*