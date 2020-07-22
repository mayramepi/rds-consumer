#!/usr/bin/env bash
#   -e @extra-vars/default-parameters.json \
ansible-playbook -i ./inventory/hosts install.yml \
            -u root \
            -e dominio_front="front-evaluacion.testr7.dev.gcba.gob.ar" \
            -e url_back="https://10.9.5.169" \
            -e env=dev \
            -e repository="git@git-asi.buenosaires.gob.ar:usuarioQA/asi-344-eval_desem_front.git" \
            -e folder="asi-344-eval_desem_front" \
            -e branch="development" \
            -e path="/var/www/html/disco50/java" \
            -e httpd_config="/etc/httpd/conf.d/front-evaluacion.conf" \
            -e httpd_daemon="httpd" \
            -v

