#!/usr/bin/env bash
echo "bb $BRANCH"
git pull origin $BRANCH
mvn clean install -Dmaven.test.skip=true
#">> /tmp/pull.log
sleep 10
