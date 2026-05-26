-- ============================================================
-- Anik AI MySQL full table creation script (CREATE only, no ALTER)
--Use: mysql -u user -p database < anik_ai_schema.sql
-- ============================================================

-- ============================================================
-- Enterprise RAG Schema for MySQL
-- ============================================================

-- Knowledge Base
CREATE TABLE anik_ai_rag
(
    id                        BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name                      VARCHAR(255) NOT NULL,
    description               TEXT,
    icon                      VARCHAR(512),
    embedding_model_id        BIGINT(128)  NOT NULL,
    dimension_of_vector_model INT          NOT NULL COMMENT 'vector dimensions',
    rerank_model_id           BIGINT(128),
    search_engine_instance_id BIGINT(128),
    vector_store_instance_id  BIGINT(128),
    search_engine_enable      TINYINT(1)            DEFAULT 0,
    delimiter                 VARCHAR(32)           DEFAULT '\n\n',
    rag_enhancement           TEXT,
    config                    TEXT                  DEFAULT NULL COMMENT 'RAG search and question and answer page configuration parameters',
    dedup_strategy            TINYINT(1)   NOT NULL DEFAULT 2            COMMENT 'Deduplication strategy: 0=NONE 1=BY_NAME 2=BY_CONTENT 3=BY_NAME_OR_CONTENT',
    dedup_action              TINYINT(1)   NOT NULL DEFAULT 0            COMMENT 'Conflicting actions: 0=REJECT 1=SKIP 2=OVERWRITE',
    upload_confirm            TINYINT(1)   NOT NULL DEFAULT 1            COMMENT 'Second confirmation before uploading: 0-off 1-on',
    create_dt                 TIMESTAMP             DEFAULT CURRENT_TIMESTAMP,
    update_dt                 TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

-- RAG Documents
CREATE TABLE anik_ai_rag_document
(
    id           BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rag_id       BIGINT NOT NULL,
    name         VARCHAR(255),
    file_type    VARCHAR(32),
    source_type  VARCHAR(32),
    source_path  VARCHAR(1024),
    storage_path VARCHAR(1024),
    storage_type VARCHAR(32) DEFAULT 'LOCAL',
    file_size    BIGINT      DEFAULT 0,
    content      TEXT,
    status       TINYINT(1)  DEFAULT 0 COMMENT 'Status: 0-Pending 1-Parsing 2-Processing 3-Processing completed 4-Processing failed',
    error_msg    TEXT,
    chunk_count  INT         DEFAULT 0,
    content_hash VARCHAR(64) DEFAULT NULL COMMENT 'SHA-256 hash of file content for deduplication',
    resource_id  BIGINT      DEFAULT NULL COMMENT 'Related resource library anik_ai_resource.id',
    create_dt    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    update_dt    TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_rag_doc_rag ON anik_ai_rag_document (rag_id);
CREATE INDEX idx_rag_content_hash ON anik_ai_rag_document (rag_id, content_hash);
CREATE INDEX idx_rag_name ON anik_ai_rag_document (rag_id, name);
CREATE INDEX idx_rag_doc_resource ON anik_ai_rag_document (resource_id);

-- RAG Chunks
CREATE TABLE anik_ai_rag_chunk
(
    id              BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    rag_id          BIGINT NOT NULL,
    document_id     BIGINT NOT NULL,
    paragraph_index INT,
    chunk_index     INT,
    content         TEXT,
    token_count     INT,
    vector_id       VARCHAR(128),
    content_hash    VARCHAR(64) DEFAULT NULL COMMENT 'Chunk content SHA-256, used for vector deduplication',
    create_dt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_dt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci;

CREATE INDEX idx_rag_chunk_rag ON anik_ai_rag_chunk (rag_id);
CREATE INDEX idx_rag_chunk_document ON anik_ai_rag_chunk (document_id);
CREATE INDEX idx_chunk_rag_hash ON anik_ai_rag_chunk (rag_id, content_hash);

CREATE TABLE anik_ai_user
(
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    role           INT,
    totals         INT,
    username       VARCHAR(255),
    email          VARCHAR(64),
    password       VARCHAR(64) NOT NULL,
    create_dt      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_dt      DATETIME    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_username (username)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4;

INSERT INTO anik_ai_user (id, role, username, email, password, create_dt, update_dt)
VALUES (1, 2, 'admin', '',  '8a739c44d0d0d134fda754cace8c06a63f0d9df431238efba4972329f5ec8346',
        '2026-02-11 13:56:48.210429', '2026-02-11 13:56:48.210429');

-- ============================================
-- 1. AI model provider list
-- ============================================
CREATE TABLE IF NOT EXISTS anik_ai_model_provider
(
    id            BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    provider_name VARCHAR(255) NOT NULL COMMENT 'Provider name',
    provider_key  VARCHAR(50)  NOT NULL COMMENT 'provider identifier',
    description   TEXT COMMENT 'Provider description',
    icon_url      VARCHAR(500) COMMENT 'LOGO icon URL',
    is_enabled    TINYINT(1) DEFAULT 1 COMMENT 'Whether enabled',
    created_dt    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    updated_dt    TIMESTAMP  DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    UNIQUE KEY uk_provider_name (provider_name),
    UNIQUE KEY uk_provider_key (provider_key)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'AI model provider table';

CREATE INDEX idx_provider_key ON anik_ai_model_provider (provider_key);
CREATE INDEX idx_is_enabled ON anik_ai_model_provider (is_enabled);

-- ============================================
-- 2. AI model configuration table
-- ============================================
CREATE TABLE IF NOT EXISTS anik_ai_model_config
(
    id           BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    provider_id  BIGINT       NOT NULL COMMENT 'Provider ID',
    model_name   VARCHAR(255) NOT NULL COMMENT 'Model name',
    model_key    VARCHAR(100) NOT NULL COMMENT 'model identifier',
    model_type   VARCHAR(50)  NOT NULL COMMENT 'Model type (CHAT/EMBEDDING/RERANKER/IMAGE/SPEECH)',
    description  VARCHAR(1000) COMMENT 'Model description',
    api_key      VARCHAR(1000) COMMENT 'API key (encrypted storage)',
    api_endpoint VARCHAR(500) COMMENT 'API endpoint URL',
    config_json  TEXT COMMENT 'Model parameter configuration (JSON format)',
    owner_id     BIGINT COMMENT 'Owner ID (NULL=overall situation, specific value=userID)',
    scope        VARCHAR(20)  NOT NULL DEFAULT 'GLOBAL' COMMENT 'Scope (GLOBAL/PERSONAL)',
    is_default   TINYINT(1)            DEFAULT 0 COMMENT 'Is it the default model?',
    is_enabled   TINYINT(1)            DEFAULT 1 COMMENT 'Whether enabled',
    created_dt   TIMESTAMP             DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    updated_dt   TIMESTAMP             DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    KEY fk_provider_id (provider_id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'AIModel configuration table';

CREATE INDEX idx_provider_model_type ON anik_ai_model_config (provider_id, model_type);
CREATE INDEX idx_model_type_enabled ON anik_ai_model_config (model_type, is_enabled);
CREATE INDEX idx_owner_id ON anik_ai_model_config (owner_id);
CREATE INDEX idx_is_default ON anik_ai_model_config (is_default);
CREATE INDEX idx_scope ON anik_ai_model_config (scope);
CREATE INDEX idx_model_key ON anik_ai_model_config (model_key);

-- ============================================
-- 3. Model usage statistics table
-- ============================================
CREATE TABLE IF NOT EXISTS anik_ai_model_usage_stat
(
    id                BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    model_id          BIGINT    NOT NULL COMMENT 'Model ID',
    user_id           BIGINT    NOT NULL COMMENT 'userID',
    total_calls       BIGINT         DEFAULT 0 COMMENT 'total calls',
    success_calls     BIGINT         DEFAULT 0 COMMENT 'Number of successful calls',
    failed_calls      BIGINT         DEFAULT 0 COMMENT 'Number of failed calls',
    total_tokens_used BIGINT         DEFAULT 0 COMMENT 'Total Token usage',
    total_cost        DECIMAL(18, 8) DEFAULT 0 COMMENT 'total cost',
    avg_response_time BIGINT         DEFAULT 0 COMMENT 'Average response time (millisecond)',
    last_used_dt      TIMESTAMP NULL DEFAULT NULL COMMENT 'last use time',
    created_dt        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    updated_dt        TIMESTAMP      DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    UNIQUE KEY unique_model_user (model_id, user_id),
    KEY fk_stat_model_id (model_id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'Model usage statistics table';

CREATE INDEX idx_model_id ON anik_ai_model_usage_stat (model_id);
CREATE INDEX idx_user_id ON anik_ai_model_usage_stat (user_id);
CREATE INDEX idx_last_used_dt ON anik_ai_model_usage_stat (last_used_dt);

-- ============================================
-- Initialization data (optional)
-- ============================================
-- Insert common AI providers (repeated provider_key is ignored)
INSERT IGNORE INTO anik_ai_model_provider (provider_name, provider_key, description, is_enabled)
VALUES ('OpenAI', 'openai', 'OpenAI official model (GPT-4, GPT-3.5, etc.)', 1),
       ('Claude', 'claude', 'Anthropic Claude model', 1),
       ('Ollama', 'ollama', 'Local open source models (Llama, Mistral, etc.)', 1),
       ('Google Gemini', 'gemini', 'Google Gemini model', 1);

-- ============================================
--agent related table
-- ============================================

--agent master table
CREATE TABLE IF NOT EXISTS anik_ai_agent
(
    id                      BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name                    VARCHAR(255) NOT NULL COMMENT 'agent name',
    description             TEXT COMMENT 'agent description',
    avatar                  VARCHAR(512) COMMENT 'Avatar URL',
    instruction             TEXT COMMENT 'system command(System Prompt)',
    greeting                TEXT COMMENT 'Welcome',
    preset_questions        TEXT COMMENT 'Preset question list (JSON array string)',
    chat_model_id           BIGINT COMMENT 'associated dialogue modelID',
    memory_enabled          TINYINT(1)  DEFAULT 0 COMMENT 'Whether enabled memory bank',
    mcp_enabled             TINYINT(1)  DEFAULT 0 COMMENT 'Whether enabledMCP',
    skill_enabled           TINYINT(1)  DEFAULT 0 COMMENT 'Whether enabledSkill',
    web_search_enabled      TINYINT(1)  DEFAULT 0 COMMENT 'Whether enabled Internet search',
    rag_enabled             TINYINT(1)  DEFAULT 0 COMMENT 'Whether enabledRAG',
    rag_id                  BIGINT NULL COMMENT 'Bind RAG ID',
    short_term_memory_size  INT         DEFAULT 20 COMMENT 'The number of items retained in the short-term memory sliding window',
    creator_id              BIGINT COMMENT 'Creator userID',
    is_featured             TINYINT(1)  DEFAULT 0 COMMENT 'Selected or not',
    view_count              INT         DEFAULT 0 COMMENT 'Views',
    status                  TINYINT     DEFAULT 1 COMMENT 'Status: 1-active 2-inactive 3-Deprecated 4-Disabled',
    config                  TEXT COMMENT 'Extended configuration (reserved)',
    app_id                  VARCHAR(128) NULL COMMENT 'Associated application ID (NULL=local execution OK)',
    create_dt               TIMESTAMP   DEFAULT CURRENT_TIMESTAMP,
    update_dt               TIMESTAMP   DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'agent table';

CREATE INDEX idx_agent_creator ON anik_ai_agent (creator_id);
CREATE INDEX idx_agent_featured ON anik_ai_agent (is_featured);

--agent dialogue table
CREATE TABLE IF NOT EXISTS anik_ai_agent_conversation
(
    id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agent_id        BIGINT      NOT NULL COMMENT 'agentID',
    user_id         BIGINT      NOT NULL COMMENT 'userID',
    conversation_id VARCHAR(64) NOT NULL COMMENT 'Conversation ID(UUID)',
    title           VARCHAR(255) COMMENT 'Conversation title',
    create_dt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_dt       TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_conv_id (conversation_id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'agent dialogue table';

CREATE INDEX idx_agent_conv_agent ON anik_ai_agent_conversation (agent_id);
CREATE INDEX idx_agent_conv_user ON anik_ai_agent_conversation (user_id);

--agent conversation message record table
CREATE TABLE IF NOT EXISTS anik_ai_agent_conversation_record
(
    id              BIGINT      NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agent_id        BIGINT      NOT NULL COMMENT 'agentID',
    conversation_id VARCHAR(64) NOT NULL COMMENT 'Conversation ID',
    user_id         BIGINT      NOT NULL COMMENT 'userID',
    role            VARCHAR(16) DEFAULT 'user' COMMENT 'user/assistant',
    content         TEXT COMMENT 'Message content',
    thinking        TEXT COMMENT 'Thought process (assistant only)',
    status          INT         DEFAULT 1 COMMENT '1=success, 2=failure, 3=in progress',
    token_count     INT         DEFAULT 0 COMMENT 'Number of Tokens',
    create_dt       TIMESTAMP   DEFAULT CURRENT_TIMESTAMP
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'Agent conversation message record';

CREATE INDEX idx_agent_rec_conv ON anik_ai_agent_conversation_record (conversation_id);

--agent usage statistics table
CREATE TABLE IF NOT EXISTS anik_ai_agent_usage_stat
(
    id                 BIGINT NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agent_id           BIGINT NOT NULL COMMENT 'agentID',
    user_id            BIGINT NOT NULL COMMENT 'userID',
    user_name          VARCHAR(255) COMMENT 'user name',
    department         VARCHAR(255) DEFAULT '' COMMENT 'department',
    message_count      INT          DEFAULT 0 COMMENT 'Number of messages',
    conversation_count INT          DEFAULT 0 COMMENT 'dialogue turns',
    stat_date          DATE   NOT NULL COMMENT 'Statistics date',
    create_dt          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_dt          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agent_user_date (agent_id, user_id, stat_date)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'agent usage statistics';

CREATE INDEX idx_usage_agent ON anik_ai_agent_usage_stat (agent_id);
CREATE INDEX idx_usage_date ON anik_ai_agent_usage_stat (stat_date);

-- ============================================
--MCP service management
-- ============================================

--MCP service configuration table
CREATE TABLE IF NOT EXISTS anik_ai_mcp_server
(
    id               BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name             VARCHAR(255)  NOT NULL COMMENT 'MCP service name',
    description      TEXT          COMMENT 'MCP service description',
    transport_type   TINYINT(1)    DEFAULT 1 COMMENT 'Transport type: 1-SSE 2-Streamable HTTP 3-Stdio',
    base_uri         VARCHAR(1024) COMMENT 'Service base address (used for SSE/Streamable HTTP)',
    endpoint         VARCHAR(1024) COMMENT 'Endpoint path (optional for SSE/Streamable HTTP)',
    command          VARCHAR(1024) COMMENT 'Stdio command (required for Stdio)',
    args             TEXT          COMMENT 'Stdio command parameter (JSON array)',
    env_vars         TEXT          COMMENT 'Stdio environment variables (JSON object)',
    version          VARCHAR(32)   DEFAULT '1.0.0' COMMENT 'Version',
    auth_type        TINYINT(1)    DEFAULT 0 COMMENT 'Authentication method: 0-No certification required 1-API Key 2-OAuth 3-Basic Auth',
    auth_config      TEXT          COMMENT 'Authentication configuration (JSON)',
    status           TINYINT(1)    DEFAULT 0 COMMENT 'Status: 0-Not connected 1-Connected 2-abnormal',
    capabilities     TEXT          COMMENT 'Capability list (JSON array)',
    last_connect_dt  TIMESTAMP NULL COMMENT 'Last connection time',
    creator_id       BIGINT        COMMENT 'Creator userID',
    create_dt        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_dt        TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'MCP service configuration table';

CREATE INDEX idx_mcp_server_creator ON anik_ai_mcp_server (creator_id);
CREATE INDEX idx_mcp_server_status ON anik_ai_mcp_server (status);

--Agent and MCP service association table (many-to-many)
CREATE TABLE IF NOT EXISTS anik_ai_agent_mcp_server
(
    id            BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agent_id      BIGINT    NOT NULL COMMENT 'agentID',
    mcp_server_id BIGINT    NOT NULL COMMENT 'MCP service ID',
    create_dt     TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agent_mcp (agent_id, mcp_server_id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'agentMCP service association table';

CREATE INDEX idx_agent_mcp_agent ON anik_ai_agent_mcp_server (agent_id);
CREATE INDEX idx_agent_mcp_server ON anik_ai_agent_mcp_server (mcp_server_id);

--Agent subscribed by user (many-to-many)
CREATE TABLE IF NOT EXISTS anik_ai_user_agent
(
    id         BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    user_id    BIGINT    NOT NULL COMMENT 'userID',
    agent_id   BIGINT    NOT NULL COMMENT 'agentID',
    create_dt  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_user_agent (user_id, agent_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'agent subscribed by user';

CREATE INDEX idx_user_agent_user ON anik_ai_user_agent (user_id);

-- ============================================
--Skill Skill package management
-- ============================================

--Skill Skill package table
CREATE TABLE IF NOT EXISTS anik_ai_skill
(
    id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name          VARCHAR(255)  NOT NULL COMMENT 'Skill name (parsed from SKILL.md)',
    description   TEXT          COMMENT 'Skill description (parsed from SKILL.md)',
    file_name     VARCHAR(255)  COMMENT 'Uploaded zip file name',
    file_path     VARCHAR(1024) COMMENT 'Storage path after decompression',
    file_size     BIGINT        DEFAULT 0 COMMENT 'File size (bytes)',
    skill_content LONGTEXT          COMMENT 'SKILL.md text content (remove frontmatter)',
    storage_path  VARCHAR(500)  DEFAULT NULL COMMENT 'Object storage relative path prefix (such as skills/123/)',
    version       BIGINT        DEFAULT 0 COMMENT 'Version number, incremented when the file is changed, used for cache consistency verification',
    has_files     TINYINT(1)    DEFAULT 0 COMMENT 'Whether to include supporting files (0=SKILL.md only, 1=scripts/references, etc.)',
    creator_id    BIGINT        COMMENT 'Creator userID',
    create_dt     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_dt     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'SkillSkill package table';

CREATE INDEX idx_skill_creator ON anik_ai_skill (creator_id);

-- Skill support file content table
CREATE TABLE IF NOT EXISTS anik_ai_skill_file
(
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    skill_id   BIGINT       NOT NULL COMMENT 'Skill ID',
    file_path  VARCHAR(255) NOT NULL COMMENT 'File relative path',
    content    LONGTEXT     NOT NULL COMMENT 'File content',
    file_size  INT          NOT NULL COMMENT 'File size (bytes)',
    encoding   VARCHAR(50)  DEFAULT 'utf-8' COMMENT 'encoding method',
    created_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP COMMENT 'creation time',
    updated_at TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Update time',
    UNIQUE KEY uk_skill_path (skill_id, file_path),
    KEY idx_skill_id (skill_id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'Skill support file content table';

--Agent and Skill association table (many-to-many)
CREATE TABLE IF NOT EXISTS anik_ai_agent_skill
(
    id         BIGINT    NOT NULL AUTO_INCREMENT PRIMARY KEY,
    agent_id   BIGINT    NOT NULL COMMENT 'agentID',
    skill_id   BIGINT    NOT NULL COMMENT 'Skill ID',
    create_dt  TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_agent_skill (agent_id, skill_id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'agentSkill association table';

CREATE INDEX idx_agent_skill_agent ON anik_ai_agent_skill (agent_id);
CREATE INDEX idx_agent_skill_skill ON anik_ai_agent_skill (skill_id);

-- ============================================================
CREATE TABLE IF NOT EXISTS anik_ai_store_instance
(
    id         BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    name       VARCHAR(128) NOT NULL COMMENT 'Instance name',
    category   TINYINT(1)   NOT NULL COMMENT 'Category: 1-vector library 2-search engine',
    type       TINYINT(1)   NOT NULL COMMENT 'Type: 1-PG_VECTOR 2-MILVUS 3-ELASTICSEARCH 4-PG_FULLTEXT',
    config     TEXT         DEFAULT NULL COMMENT 'connection parameter JSON',
    status     TINYINT(1)   DEFAULT 1 COMMENT 'Status: 0-disabled 1-enable',
    is_default TINYINT(1)   DEFAULT 0 COMMENT 'Whether it is the default instance under this category',
    create_dt  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_dt  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'Storage instance';

CREATE INDEX idx_store_instance_category ON anik_ai_store_instance (category);
CREATE INDEX idx_store_instance_type ON anik_ai_store_instance (type);

-- ============================================================
--Memory system (configuration/main table/history/summary/statistics/extraction progress)
-- Dependency: anik_ai_store_instance (conversation_memory foreign key)
-- ============================================================

--client application
-- ----------------------------
CREATE TABLE IF NOT EXISTS anik_ai_app
(
    id             BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    app_id         VARCHAR(128) NOT NULL COMMENT 'Apply unique identifier',
    app_name       VARCHAR(255) NOT NULL COMMENT 'Application name',
    description    VARCHAR(512) COMMENT 'Application description',
    token          VARCHAR(128) NOT NULL COMMENT 'communication authentication token',
    route_strategy VARCHAR(32)  DEFAULT 'LEAST_LOAD' COMMENT 'Routing strategy',
    status         TINYINT(1)   DEFAULT 1 COMMENT '1=enable, 0=disable',
    create_dt      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_dt      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_id (app_id)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'client application';

-- ----------------------------
--AI client instance node
-- ----------------------------
CREATE TABLE IF NOT EXISTS anik_ai_client_node
(
    id                  BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    app_id              VARCHAR(128) NOT NULL COMMENT 'App ID',
    host_id             VARCHAR(128) NOT NULL COMMENT 'Client instance unique identifier',
    host_ip             VARCHAR(64)  NOT NULL COMMENT 'client IP',
    grpc_port           INT          NOT NULL COMMENT 'client gRPC port',
    max_concurrent      INT          DEFAULT 10 COMMENT 'Maximum number of concurrent conversations',
    active_chats        INT          DEFAULT 0 COMMENT 'Number of currently active conversations',
    supported_providers TEXT COMMENT 'Supported model providers (JSON array)',
    labels              TEXT COMMENT 'routing label',
    expire_dt           DATETIME     NOT NULL COMMENT 'Expiration time (heartbeat renew)',
    create_dt           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_dt           TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_client_node (app_id, host_id),
    INDEX idx_app_expire (app_id, expire_dt)
) ENGINE = InnoDB
  DEFAULT CHARSET = utf8mb4
  COLLATE = utf8mb4_unicode_ci COMMENT = 'AI client instance node';

--OpenAPI external user mapping table
CREATE TABLE anik_ai_openapi_user
(
    id               BIGINT       NOT NULL AUTO_INCREMENT PRIMARY KEY,
    app_id           VARCHAR(128) NOT NULL COMMENT 'Association anik_ai_app.app_id',
    open_id          VARCHAR(64)  NOT NULL COMMENT 'Platform-assigned unique identifier (UUID)',
    platform_user_id BIGINT       NOT NULL COMMENT 'Association anik_ai_user.id, automatically created when registering',
    external_id      VARCHAR(256) DEFAULT NULL COMMENT 'The user ID of the external system (optional, idempotent)',
    nickname         VARCHAR(128) DEFAULT NULL COMMENT 'External user nickname',
    create_dt        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    update_dt        TIMESTAMP    DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_app_open (app_id, open_id),
    UNIQUE KEY uk_app_external (app_id, external_id),
    INDEX            idx_open_id (open_id),
    INDEX            idx_platform_user (platform_user_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci
  COMMENT='OpenAPI external user mapping table';

-- ----------------------------
--Universal resource storage
-- ----------------------------
CREATE TABLE IF NOT EXISTS anik_ai_resource
(
    id            BIGINT        NOT NULL AUTO_INCREMENT PRIMARY KEY,
    storage_key   VARCHAR(512)  NOT NULL COMMENT 'Storage key (relative path or object Key)',
    original_name VARCHAR(255)  NOT NULL COMMENT 'original file name',
    file_size     BIGINT        DEFAULT 0 COMMENT 'File size (bytes)',
    mime_type     VARCHAR(128)  COMMENT 'MIME type',
    storage_type  VARCHAR(32)   NOT NULL DEFAULT 'LOCAL' COMMENT 'Storage type: LOCAL/MINIO',
    access_url    VARCHAR(1024) COMMENT 'Visit URL',
    biz_type      VARCHAR(64)   NOT NULL DEFAULT 'GENERAL' COMMENT 'Business type: AVATAR/ATTACHMENT/DOCUMENT/GENERAL',
    biz_id        BIGINT        COMMENT 'Associated business ID',
    creator_id    BIGINT        COMMENT 'Uploader ID',
    create_dt     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP,
    update_dt     TIMESTAMP     DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    UNIQUE KEY uk_storage_key (storage_key),
    INDEX idx_biz (biz_type, biz_id),
    INDEX idx_creator (creator_id)
    ) ENGINE = InnoDB
    DEFAULT CHARSET = utf8mb4
    COLLATE = utf8mb4_unicode_ci COMMENT = 'Universal resource storage';
