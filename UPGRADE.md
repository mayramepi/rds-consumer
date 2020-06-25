# Configuracion 
Configurar el archivo "application.properties" con las propiedades:<br>
<strong>
app.name: rds-consumer<br>
app.version: 00.00.01-RC1<br>

spring.profiles.active=openshift<br>

spring.datasource.url=jdbc:oracle:thin:@DB_HOST/DB_SERVICE<br>
spring.datasource.username=DB_USERNAME<br>
spring.datasource.password=DB_PASSWORD<br>

spring.artemis.mode=native<br>
spring.artemis.embedded.enabled=false<br>

spring.artemis.host=AMQ_HOST<br>
spring.artemis.port=AMQ_PORT<br>
spring.artemis.user=AMQ_USER<br>
spring.artemis.password=AMQ_PASSWORD<br>

app.out_dir=/var/rds/<br>
app.out_dir_temp=/tmp<br>

app.marca.agua.gcba=true<br>
app.marca.agua.ivc=true<br>
app.marca.agua.pdc=true<br>
app.marca.agua.boberos=false<br>
app.marca.agua.issp=false<br>

app.jms_concurrency=6<br>

logging.level.root=info<br>
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
</ul>
</p>

<p>
Asi deberia quedar el archivo una vez configurado:<br>
<strong>
app.name: rds-security<br>
app.version: 00.00.01-RC1<br>

spring.profiles.active=openshift<br>

spring.datasource.url=jdbc:oracle:thin:@exadb.gcba.gob.ar:1521/dgisdv12.gcba.gob.ar<br>
spring.datasource.username=reingenieriarecibos<br>
spring.datasource.password=reing_2k19<br>

spring.artemis.mode=native<br>
spring.artemis.embedded.enabled=false<br>

spring.artemis.host=ex-aao-hdls-svc<br>
spring.artemis.port=61616<br>
spring.artemis.user=admin<br>
spring.artemis.password=admin<br>

app.out_dir=/var/rds/<br>
app.out_dir_temp=/tmp<br>

app.marca.agua.gcba=true<br>
app.marca.agua.ivc=true<br>
app.marca.agua.pdc=true<br>
app.marca.agua.boberos=false<br>
app.marca.agua.issp=false<br>

app.jms_concurrency=6<br>

logging.level.root=info<br>
</strong>
</p>

<strong style="color: red">
NOTA: Adicionalmente se debe configurar un storage persistente montado en el directorio "/var/rds". 
Este storage persistente sera compartido con el microservicio "rds-uploader".
</strong>