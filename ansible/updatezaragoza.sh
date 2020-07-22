#!/usr/bin/env bash

ansible-playbook -i ./inventory/hosts update.yml \
            -u root \
            -e env=dev \
            -e repository="git@git-asi.buenosaires.gob.ar:usuarioQA/asi-344-eval_desem_front.git" \
            -e folder="asi-344-eval_desem_front-zaragoza" \
            -e branch="master" \
            -e tag="00.03.00-RC2" \
            -e path="/var/www/html/disco50/java" \
            -v
