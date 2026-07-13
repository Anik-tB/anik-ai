package com.aianik.anik.ai.persistence.admin.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Store instance persistence object
 * Table: anik_ai_store_instance
 *
 * Represents a configuration instance of an external storage service
 * Supports multiple storage types: vector library (PG_VECTOR/Milvus/ES), file storage, etc.
 * Multiple instances of the same type can be configured to achieve load balancing or disaster recovery
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_store_instance")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StoreInstancePO {

    /**
     * Instance ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Instance name
     * Display name for user identification
     * For example: "milvus-prod", "pg-vector-backup"
     */
    private String name;

    /**
     * storage class
     * Corresponds to the StoreCategoryEnum enumeration value
     * VECTOR_STORE: vector library
     * FILE_STORE: file storage
     * SEARCH_ENGINE: search engine
     */
    private Integer category;

    /**
     * storage type
     * PG_VECTOR: PostgreSQL vector extension
     * MILVUS: Milvusvector library
     * ELASTICSEARCH: Elasticsearchsearch engine
     * PG_FULLTEXT: PostgreSQL full text search
     * OSS: Object Storage Service
     * S3: AWS S3
     */
    private Integer type;

    /**
     * Instance configuration (JSON format)
     * Store corresponding connection information according to type
     * For example:
     * {
     *   "host": "localhost",
     *   "port": 19530,
     *   "database": "default",
     *   "username": "root",
     *   "password": "Milvus"
     * }
     */
    private String config;

    /**
     * Instance status
     * ACTIVE: active/available
     * INACTIVE: inactive/Disable
     * ERROR: Error/Connection failed
     * MAINTENANCE: Under maintenance
     */
    private Integer status;

    /**
     * Is it the default instance?
     * true: the default instance of this type, used when no specific instance is specified
     * false: non-default instance
     * Only one instance of each type at the same time is the default
     */
    private Boolean isDefault;

    /**
     * creation time
     * The moment the instance configuration is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The last time the instance configuration was refreshed
     */
    private LocalDateTime updateDt;
}
