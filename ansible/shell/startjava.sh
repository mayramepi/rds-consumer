#!/usr/bin/bash

source ./config.sh
sleep 5
while [ "$1" != "" ]; do
    echo "Parameter 1 equals $1"
    echo "You now have $# positional parameters"
    case "$1" in
      '-flyway')
        echo "Running con flyway"
        declare -x FLYWAY=true
        declare -x MIGRATE=true
        ;;
      '-disableAd')
        echo "Running no ad Check"
        declare -x CHECK_AD=false
        ;;
      '-port')
        echo "Setting java port"
        declare -x PORT="$2"
        ;;
       '-debug-port')
       echo "Setting debug port $2"
       DEBUG_PORT="$2"
       ;;  
      '-debug')
	echo "Running debug...."
	JAVA_DEBUG=" -Xdebug -Xrunjdwp:server=y,transport=dt_socket,address=$DEBUG_PORT,suspend=n"
        ;;
      '-stats')
        echo "Running remote stats"
	JAVA_STATS="-Dcom.sun.management.jmxremote.port=$STATS_REMOTE_PORT -Dcom.sun.management.jmxremote.ssl=false -Dcom.sun.management.jmxremote.authenticate=false  -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=$STATS_ADDRESS"
        ;;
    esac

    # Shift all the parameters down by one
    shift

done
RES=`lsof -i -P -n | grep LISTEN|grep ':$JAVA_PORT '|wc -l`

if [ $RES -gt 0 ]
then
    echo "el puerto esta usado"
    exit;
fi
echo "port $PORT $JAVA_PORT"
CMD="$JAVA_BIN -jar $DSERVER_PARAMS $JAVA_DEBUG $JAVA_STATS $JAVA_JAR $JAVA_PARAMS"
echo $CMD
# ejecuto el jar con los parametros acumulados
nohup $CMD&
#nohup $CMD>/dev/null 2>&1 &
PID=$!
#sleep 10
#tail -f nohup.out &             
while [ ! -f "nohup.out" ];
do
      sleep 5
      tail -f nohup.out &
done


