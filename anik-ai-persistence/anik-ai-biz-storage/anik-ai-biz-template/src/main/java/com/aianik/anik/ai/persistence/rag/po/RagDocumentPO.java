package com.aianik.anik.ai.persistence.rag.po;

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
 * RAG document persistence object
 * Table: anik_ai_rag_document
 *
 * Represents a complete document in the knowledge base
 * Supports multiple file types (PDF, Word, TXT, etc.) and storage backends (local, OSS, S3, etc.)
 * The document will be divided into multiple chunks for vectorization and retrieval.
 *
 * @author openanik
 * @date 2026-04-14
 */
@TableName("anik_ai_rag_document")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RagDocumentPO {

    /**
     * Document ID (primary key)
     * Auto-increment primary key, unique in overall situation
     */
    @TableId(type = IdType.AUTO)
    private Long id;

    /**
     * RAG ID (foreign key)
     * Linked to anik_ai_rag.id
     * The RAG to which this document belongs
     */
    @TableField("rag_id")
    private Long ragId;

    /**
     * file name
     * The name of the original file (without path)
     * Used for front-end display and user identification
     */
    private String name;

    /**
     * File type
     * The file extension or MIME type
     * For example: pdf, docx, txt, md, html, etc.
     */
    private String fileType;

    /**
     * Source type
     * How the document comes from
     * Possible values: UPLOAD (user upload), URL (network acquisition), API_PROVIDED (API provided), etc.
     */
    private String sourceType;

    /**
     * Document status
     * PENDING: Pending
     * PROCESSING: Processing (segmentation, vectorization)
     * COMPLETED: Completed
     * FAILED: Processing failed
     */
    private Integer status;

    /**
     * error message
     * Error description when Processing failed
     * null in success status
     */
    private String errorMsg;

    /**
     * Number of chunks
     * The total number of chunks generated after the document is divided
     * for statistics and display
     */
    private Integer chunkCount;

    /**
     * File content SHA-256 hash
     * Used for document-level deduplication: identical hashes within the same RAG are considered duplicates
     */
    private String contentHash;

    /**
     * Repository ID (foreign key)
     * Linked to anik_ai_resource.id
     * Files are stored uniformly through the resource module
     */
    private Long resourceId;

    /**
     * creation time
     * The moment the document is first uploaded/created
     */
    private LocalDateTime createDt;

    /**
     * Update time
     * The moment when the document was last refreshed
     */
    private LocalDateTime updateDt;
}
