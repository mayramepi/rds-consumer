# Configuracion 
Configurar el archivo "application.properties" con las propiedades:<br>
<strong>
app.name=@project.artifactId@<br>
app.version=@project.version@<br>

spring.profiles.active=@spring.profiles.active@<br>

spring.datasource.url=jdbc:oracle:thin:@DB_HOST/DB_SERVICE<br>
spring.datasource.username=DB_USERNAME<br>
spring.datasource.password=DB_PASSWORD<br>

spring.artemis.mode=native<br>
spring.artemis.embedded.enabled=false<br>

spring.artemis.host=${AMQ_HOST:localhost}<br>
spring.artemis.port=${AMQ_PORT:61616}<br>
spring.artemis.user=${AMQ_USER:admin}<br>
spring.artemis.password=${AMQ_PASSWORD:admin}<br>

app.out_dir=${APP_OUT_DIR:/var/rds/}<br>
app.out_dir_temp=${APP_TEM_DIR:/tmp}<br>
app.preview_dir=${APP_PREV_DIR:/prev}<br>

resources.css=${APP_TEMPLATES_DIR:/var/rds-templates}/css/style.css<br>
resources.img=${APP_TEMPLATES_DIR:/var/rds-templates}/img/<br>
resources.templates=${APP_TEMPLATES_DIR:/var/rds-templates}/templates/<br>


app.jms_concurrency=${JMS_CONCURRENCY:5}<br>

chequeoBorradoTemplates=${CHEQUO_BORRADO_TEMPLATES:0 0/15 * * * ?}<br>


spring.servlet.multipart.max-file-size=10000KB<br>
spring.servlet.multipart.max-request-size=10000KB<br>

app.retur-error-api:${RETURN_ERROR_API:false}<br>


logging.level.root=${level.root:info}<br>
logging.level.org.springframework.data=trace<br>
logging.level.org.hibernate.SQL=trace<br>
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=trace<br>

app.log.dir=${APP_LOG_DIR:-/var/rds/logs}<br>

security.jwt.token.secret-key:core-secret-key<br>
custom.chequeo-seguridad=${seguridad.chequeo:true}<br>

</strong>

<p>
Donde:
<ul>
    <li>DB_HOST: host de la base de datos Oracle</li>
    <li>DB_PORT: puerto del conexión de Oracle</li>
    <li>DB_SERVICE_NAME: nombre del servicio de Oracle </li>
    <li>DB_USERNAME: usuario de conexión de Oracle</li>
    <li>DB_PASSWORD: password de conexión de Oracle </li>
    <li>AMQ_HOST: host del Artemis MQ</li>
    <li>AMQ_PORT: puerto del Artemis MQ</li>
    <li>AMQ_USER: usuario del Artemis MQ</li>
    <li>AMQ_PASSWORD: password del Artemis MQ</li>
    <li>APP_OUT_DIR: Directorio donde se dejan los pdf generados, este debe ser accesido por todos los consumers y el modulo de uploader y ser persistente, con permisos de lectura,escritura y borrado.</li>
    <li>APP_TEM_DIR: Directorio temporal donde se van creando los pdf, no puede ser el mismo que APP_OUT_DIR,con permisos de lectura,escritura y borrado.</li>
    <li>APP_PREV_DIR: Directorio temporal donde se crea el pdf ve vista previa, no puede ser el mismo que APP_OUT_DIR,con permisos de lectura,escritura y borrado.</li>
    <li>APP_TEMPLATES_DIR: Directorio donde se guarda los archivos de templates, con permisos de lectura,escritura y borrado.</li>
    <li>JMS_CONCURRENCY: Cantidad de pdf que se toman de la cola simulteneamente</li>
    <li>CHEQUO_BORRADO_TEMPLATES: En formato de cron, aca se espesifica cada cuanto se borra el cache de los templates</li>
    <li>RETURN_ERROR_API: Si esta en true, las apis retornan el error interno cuando este se genera.</li>
    <li>APP_LOG_DIR: Directorio donde se escriben los logs en formato json para ser tomados por el agentTD para luego subirlo al elasticsearch</li>
    <li>seguridad.cheque: Si esta en false, el sistema ya no requiere que se le mante el tocken de seguridad.</li>
    

</ul>
</p>

<p>
Asi deberia quedar el archivo una vez configurado:<br>
<strong>
app.name=@project.artifactId@<br>
app.version=@project.version@<br>

spring.profiles.active=@spring.profiles.active@<br>



spring.datasource.url=jdbc:oracle:thin:@${DB_HOST:exadb.gcba.gob.ar}:${DB_PORT:1521}/${DB_SERVICE_NAME:dgisdv12.gcba.gob.ar}<br>
spring.datasource.username=${DB_USERNAME:reingenieriarecibos_dev}<br>
spring.datasource.password=${DB_PASSWORD:reing_2k19}<br>

spring.main.allow-bean-definition-overriding=true<br>
springfox.documentation.swagger.v2.path: /docs<br>


spring.artemis.mode=native<br>
spring.artemis.embedded.enabled=false<br>

spring.artemis.host=${AMQ_HOST:localhost}<br>
spring.artemis.port=${AMQ_PORT:61616}<br>
spring.artemis.user=${AMQ_USER:admin}<br>
spring.artemis.password=${AMQ_PASSWORD:admin}<br>

app.out_dir=${APP_OUT_DIR:/var/rds/}<br>
app.out_dir_temp=${APP_TEM_DIR:/tmp}<br>
app.preview_dir=${APP_PREV_DIR:/prev}<br>

resources.css=${APP_TEMPLATES_DIR:/var/rds-templates}/css/style.css<br>
resources.img=${APP_TEMPLATES_DIR:/var/rds-templates}/img/<br>
resources.templates=${APP_TEMPLATES_DIR:/var/rds-templates}/templates/<br>


app.jms_concurrency=${JMS_CONCURRENCY:5}<br>

chequeoBorradoTemplates=${CHEQUO_BORRADO_TEMPLATES:0 0/15 * * * ?}<br>


spring.servlet.multipart.max-file-size=10000KB<br>
spring.servlet.multipart.max-request-size=10000KB<br>

app.retur-error-api:${RETURN_ERROR_API:false}<br>



logging.level.root=${level.root:info}<br>
logging.level.org.springframework.data=trace<br>
logging.level.org.hibernate.SQL=trace<br>
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=trace<br>

app.log.dir=${APP_LOG_DIR:-/var/rds/logs}<br>

security.jwt.token.secret-key:core-secret-key<br>
custom.chequeo-seguridad=${seguridad.chequeo:true}<br>
</strong>
</p>

<br>
<strong style="color: red">
NOTA 1: Configurar un storage persistente montado en el directorio "/var/rds". 
Este storage persistente sera compartido con el microservicio "rds-uploader".
</strong>
<br>
<strong style="color: red">
NOTA 2: Configurar un storage persistente montado en el directorio "/var/rds-templates". 
Copiar el todo contenido del directorio "templates/plantillas" en el directorio "/var/rds-templates"
</strong>
