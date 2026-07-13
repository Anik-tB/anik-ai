package com.aianik.anik.ai.admin.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vector dimension constraint info (model max + vector store max + effective max).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VectorDimensionConstraintVO {

    /**
     * The maximum supported dimension of the model.
     */
    private Integer modelMaxDimension;

    /**
     * Maximum supported dimensions for the vector store.
     */
    private Integer storeMaxDimension;

    /**
     * The actual effective maximum dimension (take the minimum of the two).
     */
    private Integer effectiveMaxDimension;
}
