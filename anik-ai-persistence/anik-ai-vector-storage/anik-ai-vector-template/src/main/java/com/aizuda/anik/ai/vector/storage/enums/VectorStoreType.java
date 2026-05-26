package com.aizuda.anik.ai.vector.storage.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum VectorStoreType {

    PG_VECTOR("PG_VECTOR"),
    MILVUS("MILVUS"),
    ELASTICSEARCH("ELASTICSEARCH"),
    MONGO("MONGO");

    private final String type;
}
