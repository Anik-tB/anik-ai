package com.aizuda.anik.ai.common.constants;

/**
 * System common constants
 */
public interface SystemConstants {

    /**
     * long format
     */
    String YYYY_MM_DD_HH_MM_SS = "yyyy-MM-dd HH:mm:ss";

    /**
     * short time format
     */
    String YYYY_MM_DD = "yyyy-MM-dd";

    String SKILL_MD = "SKILL.md";

    /**
     * RAG MCP server name constant
     */
    String RAG_MCP_SERVER_NAME = "anik-ai-rag-mcp-server";

    String LOGO = """
              ___           _   _                 /\\      _\s
             / _ \\   _ __  (_) | |  __           /  \\    (_)
            / /_\\ \\ | '_ \\  _  | |/ /           / /\\ \\   | |
           / /   \\ \\| | | || | |   <          / ____ \\  | |
          /_/     \\_\\_| |_||_||_|\\_\\        /_/    \\_\\ |_|\s
          :: Anik Ai ::                                 (v{}) \s
          """;
}
