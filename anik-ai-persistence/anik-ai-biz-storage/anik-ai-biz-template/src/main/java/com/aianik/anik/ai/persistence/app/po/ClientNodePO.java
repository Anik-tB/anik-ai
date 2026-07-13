package com.aianik.anik.ai.persistence.app.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Client node information persistence object
 * Table:anik_ai_client_node
 *
 * Record the information and status of each client node in the distributed system
 * Used for load balancing, health checking and resource allocation
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_client_node")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ClientNodePO {

    /**
     * Node ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * Application ID (foreign key)
     * Linked to anik_ai_app.app_id
     * Identify which application the node belongs to
     */
    private String appId;

    /**
     * host unique identifier
     * Typically a hostname or UUID, generated when the node starts
     */
    private String hostId;

    /**
     * Host IP address
     * The IP address of the host where the client node is located
     */
    private String hostIp;

    /**
     * gRPC service port
     * This node provides the listening port of the gRPC service
     */
    private Integer grpcPort;

    /**
     * Maximum concurrent processing capacity
     * The maximum number of conversations/requests that this node can handle simultaneously
     */
    private Integer maxConcurrent;

    /**
     * Number of currently active conversations
     * Real-time statistics for load balancing
     */
    private Integer activeChats;

    /**
     * List of supported model providers (JSON format)
     * For example: ["openai", "claude", "ollama"]
     * Comma separated or JSON array format
     */
    private String supportedProviders;

    /**
     * Node label (JSON format)
     * Custom labels that can be used for node selection and classification
     * For example: {"region":"cn-east", "priority":"high"}
     */
    private String labels;

    /**
     * Node expiration time
     * Expiration time of node registration, used for heartbeat detection and automatic cleanup
     * For regular renewal, renew this time
     */
    private LocalDateTime expireDt;

    /**
     * creation time
     * The moment when the node first registers
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The last time the node updated information (such as heartbeat, Statusrenew)
     */
    private LocalDateTime updateDt;
}
