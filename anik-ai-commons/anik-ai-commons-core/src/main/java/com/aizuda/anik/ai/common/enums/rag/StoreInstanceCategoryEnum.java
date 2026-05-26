package com.aizuda.anik.ai.common.enums.rag;

import com.baomidou.mybatisplus.annotation.EnumValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum StoreInstanceCategoryEnum {

    VECTOR_STORE(1, "vector library"),
    SEARCH_ENGINE(2, "search engine");

    @EnumValue
    private final Integer category;
    private final String description;

    public static StoreInstanceCategoryEnum fromCategory(Integer category) {
        if (category == null) {
            return null;
        }
        for (StoreInstanceCategoryEnum e : values()) {
            if (e.category.equals(category)) {
                return e;
            }
        }
        return null;
    }
}
