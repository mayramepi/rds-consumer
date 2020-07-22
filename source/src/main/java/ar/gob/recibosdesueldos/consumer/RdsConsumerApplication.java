package ar.gob.recibosdesueldos.consumer;

import ar.gob.recibosdesueldos.commons.config.CorsConfig;
import ar.gob.recibosdesueldos.commons.config.SharedConfigurationReference;
import ar.gob.recibosdesueldos.commons.config.WebSecurityConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@SpringBootApplication
@ComponentScan(basePackages = {"ar.gob.recibosdesueldos"})
@Import({CorsConfig.class, WebSecurityConfig.class, SharedConfigurationReference.class})

public class RdsConsumerApplication {

    public static void main(String[] args) {
        SpringApplication.run(RdsConsumerApplication.class, args);
    }
//    @Bean
//    public WebMvcConfigurer corsConfigurer() {
//        return new WebMvcConfigurer() {
//            @Override
//            public void addCorsMappings(CorsRegistry registry) {
//                registry
//                        .addMapping("/**")
//                        .allowedOrigins("*")
//                        .allowedHeaders("*")
//                        .allowedMethods("HEAD", "GET", "POST", "PUT", "DELETE", "PATCH")
//                        .allowCredentials(true);
//            }
//        };
//    }
}
