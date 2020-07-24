#!/usr/bin/env bash

########################################################################################################################

# CAPTURANDO LAS VARIABLES RECIBIDAS DEL COMMAND STRING EN VARIABLES DEL ARCHIVO
if [ -z $1 ]; then
  echo ""
  exit 1
else
  APP_PATH=$1
fi
CONFIG_USER=$2
CONFIG_PASS=$3
CONFIG_HOST=$4
CONFIG_SERVICE_NAME=$5
CONFIG_PORT=$6
echo "
APP_PATH=$1
CONFIG_USER=$2
CONFIG_HOST=$4
CONFIG_SERVICE_NAME=$5
CONFIG_PORT=$6"
cd $APP_PATH/source


cp ../ansible/inventory/config.sh config.sh
sed -i "s/CONFIG_USER/${CONFIG_USER//\//\\/}/g" config.sh
sed -i "s/CONFIG_PASS/${CONFIG_PASS//\//\\/}/g" config.sh
sed -i "s/CONFIG_HOST/${CONFIG_HOST//\//\\/}/g" config.sh
sed -i "s/CONFIG_SERVICE_NAME/${CONFIG_SERVICE_NAME//\//\\/}/g" config.sh
sed -i "s/CONFIG_PORT/${CONFIG_PORT//\//\\/}/g" config..sh

source config.sh
echo "compilando..."
 ../ansible/shell/compile.sh
../ansible/shell/startjava.sh




########################################################################################################################

