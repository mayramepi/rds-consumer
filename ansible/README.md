### EXPLICACION

El proceso de instalacion se ejecuta con un comando ansible-playbook desde el directorio ansible:

	ansible-playbook -i ./inventory/asi-234-establecimientos install.yml \ 
        -e @extra-vars/asi-234-establecimientos.json \
        -u {username} \
        -e {param}={value} \
        ... \
        ... \
        ... 

donde los parametros obligatorios son:

- __-u username__: es el nombre de usuario de accesso por ssh __(OBLIGATORIO)__
- __-e ansible_ssh_pass=`{password}`__: clave de acceso por ssh __(OBLIGATORIO)__
- __-e db_host=`{db_host}`__: host (ip, dominio) de la base de datos __(OBLIGATORIO)__
- __-e db_name=`{db_name}`__: nombre de la base de datos __(OBLIGATORIO)__
- __-e db_user=`{db_user}`__: usuario de la base de datos __(OBLIGATORIO)__
- __-e db_pass=`{db_pass}`__: clave de usuario de la base de datos __(OBLIGATORIO)__

Exiten otros parametros que vienen definidos por defecto, los cuales son los siguientes:

- __-e repository=`{url_repo}`__: url del repositorio git del proyecto __(DEFAULT: `git@git-asi.buenosaires.gob.ar:usuarioQA/asi-260-regularizacion-dominial.git`)__
- __-e folder=`{folder_name}`__: nombre de la carpeta en donde se realizará el clonado del repo __(DEFAULT: `asi-260-regularizacion-dominial`)__
- __-e path=`{peth/base/to/deploy}`__: ruta base donde se realizará el deploy __(DEFAULT: `/var/www/html`)__ 
- __-e branch=`{git-branch}`__: rama base para hacer el clonado __(OPCIONAL, DEFAULT: `master`)__
- __-e tag=`{x.x.x}`__: tag de la version para el deploy __(OPCIONAL, DEFAULT: TOMA ULTIMO TAG CREADO)__
- __-e db_port=`{db_port}`__: puerto de conexion a la base de datos __(OPCIONAL, DEFAULT: `3306`)__
- __-e env=`{env}`__: entornos a instalar [`qa`, `hml`, `prod`, `asi`], cuando el valor es `asi`, el deploy se impactrará en todos los ambientes __(OPCIONAL, DEFAULT: `prod`)__
- __-vvv__: muestra los detalles de los comandos de ejecucion __(OPCIONAL)__

En caso que se quiera sobreescribir algunos de estos parametros, solo debe agregarlo al commando ansible-playbook

### COMMANDO DE INSTALACIÓN

El comando recomendado para la instalcion es el siguiente:

    ansible-playbook -i ./inventory/hosts install.yml \
        -e @extra-vars/default-parameters.json \
        -u {username} \
        -e ansible_ssh_pass={password} \
        -e db_host={db_host} \
        -e db_name={db_name} \
        -e db_user={db_user} \
        -e db_pass={db_pass} \
        -e db_port={db_port} \
        -e env={env} \
        -vvv

### COMMANDO DE ACTUALIZACION

El comando recomendado para la actualizacion es el siguiente:

    ansible-playbook -i ./inventory/hosts update.yml \ 
        -e @extra-vars/default-parameters.json \
        -u {username} \
        -e ansible_ssh_pass={password} \
        -e env={env} \
        -e tag={} \
        -vvv

# NOTA IMPORTANTE: 
> En las referencias de las variables de los parametros, las llaves `{}` no son parte del parametro, ejemplo:
> -e db_port={db_port} => -e db_port=3306
