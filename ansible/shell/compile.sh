#!/usr/bin/env bash
source config.sh
echo "bb $BRANCH"
git pull origin $BRANCH
mvn clean install -Dmaven.test.skip=true
#">> /tmp/pull.log
sleep 10
if [ -s "$JAVA_JAR" ]
then 
   echo " file exists and is not empty "
   stopjava.sh
   startjava.sh
fi

