package com.aianik.anik.ai.memory.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * Short-term memory messages - used to communicate session history across storage media
 *
 * author: openanik
 * date: 2026-03-26
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ShortTermMessage implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** Message role: user/assistant */
    private String role;

    /** Message content*/
    private String content;
}
