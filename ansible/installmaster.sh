#!/usr/bin/env bash
#   -e @extra-vars/default-parameters.json \
ansible-playbook -i ./inventory/hosts.dist install.yml \
            -u root \
            -e env=dev \
            -e repository="git@ssh-repositorio-ce-asi.buenosaires.gob.ar:reingenieria-recibos-sueldo/rds-consumer.git" \
            -e folder="rds-consumer" \
            -e branch="dev" \
            -e path="/prueba" \
            -e db_user="APPRECIBOS_DEMO" \
            -e db_pass="123456" \
            -e db_host="10.9.10.22" \
            -e db-servicename="estab.gcba.gob.ar" \
            -e db-port="1521"
            -vv

-u root -e env=dev  -e repository="git@ssh-repositorio-ce-asi.buenosaires.gob.ar:reingenieria-recibos-sueldo/rds-consumer.git"  -e folder="rds-consumer"  -e branch="dev"  -e path="/prueba/rds-consumer"  -e db_user="APPRECIBOS_DEMO"  -e db_pass="123456" -e db_host="10.9.10.22" -e db-servicename="estab.gcba.gob.ar" -e db-port="1521"