package com.ing.brokerage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

    @Value("${dopi.api.title:Doping Tech API Title}")
    String apiTitle;

    @Value("${dopi.api.description:Doping Tech API Description}")
    String apiDescription;

    @Value("${dopi.api.version:v1.0}")
    String apiVersion;

    static {
        var schema = new Schema<OffsetTime>();
        schema.example(OffsetTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ssZ")));
        SpringDocUtils.getConfig().replaceWithSchema(OffsetTime.class, schema);
    }

    /**
     * Default openApi Bean.
     * @return OpenAPI
     */
    @Bean
    public OpenAPI openApi() {

        final String securitySchemeName = "bearerAuth";
        return new OpenAPI()
            .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
            .components(
                new Components()
                    .addSecuritySchemes(securitySchemeName,
                        new SecurityScheme()
                            .name(securitySchemeName)
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT")
                    )
            )
            .info(new Info()
                .title(apiTitle)
                .description(apiDescription)
                .version(apiVersion)
                .contact(new Contact()
                    .name("Doping Technology")
                    .url("https://dopingtech.net")
                    .email("info@dopingtech.com"))
                .termsOfService("https://www.dopinghafiza.com/kurumsal/34/iptal-iade-sartlari.html")
                .license(new License()
                    .name("License")
                    .url("https://www.dopinghafiza.com/kurumsal/100/kvkk-aydinlatma-metni.html"))
            );
    }
}
