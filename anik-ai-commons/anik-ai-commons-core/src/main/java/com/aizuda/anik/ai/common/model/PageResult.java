package com.aizuda.anik.ai.common.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Paginated response
 *
 * @author openanik
 * @date 2022-02-16
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
public class PageResult<T> extends Result<T> {

    private long page;
    private long size;
    private long total;

    public PageResult(int status, String message, T data) {
        super(status, message, data);
    }

    public PageResult() {
        super();
    }
}
