package com.ing.brokerage.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import java.time.OffsetTime;
import java.time.format.DateTimeFormatter;
import org.springdoc.core.utils.SpringDocUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfiguration {

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
                .title("Brokerage")
                .description("ING Brokerage System")
                .version("v1")
                .contact(new Contact()
                    .name("Can Şahintaş")
                    .email("can.sahintas@gmail.com"))
            );
    }
}
