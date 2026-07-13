package com.aianik.anik.ai.persistence.model.mapper;

import com.aianik.anik.ai.persistence.model.po.AiModelConfigPO;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * AI model configuration Mapper interface (optimized)
 *
 * Design concept:
 * - All query logic is implemented in the Service layer using LambdaQueryWrapper
 * - Mapper layer only retains basic CRUD operations
 * - Advantages: type safety, easy maintenance, supports dynamic conditions, avoids SQL string concatenation
 */
@Mapper
public interface AiModelConfigMapper extends BaseMapper<AiModelConfigPO> {
}
