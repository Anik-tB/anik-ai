package com.aizuda.anik.ai.search.storage.search;

import com.aizuda.anik.ai.common.enums.rag.StoreInstanceTypeEnum;
import com.aizuda.anik.ai.persistence.admin.mapper.StoreInstanceMapper;
import com.aizuda.anik.ai.persistence.admin.po.StoreInstancePO;
import com.aizuda.anik.ai.search.storage.search.api.SearchEngine;
import com.aizuda.anik.ai.search.storage.search.enums.SearchEngineEnum;
import com.aizuda.anik.ai.search.storage.search.exception.SearchEngineException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@RequiredArgsConstructor
public class SearchEngineFactory {

    public static final Map<SearchEngineEnum, Function<SearchEngineConfigDTO, SearchEngine>> REGISTER
            = new ConcurrentHashMap<>();

    private final StoreInstanceMapper storeInstanceMapper;

    public SearchEngine forStoreInstance(Long instanceId) {
        if (instanceId == null) {
            throw new SearchEngineException("Search engine instance ID cannot be empty");
        }
        StoreInstancePO inst = storeInstanceMapper.selectById(instanceId);
        if (inst == null) {
            throw new SearchEngineException("Search engine instance does not exist: " + instanceId);
        }

        StoreInstanceTypeEnum typeEnum = StoreInstanceTypeEnum.fromType(inst.getType());
        String type = typeEnum != null ? typeEnum.name() : null;
        SearchEngineEnum engineType = SearchEngineEnum.fromType(type);
        if (engineType == null) {
            throw new SearchEngineException("Search engine type does not exist type:" + type);
        }

        Function<SearchEngineConfigDTO, SearchEngine> constructor = REGISTER.get(engineType);
        if (constructor == null) {
            throw new SearchEngineException("Search engine type is not registered type:" + type);
        }
        return constructor.apply(SearchEngineConfigDTO.builder()
                .storeInstance(inst)
                .config(inst.getConfig())
                .build());
    }
}
