package com.aizuda.anik.ai.admin.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.jackson.autoconfigure.JsonMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ext.javatime.deser.LocalDateDeserializer;
import tools.jackson.databind.ext.javatime.deser.LocalDateTimeDeserializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateSerializer;
import tools.jackson.databind.ext.javatime.ser.LocalDateTimeSerializer;
import tools.jackson.databind.module.SimpleModule;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.TimeZone;

import static com.aizuda.anik.ai.common.constants.SystemConstants.YYYY_MM_DD;
import static com.aizuda.anik.ai.common.constants.SystemConstants.YYYY_MM_DD_HH_MM_SS;

/**
 * Jackson overall situation configuration
 * Unified configuration of date and time formatting
 *
 * @Author: openanik
 * @Date: 2018/09/27 12:52
 */

@Configuration
@Slf4j
public class JacksonConfig {

    @Bean
    public SimpleModule registerJavaTimeModule() {
        DateTimeFormatter localDateTimeFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD_HH_MM_SS);
        DateTimeFormatter localDateFormatter = DateTimeFormatter.ofPattern(YYYY_MM_DD);
        //Overall situation configuration serialization returns JSON processing
        SimpleModule simpleModule = new SimpleModule();
        simpleModule.addSerializer(LocalDate.class, new LocalDateSerializer(localDateFormatter));
        simpleModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(localDateTimeFormatter));
        simpleModule.addDeserializer(LocalDate.class, new LocalDateDeserializer(localDateFormatter));
        simpleModule.addDeserializer(LocalDateTime.class, new LocalDateTimeDeserializer(localDateTimeFormatter));
        return simpleModule;
    }

    @Bean
    public JsonMapperBuilderCustomizer jsonInitCustomizer() {
        return builder -> {
            builder.defaultTimeZone(TimeZone.getDefault());
            log.info("Initialize jackson configuration");
        };
    }



}
