#!/bin/bash
#
# Author: Raju Uppalapati (ruppalapati@netflix.com)
#
if [ -e /etc/profile.d/netflix_environment.sh ]; then
   source /etc/profile.d/netflix_environment.sh
fi

JAVA_HOME="${JAVA_HOME:=/apps/java}"
pids=`$JAVA_HOME/bin/jps | grep Main | sed 's/^ *//;' | cut -d' ' -f 1 | xargs`

if [ "x${pids}" != "x" ]; then
   kill -9 ${pids}
fi
