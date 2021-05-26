package it.gov.pagopa.tkm.ms.cardmanager.config;

import org.jetbrains.annotations.*;
import org.springframework.context.annotation.*;
import org.springframework.http.converter.*;
import org.springframework.http.converter.json.*;
import org.springframework.http.converter.xml.*;
import org.springframework.web.servlet.config.annotation.*;

import java.util.*;

@EnableWebMvc
@Configuration
public class MessageConvertersConfig implements WebMvcConfigurer {

    @Override
    public void configureMessageConverters(@NotNull List<HttpMessageConverter<?>> converters) {
        converters.add(new MappingJackson2HttpMessageConverter());
        converters.add(new MappingJackson2XmlHttpMessageConverter());
    }

}