#!/usr/bin/env bash

ansible-playbook -i ./inventory/hosts update.yml \
            -u root \
            -e env=dev \
            -e repository="git@ssh-repositorio-ce-asi.buenosaires.gob.ar:reingenieria-recibos-sueldo/rds-consumer.git" \
            -e folder="rds-consumer" \
            -e branch="dev" \
            -e path="/prueba/rds-consumer" \

            -vv