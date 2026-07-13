package com.aianik.anik.ai.agent.core.tool;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ShellTool {

    private final String workingDirectory;
    private final long commandTimeout;
    private final int maxOutputLines;

    public ShellTool(String workingDirectory, long commandTimeout, int maxOutputLines) {
        this.workingDirectory = workingDirectory;
        this.commandTimeout = commandTimeout;
        this.maxOutputLines = maxOutputLines;
    }

    @Tool(name = "shell", description = "Execute a shell command. Can be used to run scripts in the skill directory, view files, install dependencies, etc. "
            + "Ensure the working directory is correct before use. Supports chained commands (&& or ;).")
    public String execute(
            @ToolParam(description = "The shell command to execute") String command,
            @ToolParam(description = "Working directory (optional, defaults to skill directory)", required = false) String workDir) {
        log.info("shell:{}", command);

        if (command == null || command.trim().isEmpty()) {
            return "Error: command cannot be empty";
        }

        try {
            // Determine working directory
            String effectiveWorkDir = (workDir != null && !workDir.trim().isEmpty())
                    ? workDir : workingDirectory;
            Path workPath = Paths.get(effectiveWorkDir);
            if (!Files.exists(workPath) || !Files.isDirectory(workPath)) {
                return "Error: Working directory does not exist: " + effectiveWorkDir;
            }

            //Detect operation system and build commands
            String[] shellCommand = getShellCommand(command);

            ProcessBuilder pb = new ProcessBuilder(shellCommand);
            pb.directory(workPath.toFile());
            pb.redirectErrorStream(true);

            Process process = pb.start();

            // Read output
            StringBuilder output = new StringBuilder();
            int lineCount = 0;
            boolean truncated = false;

            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    lineCount++;
                    if (lineCount <= maxOutputLines) {
                        if (output.length() > 0) {
                            output.append("\n");
                        }
                        output.append(line);
                    } else {
                        truncated = true;
                    }
                }
            }

            // Wait for the process to end and kill it if it times out.
            boolean finished = process.waitFor(commandTimeout, TimeUnit.MILLISECONDS);
            if (!finished) {
                process.destroyForcibly();
                return "Error: Command execution timed out (" + (commandTimeout / 1000) + "Second)";
            }

            int exitCode = process.exitValue();

            // Build results
            StringBuilder result = new StringBuilder();
            if (output.length() == 0) {
                result.append("<No output>");
            } else {
                result.append(output);
            }

            if (truncated) {
                result.append("\n\n... The output has been truncated before displaying ")
                        .append(maxOutputLines).append(" row (common ")
                        .append(lineCount).append(" OK)");
            }

            if (exitCode != 0) {
                result.append("\n\nExit code: ").append(exitCode);
            }

            return result.toString();

        } catch (Exception e) {
            log.error("Shell command execution failed: {}", command, e);
            return "Error: " + e.getMessage();
        }
    }

    private String[] getShellCommand(String command) {
        String os = System.getProperty("os.name", "").toLowerCase();
        if (os.contains("win")) {
            return new String[]{"cmd", "/c", command};
        } else {
            return new String[]{"/bin/sh", "-c", command};
        }
    }
}
