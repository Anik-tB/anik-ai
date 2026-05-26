package com.aizuda.anik.ai.persistence.app.mapper;

import com.aizuda.anik.ai.persistence.app.po.ClientNodePO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ClientNodeMapper extends BaseMapper<ClientNodePO> {

    /**
     * Insert client nodes in batches
     */
    int insertBatch(@Param("list") List<ClientNodePO> list);

    /**
     * Expiration time and status of batch renewclient end nodes
     */
    int updateBatchExpireAt(@Param("list") List<ClientNodePO> list);
}
