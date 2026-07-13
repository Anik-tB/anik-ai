package com.aianik.anik.ai.common.vo;

import cn.hutool.core.util.ObjUtil;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * @author openanik
 * @date 2022-02-27
 * @since 2.0
 */
@Data
public class BaseQueryVO {

    /**
     * Current page number
     */
    private int page = 1;

    /**
     * Number of items per page
     */
    private int size = 10;

    /**
     * Time interval, used to filter the createDt field,
     * The front-end input uses SpringBoot's default ISO 8601 format: yyyy-MM-dd'T'HH:mm:ss
     */
    private LocalDateTime[] datetimeRange;

    /**
     * start time
     *
     * @return starting time
     */
    public LocalDateTime getStartDt() {
        return ObjUtil.isEmpty(datetimeRange) ? null : datetimeRange[0];
    }

    /**
     * end time
     *
     * @return end time
     */
    public LocalDateTime getEndDt() {
        return ObjUtil.isEmpty(datetimeRange) ? null : datetimeRange[1];
    }

}
