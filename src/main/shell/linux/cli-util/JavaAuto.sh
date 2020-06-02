#!/usr/bin/env bash

# Will find appropriate JRE (embedded or system)
# Must be run from TDUF root directory

# Default is system-wide
JAVA_EXECUTABLE=java

# Lookup in tools/jre/bin/java
JAVA_EMBEDDED=./tools/jre/bin/java
JAVA_EMBEDDED_INIT=../tools/jre/bin/java
if [ -f $JAVA_EMBEDDED ]; then
  echo "(i) Will use embedded Java runtime: $JAVA_EMBEDDED"  
  JAVA_EXECUTABLE=$JAVA_EMBEDDED
elif [ -f $JAVA_EMBEDDED_INIT ]; then
  echo "(i) Will use embedded Java runtime: $JAVA_EMBEDDED_INIT"  
  JAVA_EXECUTABLE=$JAVA_EMBEDDED_INIT
else
  echo "(i) Will use system-wide Java runtime"    
fi

$JAVA_EXECUTABLE "$@"
