package com.aianik.anik.ai.persistence.agent.enums;

import lombok.Getter;

/**
 * Conversation message role enum
 */
@Getter
public enum ConversationRoleEnum {

    USER("user"),
    ASSISTANT("assistant"),
    SYSTEM("system");

    private final String value;

    ConversationRoleEnum(String value) {
        this.value = value;
    }

    public boolean matches(String role) {
        return value.equals(role);
    }
}
