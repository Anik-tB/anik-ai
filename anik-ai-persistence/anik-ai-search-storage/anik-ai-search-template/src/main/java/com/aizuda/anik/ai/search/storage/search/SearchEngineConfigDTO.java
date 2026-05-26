package com.aizuda.anik.ai.search.storage.search;

import com.aizuda.anik.ai.persistence.admin.po.StoreInstancePO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class SearchEngineConfigDTO {

    private StoreInstancePO storeInstance;

    private String config;
}
