package com.aianik.anik.ai.persistence.model.mapper;

import com.aianik.anik.ai.persistence.model.po.AiModelUsageStatPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.time.LocalDate;
import java.util.List;

/**
 * Model usage statistics Mapper interface
 */
@Mapper
public interface AiModelUsageStatMapper extends BaseMapper<AiModelUsageStatPO> {

}
