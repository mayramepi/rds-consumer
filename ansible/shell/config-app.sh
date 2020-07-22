#!/usr/bin/env bash

########################################################################################################################

# CAPTURANDO LAS VARIABLES RECIBIDAS DEL COMMAND STRING EN VARIABLES DEL ARCHIVO
if [ -z $1 ]; then
  echo ""
  exit 1
else
  APP_PATH=$1
fi
DATABASE_URL=$2
URL_BACK=$3
HTTPD_CONFIG=$4
HTTPD_DAEMON=$5
echo "
DOMINIO_FRONT=$3
URL_BACK=$4
HTTPD_CONFIG=$4
HTTPD_DAEMON=$5
cd $APP_PATH/source

source config.sh
startjava


########################################################################################################################

