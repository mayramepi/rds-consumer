package ar.gob.recibosdesueldos.consumer.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.FileTemplateResolver;

@Configuration
@EnableWebMvc
public class ThymeleafConfig implements WebMvcConfigurer {

	@Value("${resources.templates}")
	private String prefix;

    @Bean
    public FileTemplateResolver yourTemplateResolver() {
    	FileTemplateResolver configurer = new FileTemplateResolver();
        configurer.setPrefix(prefix);
        configurer.setSuffix(".html");
        configurer.setTemplateMode(TemplateMode.HTML);
        configurer.setCharacterEncoding("UTF-8");
        configurer.setOrder(0);
        configurer.setCheckExistence(true);
        return configurer;
    }

    // si se usa EnableWebMvc, hay que poner esto: (sino el swagger no funciona)
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        registry.addResourceHandler("swagger-ui.html")
                .addResourceLocations("classpath:/META-INF/resources/");

        registry.addResourceHandler("/webjars/**")
                .addResourceLocations("classpath:/META-INF/resources/webjars/");

    }

}
