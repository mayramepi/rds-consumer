package ar.gob.recibosdesueldos.consumer.config;

import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.client.LinkDiscoverer;
import org.springframework.hateoas.client.LinkDiscoverers;
import org.springframework.hateoas.mediatype.collectionjson.CollectionJsonLinkDiscoverer;
import org.springframework.http.ResponseEntity;
import org.springframework.plugin.core.SimplePluginRegistry;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.*;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spi.service.contexts.SecurityContext;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.awt.print.Pageable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author Pablo Wilczek
 * @date 26/02/2019
 **/

@Configuration
@EnableSwagger2
@Import(springfox.bean.validators.configuration.BeanValidatorPluginsConfiguration.class)
public class SwaggerConfig {
	@Value("${app.version:sin version}")
	private String appVersion;

	public static final String AUTHORIZATION_HEADER = "Authorization";
	public static final String DEFAULT_INCLUDE_PATTERN = "/.*";
	private final Logger log = LoggerFactory.getLogger(SwaggerConfig.class);

	    @Bean
    public LinkDiscoverers discoverers() {
        List<LinkDiscoverer> plugins = new ArrayList<>();
        plugins.add(new CollectionJsonLinkDiscoverer());
        return new LinkDiscoverers(SimplePluginRegistry.create(plugins));

    }
	@Bean
	public Docket swaggerSpringfoxDocket() {
		log.debug("Starting Swagger");
		ApiInfo apiInfo = new ApiInfo(
				"rds-consumer",
				"RDS Consumer",
				appVersion,
				"Terms of service",
				new Contact("ASI Systems Development", "https://asijira-confluence.buenosaires.gob.ar/display/APPGE/Desarrollo", ""),
				"Apache License Version 2.0",
				"https://www.apache.org/licenses/LICENSE-2.0", Collections.emptyList());

		Docket docket = new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo)
				//.apiInfo(ApiInfo.DEFAULT)
				.forCodeGeneration(true)
				.genericModelSubstitutes(ResponseEntity.class)
				.ignoredParameterTypes(Pageable.class)
				.ignoredParameterTypes(java.sql.Date.class)
				.directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
				.directModelSubstitute(java.time.ZonedDateTime.class, Date.class)
				.directModelSubstitute(java.time.LocalDateTime.class, Date.class)
				.securityContexts(Lists.newArrayList(Collections.singleton(securityContext())))
				.securitySchemes(Lists.newArrayList(apiKey()))
				//.enableUrlTemplating(true)
				.useDefaultResponseMessages(false);

		docket = docket.select()
				.apis(RequestHandlerSelectors.basePackage("ar.gob.recibosdesueldos.consumer.controllers"))
				.paths(PathSelectors.any())
				.build();
		return docket;
	}


	private ApiKey apiKey() {
		return new ApiKey("JWT", AUTHORIZATION_HEADER, "header");
	}

	private SecurityContext securityContext() {
		return SecurityContext.builder()
				.securityReferences(defaultAuth())
				.forPaths(PathSelectors.regex(DEFAULT_INCLUDE_PATTERN))
				.build();
	}

	List<SecurityReference> defaultAuth() {
		AuthorizationScope authorizationScope
				= new AuthorizationScope("global", "accessEverything");
		AuthorizationScope[] authorizationScopes = new AuthorizationScope[1];
		authorizationScopes[0] = authorizationScope;
		return Lists.newArrayList(
				new SecurityReference("JWT", authorizationScopes));
	}
}
