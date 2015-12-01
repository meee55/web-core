#!/bin/bash
export SERVER_BASE=${server.base}/${project.artifactId};
export SERVER_HOME=$SERVER_BASE/${project.artifactId}-${project.version};
export LOG_DIR=${log4j.log.base}/${project.artifactId};

PID_FILE=$LOG_DIR/${project.artifactId}.pid

if test -f $PID_FILE ; then
        kill -9 `cat $PID_FILE`
fi

echo batch server shutting down ...