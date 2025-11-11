package com.ing.brokerage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import com.ing.brokerage.config.JwtService;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.MessageSource;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@ExtendWith(MockitoExtension.class)
public abstract class BaseTest {

    protected static ResourceBundleMessageSource messageResource;

    protected static MockMvc mockMvc;



    static {

        messageResource = new ResourceBundleMessageSource();
        messageResource.setBasename("messages/messages");
        messageResource.setDefaultEncoding("UTF-8");
    }

    protected ObjectMapper objectMapper =
        JsonMapper.builder()
                  .addModule(new ParameterNamesModule())
                  .addModule(new Jdk8Module())
                  .addModule(new JavaTimeModule()).build();
}
