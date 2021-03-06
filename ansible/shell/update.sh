#!/usr/bin/env bash

########################################################################################################################

# CAPTURANDO LAS VARIABLES RECIBIDAS DEL COMMAND STRING EN VARIABLES DEL ARCHIVO
if [ -z $1 ]; then
  echo "faltan parametros \n"
  exit 1
else
  APP_PATH=$1
fi
echo "
APP_PATH=$1
CONFIG_HOST=$4
CONFIG_SERVICE_NAME=$5
CONFIG_PORT=$6"
cd $APP_PATH/source


source config.sh
export M2_HOME=/usr/local/apache-maven-3.6.3
export JRE_HOME=/usr/lib/jvm/jre
export M2=$M2_HOME/bin
export PATH=$M2:$PATH
export JAVA_HOME=/usr/lib/jvm/jre-1.8.0-openjdk
rm -frv /root/.m2/repository/ar/gob/recibosdesueldos/consumer/
rm -frv /root/.m2/repository/ar/gob/recibosdesueldos/logging/
rm -frv /root/.m2/repository/ar/gob/recibosdesueldos/model/
rm -frv /root/.m2/repository/ar/gob/recibosdesueldos/commons/
 ../ansible/shell/compile.sh
../ansible/shell/stopjava.sh
../ansible/shell/startjava.sh
