#!/bin/bash
#
# Author: Raju Uppalapati (ruppalapati@netflix.com)
#

HEAP="${HEAP:=500m}"
NEWGEN_RATIO="${NEWGEN_RATIO:=3}"

PRANA_HOME="${PRANA_HOME:=/apps/prana}"
PRANA_EXT_LIB="${PRANA_EXT_LIB:=$PRANA_HOME/ext}"
PRANA_LIB=$PRANA_HOME/lib
PRANA_LOGS=$PRANA_HOME/logs

EXT_CLASSPATH=$(JARS=("$PRANA_EXT_LIB"/*.jar); IFS=:; echo "${JARS[*]}")
CLASSPATH=$EXT_CLASSPATH:$(JARS=("$PRANA_LIB"/*.jar); IFS=:; echo "${JARS[*]}")
if [ "$CLASSPATH_OVERRIDE" == "true" ]; then
    echo "Overriding classpath"
    CLASSPATH=$EXT_CLASSPATH
fi

JAVA_OPTS="-verbose:sizes \
-Xloggc:$PRANA_LOGS/gc.log \
-Xmx$HEAP \
-Xms$HEAP \
-XX:NewRatio=$NEWGEN_RATIO \
-XX:MaxPermSize=75m \
-XX:+OptimizeStringConcat \
-XX:+UseParNewGC \
-XX:+UseConcMarkSweepGC \
-XX:+CMSConcurrentMTEnabled \
-XX:+CMSScavengeBeforeRemark \
-XX:+CMSClassUnloadingEnabled \
-XX:CMSInitiatingOccupancyFraction=75 \
-XX:+UseCMSInitiatingOccupancyOnly \
-XX:+PrintGCDateStamps \
-XX:+PrintGCDetails \
-XX:+ExplicitGCInvokesConcurrent \
-XX:+HeapDumpOnOutOfMemoryError \
-XX:HeapDumpPath=$PRANA_LOGS/ \
-XX:OnOutOfMemoryError=$PRANA_HOME/bin/stop.sh \
-XX:+TraceClassUnloading \
-XX:-UseGCOverheadLimit \
-Djsse.enableSNIExtension=false \
-Dfile.encoding=UTF-8 \
-Dcom.sun.management.jmxremote.port=8076 \
-Dcom.sun.management.jmxremote.authenticate=false \
-Dcom.sun.management.jmxremote.ssl=false"

echo "$JAVA_HOME/bin/java" ${JAVA_OPTS[@]} ${PRANA_OPTS} -classpath "$CLASSPATH" com.netflix.prana.Main "$@"
exec "$JAVA_HOME/bin/java" ${JAVA_OPTS[@]} ${PRANA_OPTS} -classpath "$CLASSPATH" com.netflix.prana.Main "$@"
