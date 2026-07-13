package com.aianik.anik.ai.features.resource.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "anik-ai.resource")
public class ResourceConfig {

    /** Storage backend for activation: LOCAL or MINIO*/
    private String storageType = "LOCAL";

    /** LOCAL storage root directory */
    private String uploadDir = "./upload/resource";

    /** MinIO connection configuration */
    private MinioConfig minio = new MinioConfig();

    @Data
    public static class MinioConfig {
        private String endpoint = "http://localhost:9000";
        private String accessKey = "minioadmin";
        private String secretKey = "minioadmin";
        private String bucket = "anik-ai";
    }
}
