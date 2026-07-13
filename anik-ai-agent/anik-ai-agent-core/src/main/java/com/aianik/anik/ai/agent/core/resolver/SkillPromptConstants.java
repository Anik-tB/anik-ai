package com.aianik.anik.ai.agent.core.resolver;

/**
 * Skill progressive disclosure — system prompt word template constant + lightweight Skill list construction
 */
public final class SkillPromptConstants {

    private SkillPromptConstants() {}

    /** Progressive disclosure system prompt word template, {skills_list} is a placeholder*/
    public static final String SYSTEM_PROMPT_TEMPLATE = """

            ## Skills System

            You can use the skill library to gain specialized abilities and domain knowledge.

            ### Available skills

            {skills_list}

            ### Usage (Progressive Disclosure)

            Skills follow a **Progressive Disclosure** model — you know the name and description of each skill, but need to read the full instructions on demand:

            1. **Identify skills**: Determine whether the user's question matches the description of a skill
            2. **Read command**: Use the `read_skill` tool to pass in the skill name and obtain the complete SKILL.md command
            3. **Execute Instructions**: Follow the steps, workflow and best practices in SKILL.md
            4. **Access supporting files**: Skills may contain scripts, configurations or reference documents, use the `shell` tool with absolute path access
            5. **Call the remote interface**: If you need to request an external API, use the `http_request` tool to initiate a GET/POST request

            **important**:
            - Always read skill instructions through the `read_skill` tool, do not try to access SKILL.md through other means
            - For other supporting files in the skill (scripts, reference materials, etc.), you can use the `shell` tool with absolute path access
            - When you need to call the remote interface, use the `http_request` tool first, without writing a curl script

            ### When to use skills

            - When the user's request matches the domain of a certain skill (e.g. "Extract PDF content" → pdf-extractor skill)
            - When you need expertise or structured workflow
            - When a skill provides a proven pattern for a complex task

            ### Self-documentation of skills

            - Each SKILL.md will tell you exactly what that skill does and how to use it
            - The supporting file directory path for each skill is shown in the skill list above

            ### Execute skill script

            Skills may contain Python scripts or other executable files, which are always executed using the absolute path in the skill list.

            ### Sample workflow

            user: "Help me extract key information from this PDF"

            1. View available skills above → Discover "pdf-extractor" Skill
            2. Use `read_skill("pdf-extractor")` Read the complete command
            3. Follow the extraction workflow in the skill (parse → extract → organize)
            4. If there is an auxiliary script, use the `shell` tool with the absolute path to execute it

            Remember: Skills are tools that make you more powerful and consistent. When encountering a task, check first to see if there are matching skills!
            """;
}
