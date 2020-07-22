#!/usr/bin/env bash

########################################################################################################################

# CAPTURANDO LAS VARIABLES RECIBIDAS DEL COMMAND STRING EN VARIABLES DEL ARCHIVO
if [ -z $1 ]; then
  echo ""
  exit 1
else
  APP_PATH=$1
fi

DOMINIO_FRONT=$2
URL_BACK=$3
HTTPD_CONFIG=$4
HTTPD_DAEMON=$5
echo "
DOMINIO_FRONT=$3
URL_BACK=$4
HTTPD_CONFIG=$4
HTTPD_DAEMON=$5


"

cd $APP_PATH/source

########################################################################################################################

########################################################################################################################




########################################################################################################################



########################################################################################################################




########################################################################################################################


# CONFIG_HTTPD="//opt/rh/httpd24/root/etc/httpd/conf.d/front-eval.conf"
cp ../ansible/inventory/apache.conf.dist $HTTPD_CONFIG
sed -i "s/PROJECT_ROOT/${APP_PATH//\//\\/}/g" $HTTPD_CONFIG
sed -i "s/DOMINIO_FRONT/${DOMINIO_FRONT}/g" $HTTPD_CONFIG

########################################################################################################################
##################CONFIGURANDO EL FRONT ##########################################################################################
mkdir front/assets/config
cp ../ansible/inventory/config.json.dist front/assets/config/config.json
sed -i "s/URL_BACK/${URL_BACK//\//\\/}/g" front/assets/config/config.json

#service httpd restart
#service httpd24-httpd restart
#service httpd24-httpd status -l
service $HTTPD_DAEMON restart
service $HTTPD_DAEMON status -l

########################################################################################################################

