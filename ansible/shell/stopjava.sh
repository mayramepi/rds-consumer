#!/usr/bin/env bash

source ./config.sh
if [ -s "nohup.out" ]
then
       
	tail -f nohup.out &
fi
input="/tmp/process.pid"
if [ -s "$input" ]
then
	while IFS= read -r line
	do
  		kill $line
	done < "$input"
	rm -f $input
fi
echo "$JAVA_PORT del config"
PID=`netstat -lptn|grep "::$JAVA_PORT "|awk '{print $7}'|cut -d '/' -f1`
if [ "$PID" != "" ]
 then
  echo "killing proceso $PID/$JAVA_PORT  "
  kill  $PID
fi

killall tail
