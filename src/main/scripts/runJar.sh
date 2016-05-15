#!/bin/bash
export SERVER_BASE=${server.base}/${project.artifactId};
export SERVER_HOME=$SERVER_BASE/${project.artifactId}-${project.version};
export LOG_DIR=${log4j.log.base}/${project.artifactId};

export JAVA_OPTS="$JAVA_OPTS -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=8000,suspend=n -Xms512M -Xmx1024M";

if [ ! -d $LOG_DIR ];
 then 
 	mkdir $LOG_DIR
fi

PID_FILE=$LOG_DIR/${project.artifactId}.pid

if test -f $PID_FILE ; then
        kill -9 `cat $PID_FILE`
fi

cd $SERVER_HOME

_jar=`ls lib | grep "..*\.jar$"`
_classpath="${_jar}"
classpath=`echo ${_classpath} | sed -e 's/ /:lib\//g'`
JAVA_OPTS="$JAVA_OPTS -classpath .:${classpath}"

java $JAVA_OPTS \
  -d64 \
  org.springframework.boot.loader.WarLauncher ${server.main} &

if test $? -ne 0 ; then
        exit 1
else
        echo $! > $PID_FILE
fi
