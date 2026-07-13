package com.aianik.anik.ai.features.resource.strategy;

import java.io.InputStream;

/**
 * Universal resource storage interface (not bound to any business fields)
 */
public interface ResourceStorageService {

    /** storage type identification*/
    String getType();

    /**
     * Store the file and return the storage key
     *
     * @param bizType business type (for path partitioning)
     * @param fileName original file name
     * @param inputStream file content
     * @return storage key (relative path or object Key)
     */
    String store(String bizType, String fileName, InputStream inputStream);

    /** Load file stream*/
    InputStream load(String storageKey);

    /**delete file*/
    void delete(String storageKey);

    /** Get access URL */
    String getAccessUrl(Long resourceId, String storageKey);

    /** Check if the file exists */
    boolean exists(String storageKey);
}
