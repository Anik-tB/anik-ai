package com.aizuda.anik.ai.persistence.rag.po;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * RAG text block persistent object
 * source: anik_eye_rag_chunk
 *
 * Represents the smallest semantic unit after segmentation of knowledge base documents
 * Each chunk is a text fragment that can be vectorized and retrieved independently
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_rag_chunk")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RagChunkPO {

    /**
     * Chunk ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * RAG ID (foreign key)
     * Linked to anik_ai_rag.id
     * The RAG to which this chunk belongs
     */
    @TableField("rag_id")
    private Long ragId;

    /**
     * Document ID (foreign key)
     * Linked to anik_ai_rag_document.id
     * The document from which the chunk comes
     */
    private Long documentId;

    /**
     * paragraph index
     * The paragraph number of this chunk in the document to which it belongs (0-based)
     * For sorting and restoring original structures
     */
    private Integer paragraphIndex;

    /**
     * Chunk index
     * The serial number of the chunk in the paragraph to which it belongs (0-based)
     * Multiple chunks may come from the same paragraph (if the paragraph is very long)
     */
    private Integer chunkIndex;

    /**
     * Chunk text content
     * The complete content of this text block
     * Length is determined by splitting rules and configuration
     */
    private String content;

    /**
     * Token quantity
     * The number of tokens consumed in this chunk
     * Used for cost calculations and size limits
     */
    private Integer tokenCount;

    /**
     * Vector ID (foreign key)
     * The unique identifier of the vector corresponding to this chunk in the vector library
     * For vector retrieval and renew
     */
    private String vectorId;

    /**
     * Chunk content SHA-256 hash
     * Used for chunk-level deduplication: chunk shared vectors with the same content in the same RAG
     */
    private String contentHash;

    /**
     * creation time
     * The moment when the chunk is first created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The last time the chunk was renewed
     */
    private LocalDateTime updateDt;
}
