#!/usr/bin/env bash

# Will find appropriate JRE (embedded or system)
# Must be run from TDUF root directory

# Default is system-wide
JAVA_EXECUTABLE=java
EMBEDDED=false

# Lookup in tools/jre/bin/java from many source locations
JAVA_EMBEDDED=./tools/jre/bin/java
JAVA_EMBEDDED_ALT1=../tools/jre/bin/java
JAVA_EMBEDDED_ALT2=../jre/bin/java
if [ -f $JAVA_EMBEDDED ]; then
  EMBEDDED=true
  JAVA_EXECUTABLE=$JAVA_EMBEDDED
elif [ -f $JAVA_EMBEDDED_ALT1 ]; then
  EMBEDDED=true
  JAVA_EXECUTABLE=$JAVA_EMBEDDED_ALT1
elif [ -f $JAVA_EMBEDDED_ALT2 ]; then
  EMBEDDED=true
  JAVA_EXECUTABLE=$JAVA_EMBEDDED_ALT2
fi

if [ $EMBEDDED == "true" ]; then
  echo "(i) Will use embedded Java runtime: $JAVA_EXECUTABLE"
else
  echo "(i) Will use system-wide Java runtime"
fi

$JAVA_EXECUTABLE "$@"
