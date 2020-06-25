package ar.gob.recibosdesueldos.consumer;

import ar.gob.recibosdesueldos.commons.config.CorsConfig;
import ar.gob.recibosdesueldos.commons.config.WebSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@ComponentScan(basePackages = {"ar.gob.recibosdesueldos"})
@Import({CorsConfig.class, WebSecurityConfig.class})
public class RdsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RdsConsumerApplication.class, args);
    }

}
