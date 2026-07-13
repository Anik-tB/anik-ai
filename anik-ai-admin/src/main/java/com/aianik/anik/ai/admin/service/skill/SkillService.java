package com.aianik.anik.ai.admin.service.skill;

import cn.hutool.core.util.StrUtil;
import com.aianik.anik.ai.common.execption.AnikAiCommonException;
import com.aianik.anik.ai.common.execption.AnikAiException;
import com.aianik.anik.ai.persistence.agent.mapper.AgentSkillMapper;
import com.aianik.anik.ai.persistence.skill.mapper.SkillFileMapper;
import com.aianik.anik.ai.persistence.skill.mapper.SkillMapper;
import com.aianik.anik.ai.admin.vo.PageResult;
import com.aianik.anik.ai.admin.vo.skill.SkillCreateFileRequestVO;
import com.aianik.anik.ai.admin.vo.skill.SkillRenameFileRequestVO;
import com.aianik.anik.ai.persistence.agent.po.AgentSkillPO;
import com.aianik.anik.ai.persistence.skill.po.SkillFilePO;
import com.aianik.anik.ai.persistence.skill.po.SkillPO;
import com.aianik.anik.ai.admin.vo.skill.SkillCreateRequestVO;
import com.aianik.anik.ai.admin.vo.skill.SkillFileContentRequestVO;
import com.aianik.anik.ai.admin.vo.skill.SkillFileTreeNodeVO;
import com.aianik.anik.ai.admin.vo.skill.SkillResponseVO;
import com.aianik.anik.ai.admin.vo.skill.SkillUpdateRequestVO;
import com.aianik.anik.ai.persistence.security.UserSessionUtils;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.PageDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import org.yaml.snakeyaml.Yaml;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static com.aianik.anik.ai.common.constants.SystemConstants.SKILL_MD;

@Slf4j
@Service
@RequiredArgsConstructor
public class SkillService {

    private final SkillMapper skillMapper;
    private final SkillFileMapper skillFileMapper;
    private final AgentSkillMapper agentSkillMapper;
    @Value("${anik-ai.skill.upload-dir:./upload/skills}")
    private String skillUploadDir;

    /**
     * Upload Skill zip package
     */
    public SkillResponseVO upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".zip")) {
            throw new AnikAiException("Only skill packages in .zip format are supported.");
        }

        try {
            // Create temporary directory to unzip
            Path tempDir = Files.createTempDirectory("skill-upload-");

            try {
                // Unzip the zip file
                unzip(file.getInputStream(), tempDir);

                // Find SKILL.md (may be in the root directory or a subdirectory)
                Path skillMdPath = findSkillMd(tempDir);
                if (skillMdPath == null) {
                    throw new AnikAiCommonException("SKILL.md file not found in zip package");
                }

                // Parsing SKILL.md
                String content = Files.readString(skillMdPath, StandardCharsets.UTF_8);
                Map<String, Object> frontmatter = parseFrontmatter(content);
                if (frontmatter == null || frontmatter.isEmpty()) {
                    throw new AnikAiCommonException("SKILL.md is missing YAML frontmatter (---...---)");
                }

                String name = (String) frontmatter.get("name");
                String description = (String) frontmatter.get("description");
                if (StrUtil.isBlank(name)) {
                    throw new AnikAiCommonException("name field missing in SKILL.md frontmatter");
                }
                if (StrUtil.isBlank(description)) {
                    throw new AnikAiCommonException("description field missing in SKILL.md frontmatter");
                }
                Long count = skillMapper.selectCount(new LambdaQueryWrapper<SkillPO>().eq(SkillPO::getName, name));
                if (count > 0) {
                    throw new AnikAiCommonException("{} already exists", name);
                }

                // Remove frontmatter and keep text
                String skillContent = removeFrontmatter(content);

                // First create a record to get the id
                SkillPO po = SkillPO.builder()
                        .name(name)
                        .description(description)
                        .fileName(originalFilename)
                        .fileSize(file.getSize())
                        .skillContent(skillContent)
                        .version(1L)
                        .creatorId(UserSessionUtils.currentUserSession().getId())
                        .build();
                skillMapper.insert(po);

                //Move to persistence directory {upload-dir}/{skillId}/
                Path targetDir = Paths.get(skillUploadDir, String.valueOf(po.getId()));
                Files.createDirectories(targetDir);

                //Determine the source directory to be moved (the directory where SKILL.md is located)
                Path skillRootDir = skillMdPath.getParent();
                copyDirectory(skillRootDir, targetDir);

                //renew filePath and hasFiles
                po.setFilePath(targetDir.toAbsolutePath().toString());
                po.setHasFiles(hasExtraFiles(skillRootDir));
                skillMapper.updateById(po);

                // Save supporting files to database
                if (po.getHasFiles()) {
                    saveSkillFilesFromZip(po.getId(), skillRootDir);
                }

                return toResponseVO(po);

            } finally {
                // Clean up temporary directory
                deleteDirectory(tempDir);
            }

        } catch (AnikAiCommonException e) {
            throw e;
        } catch (Exception e) {
            log.error("Failed to upload skill package", e);
            throw new AnikAiCommonException("Failed to upload skill package: ", e);
        }
    }

    /**
     * Page query
     */
    public PageResult<List<SkillResponseVO>> page(int page, int size, String keyword) {
        LambdaQueryWrapper<SkillPO> wrapper = new LambdaQueryWrapper<SkillPO>()
                .like(StrUtil.isNotBlank(keyword), SkillPO::getName, keyword)
                .orderByDesc(SkillPO::getCreateDt);

        PageDTO<SkillPO> pageDTO = new PageDTO<>(page, size);
        IPage<SkillPO> result = skillMapper.selectPage(pageDTO, wrapper);

        List<SkillResponseVO> records = result.getRecords().stream()
                .map(this::toResponseVO)
                .collect(Collectors.toList());

        return new PageResult<>(pageDTO, records);
    }

    /**
     * delete Skill
     */
    @Transactional
    public void delete(Long id) {
        SkillPO po = skillMapper.selectById(id);
        if (po == null) {
            return;
        }

        //delete association
        agentSkillMapper.delete(
                new LambdaQueryWrapper<AgentSkillPO>().eq(AgentSkillPO::getSkillId, id));

        //Delete Skill file records in the database
        skillFileMapper.delete(new LambdaQueryWrapper<SkillFilePO>().eq(SkillFilePO::getSkillId, id));

        //delete disk file
        if (StrUtil.isNotBlank(po.getFilePath())) {
            try {
                deleteDirectory(Paths.get(po.getFilePath()));
            } catch (IOException e) {
                log.warn("Failed to delete Skill file: {}", po.getFilePath(), e);
            }
        }

        skillMapper.deleteById(id);
    }

    /**
     * Full list (for agent selection drop-down)
     */
    public List<SkillResponseVO> listAll() {
        List<SkillPO> list = skillMapper.selectList(
                new LambdaQueryWrapper<SkillPO>().orderByDesc(SkillPO::getCreateDt));
        return list.stream().map(this::toResponseVO).collect(Collectors.toList());
    }

    public SkillResponseVO getById(Long id) {
        SkillPO po = skillMapper.selectById(id);
        if (po == null) {
            throw new AnikAiException("Skill does not exist: " + id);
        }
        return toResponseVO(po);
    }

    /**
     * Create skills online (no zip, DB only)
     */
    @Transactional
    public SkillResponseVO createOnline(SkillCreateRequestVO request) {
        String name = request.getName().trim();
        Long dup = skillMapper.selectCount(new LambdaQueryWrapper<SkillPO>().eq(SkillPO::getName, name));
        if (dup != null && dup > 0) {
            throw new AnikAiCommonException("Skill name already exists: " + name);
        }
        String desc = StrUtil.nullToDefault(request.getDescription(), "");
        SkillPO po = SkillPO.builder()
                .name(name)
                .description(desc)
                .fileName("online")
                .fileSize(0L)
                .skillContent("")
                .version(1L)
                .hasFiles(false)
                .creatorId(UserSessionUtils.currentUserSession().getId())
                .build();
        skillMapper.insert(po);
        return toResponseVO(po);
    }

    /**
     * renew Skill metadata (name/description)
     */
    @Transactional
    public SkillResponseVO update(Long id, SkillUpdateRequestVO request) {
        SkillPO po = skillMapper.selectById(id);
        if (po == null) {
            throw new AnikAiCommonException("Skill does not exist: " + id);
        }
        boolean changed = false;
        if (StrUtil.isNotBlank(request.getName())) {
            String newName = request.getName().trim();
            SkillPO other = skillMapper.selectOne(
                    new LambdaQueryWrapper<SkillPO>().eq(SkillPO::getName, newName).ne(SkillPO::getId, id));
            if (other != null) {
                throw new AnikAiCommonException("Skill name already exists: " + newName);
            }
            if (!newName.equals(po.getName())) {
                po.setName(newName);
                changed = true;
            }
        }
        if (request.getDescription() != null) {
            String desc = request.getDescription();
            if (!Objects.equals(desc, po.getDescription())) {
                po.setDescription(desc);
                changed = true;
            }
        }
        if (changed) {
            long newVersion = (po.getVersion() != null ? po.getVersion() : 0L) + 1;
            po.setVersion(newVersion);
            po.setUpdateDt(LocalDateTime.now());
            skillMapper.updateById(po);
        }
        return toResponseVO(po);
    }

    /**
     * Get the Skill list associated with the agent
     */
    public List<SkillResponseVO> getByAgentId(Long agentId) {
        List<AgentSkillPO> relations = agentSkillMapper.selectList(
                new LambdaQueryWrapper<AgentSkillPO>().eq(AgentSkillPO::getAgentId, agentId));

        if (relations.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> skillIds = relations.stream()
                .map(AgentSkillPO::getSkillId)
                .collect(Collectors.toList());
        List<SkillPO> skills = skillMapper.selectByIds(skillIds);
        return skills.stream().map(this::toResponseVO).collect(Collectors.toList());
    }

    /**
     * renewagent's Skill association
     */
    @Transactional
    public void updateAgentSkills(Long agentId, List<Long> skillIds) {
        //Delete the old association first
        agentSkillMapper.delete(
                new LambdaQueryWrapper<AgentSkillPO>().eq(AgentSkillPO::getAgentId, agentId));

        // Insert new association
        if (skillIds != null && !skillIds.isEmpty()) {
            for (Long skillId : skillIds) {
                AgentSkillPO relation = AgentSkillPO.builder()
                        .agentId(agentId)
                        .skillId(skillId)
                        .build();
                agentSkillMapper.insert(relation);
            }
        }
    }


    // ==================== File Management API (online editing, via SkillCacheService + SkillStorageService) ====================


    /**
     * Verify relative paths, prohibit .. and absolute paths
     */
    private String normalizeRelativePath(String path) {
        if (StrUtil.isBlank(path)) {
            throw new AnikAiException("Path cannot be empty");
        }
        path = path.replace('\\', '/').trim();
        if (path.startsWith("/") || path.contains("..")) {
            throw new AnikAiException("Illegal path: " + path);
        }
        return path;
    }

    /**
     * Build file tree (SKILL.md + skill_file)
     */
    public SkillFileTreeNodeVO buildFileTree(Long skillId) {
        SkillPO po = skillMapper.selectById(skillId);
        if (po == null) {
            throw new AnikAiException("Skill does not exist: " + skillId);
        }
        String skillMdFull = rebuildSkillMd(po);
        long skillMdSize = skillMdFull.getBytes(StandardCharsets.UTF_8).length;
        SkillFileTreeNodeVO skillMdNode = SkillFileTreeNodeVO.builder()
                .name(SKILL_MD)
                .type("file")
                .size(skillMdSize)
                .build();

        List<SkillFileTreeNodeVO> children = new ArrayList<>();
        children.add(skillMdNode);

        List<SkillFilePO> files = skillFileMapper.selectList(
                new LambdaQueryWrapper<SkillFilePO>()
                        .eq(SkillFilePO::getSkillId, skillId)
                        .orderByAsc(SkillFilePO::getFilePath));
        for (SkillFilePO f : files) {
            String fp = f.getFilePath();
            if (fp == null) {
                continue;
            }
            String norm = fp.replace('\\', '/').trim();
            if (SKILL_MD.equalsIgnoreCase(norm) || norm.endsWith("/" + SKILL_MD)) {
                continue;
            }
            long sz = f.getFileSize() != null ? f.getFileSize().longValue() : 0L;
            insertPathIntoTree(children, norm, sz);
        }
        sortTreeChildren(children);
        return SkillFileTreeNodeVO.builder()
                .name("/")
                .type("directory")
                .children(children)
                .build();
    }

    private void insertPathIntoTree(List<SkillFileTreeNodeVO> rootChildren, String relativePath, long size) {
        String p = relativePath.replace('\\', '/').trim();
        if (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (StrUtil.isBlank(p)) {
            return;
        }
        String[] parts = p.split("/");
        List<SkillFileTreeNodeVO> level = rootChildren;
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i];
            if (StrUtil.isBlank(part)) {
                continue;
            }
            boolean isLast = i == parts.length - 1;
            if (isLast) {
                level.add(SkillFileTreeNodeVO.builder()
                        .name(part)
                        .type("file")
                        .size(size)
                        .build());
            } else {
                SkillFileTreeNodeVO dir = findOrCreateDir(level, part);
                if (dir.getChildren() == null) {
                    dir.setChildren(new ArrayList<>());
                }
                level = dir.getChildren();
            }
        }
    }

    private SkillFileTreeNodeVO findOrCreateDir(List<SkillFileTreeNodeVO> siblings, String name) {
        for (SkillFileTreeNodeVO n : siblings) {
            if ("directory".equals(n.getType()) && name.equals(n.getName())) {
                return n;
            }
        }
        SkillFileTreeNodeVO dir = SkillFileTreeNodeVO.builder()
                .name(name)
                .type("directory")
                .children(new ArrayList<>())
                .build();
        siblings.add(dir);
        return dir;
    }

    private void sortTreeChildren(List<SkillFileTreeNodeVO> children) {
        if (children == null) {
            return;
        }
        children.sort((a, b) -> {
            boolean skillA = SKILL_MD.equalsIgnoreCase(a.getName());
            boolean skillB = SKILL_MD.equalsIgnoreCase(b.getName());
            if (skillA != skillB) {
                return skillA ? -1 : 1;
            }
            boolean da = "directory".equals(a.getType());
            boolean db = "directory".equals(b.getType());
            if (da != db) {
                return da ? -1 : 1;
            }
            return a.getName().compareToIgnoreCase(b.getName());
        });
        for (SkillFileTreeNodeVO c : children) {
            if (c.getChildren() != null) {
                sortTreeChildren(c.getChildren());
            }
        }
    }

    /**
     * Read the file content (SKILL.md is complete Markdown; other paths are from skill_file)
     */
    public SkillFileContentRequestVO getFileContent(Long skillId, String path) {
        path = normalizeRelativePath(path);
        SkillPO po = skillMapper.selectById(skillId);
        if (po == null) {
            throw new AnikAiException("Skill does not exist: " + skillId);
        }
        if (SKILL_MD.equals(path) || path.endsWith("/" + SKILL_MD)) {
            String full = rebuildSkillMd(po);
            byte[] bytes = full.getBytes(StandardCharsets.UTF_8);
            return SkillFileContentRequestVO.builder()
                    .content(full)
                    .encoding("utf-8")
                    .size((long) bytes.length)
                    .build();
        }
        SkillFilePO f = skillFileMapper.selectOne(new LambdaQueryWrapper<SkillFilePO>()
                .eq(SkillFilePO::getSkillId, skillId)
                .eq(SkillFilePO::getFilePath, path));
        if (f == null) {
            throw new AnikAiException("File does not exist: " + path);
        }
        String c = f.getContent() != null ? f.getContent() : "";
        byte[] bytes = c.getBytes(StandardCharsets.UTF_8);
        return SkillFileContentRequestVO.builder()
                .content(c)
                .encoding("utf-8")
                .size((long) bytes.length)
                .build();
    }

    /**
     * Save the file content; if it is SKILL.md, parse frontmatter renew DB; write storage and invalidate the cache; increment the version number
     */
    @Transactional
    public void saveFileContent(Long skillId, String path, String content) {
        path = normalizeRelativePath(path);
        String body = content == null ? "" : content;

        SkillPO po = skillMapper.selectById(skillId);
        if (po != null) {
            if (SKILL_MD.equals(path) || path.endsWith("/" + SKILL_MD)) {
                Map<String, Object> frontmatter = parseFrontmatter(body);
                if (frontmatter != null && !frontmatter.isEmpty()) {
                    po.setName((String) frontmatter.getOrDefault("name", po.getName()));
                    po.setDescription((String) frontmatter.getOrDefault("description", po.getDescription()));
                }
                po.setSkillContent(removeFrontmatter(body));
            } else {
                // Non-SKILL.md files: save to database
                int fileSize = body.length();
                SkillFilePO existingFile = skillFileMapper.selectOne(new LambdaQueryWrapper<SkillFilePO>()
                        .eq(SkillFilePO::getSkillId, skillId)
                        .eq(SkillFilePO::getFilePath, path));
                if (existingFile != null) {
                    existingFile.setContent(body);
                    existingFile.setFileSize(fileSize);
                    existingFile.setUpdatedAt(LocalDateTime.now());
                    skillFileMapper.updateById(existingFile);
                } else {
                    SkillFilePO newFile = SkillFilePO.builder()
                            .skillId(skillId)
                            .filePath(path)
                            .content(body)
                            .fileSize(fileSize)
                            .encoding("utf-8")
                            .createdAt(LocalDateTime.now())
                            .updatedAt(LocalDateTime.now())
                            .build();
                    skillFileMapper.insert(newFile);
                }
            }
            // Increment the version number and recalculate the file size
            long newVersion = (po.getVersion() != null ? po.getVersion() : 0L) + 1;
            po.setVersion(newVersion);
            po.setFileSize(calculateTotalFileSize(po));
            po.setUpdateDt(LocalDateTime.now());
            skillMapper.updateById(po);
        }
    }

    /**
     * Create a new file or directory (write storage and invalidate tree cache; mark hasFiles, auto-increment version number)
     */
    @Transactional
    public void createFile(Long skillId, SkillCreateFileRequestVO dto) {
        dto.setPath(normalizeRelativePath(dto.getPath()));

        if ("file".equals(dto.getType())) {
            SkillFilePO newFile = SkillFilePO.builder()
                    .skillId(skillId)
                    .filePath(dto.getPath())
                    .content("")
                    .fileSize(0)
                    .encoding("utf-8")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            skillFileMapper.insert(newFile);
        } else if ("directory".equals(dto.getType())) {
            String base = dto.getPath().replaceAll("/+$", "");
            if (StrUtil.isBlank(base)) {
                throw new AnikAiException("Directory path cannot be empty");
            }
            String markerPath = base + "/.keep";
            Long exists = skillFileMapper.selectCount(new LambdaQueryWrapper<SkillFilePO>()
                    .eq(SkillFilePO::getSkillId, skillId)
                    .eq(SkillFilePO::getFilePath, markerPath));
            if (exists != null && exists > 0) {
                throw new AnikAiException("Directory already exists: " + base);
            }
            SkillFilePO marker = SkillFilePO.builder()
                    .skillId(skillId)
                    .filePath(markerPath)
                    .content("")
                    .fileSize(0)
                    .encoding("utf-8")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            skillFileMapper.insert(marker);
        }

        // Create a new supporting file, mark hasFiles = true and increment the version number
        SkillPO po = skillMapper.selectById(skillId);
        if (po != null) {
            if (!Boolean.TRUE.equals(po.getHasFiles())) {
                po.setHasFiles(true);
            }
            long newVersion = (po.getVersion() != null ? po.getVersion() : 0L) + 1;
            po.setVersion(newVersion);
            po.setFileSize(calculateTotalFileSize(po));
            po.setUpdateDt(LocalDateTime.now());
            skillMapper.updateById(po);
        }
    }

    /**
     * Delete files; prohibit delete SKILL.md (write storage and invalidate cache; re-judge hasFiles, auto-increment version number)
     */
    @Transactional
    public void deleteFile(Long skillId, String path) {
        path = normalizeRelativePath(path);
        if (SKILL_MD.equals(path) || path.endsWith("/" + SKILL_MD)) {
            throw new AnikAiException("Cannot delete SKILL.md");
        }

        //delete files from database
        skillFileMapper.delete(new LambdaQueryWrapper<SkillFilePO>()
                .eq(SkillFilePO::getSkillId, skillId)
                .eq(SkillFilePO::getFilePath, path));

        //After deleting, re-judge whether there are supporting files and increment the version number.
        SkillPO po = skillMapper.selectById(skillId);
        if (po != null) {
            // Check if there are any other files
            long fileCount = skillFileMapper.selectCount(new LambdaQueryWrapper<SkillFilePO>()
                    .eq(SkillFilePO::getSkillId, skillId));
            po.setHasFiles(fileCount > 0);
            long newVersion = (po.getVersion() != null ? po.getVersion() : 0L) + 1;
            po.setVersion(newVersion);
            po.setFileSize(calculateTotalFileSize(po));
            po.setUpdateDt(LocalDateTime.now());
            skillMapper.updateById(po);
        }
    }

    /**
     * Rename file or directory (write storage and invalidate cache; auto-increment version number)
     */
    @Transactional
    public void renameFile(Long skillId, SkillRenameFileRequestVO dto) {
        dto.setOldPath(normalizeRelativePath(dto.getOldPath()));
        dto.setNewPath(normalizeRelativePath(dto.getNewPath()));

        // Read old files from database
        SkillFilePO oldFile = skillFileMapper.selectOne(new LambdaQueryWrapper<SkillFilePO>()
                .eq(SkillFilePO::getSkillId, skillId)
                .eq(SkillFilePO::getFilePath, dto.getOldPath()));
        if (oldFile != null) {
            // Create new file record
            SkillFilePO newFile = SkillFilePO.builder()
                    .skillId(skillId)
                    .filePath(dto.getNewPath())
                    .content(oldFile.getContent())
                    .fileSize(oldFile.getFileSize())
                    .encoding(oldFile.getEncoding())
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            skillFileMapper.insert(newFile);
            //delete old file record
            skillFileMapper.deleteById(oldFile.getId());
        }

        // Increment version number
        SkillPO po = skillMapper.selectById(skillId);
        if (po != null) {
            long newVersion = (po.getVersion() != null ? po.getVersion() : 0L) + 1;
            po.setVersion(newVersion);
            po.setFileSize(calculateTotalFileSize(po));
            po.setUpdateDt(LocalDateTime.now());
            skillMapper.updateById(po);
        }
    }

    /**
     * Pack the Skill directory into a zip and write it to the output stream (for download; based on DB content)
     */
    public void writeZipToStream(Long skillId, OutputStream out) {
        SkillPO po = skillMapper.selectById(skillId);
        if (po == null) {
            throw new AnikAiException("Skill does not exist: " + skillId);
        }
        try (ZipOutputStream zos = new ZipOutputStream(out, StandardCharsets.UTF_8)) {
            String skillMd = rebuildSkillMd(po);
            ZipEntry skillEntry = new ZipEntry(SKILL_MD);
            zos.putNextEntry(skillEntry);
            zos.write(skillMd.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            List<SkillFilePO> files = skillFileMapper.selectList(
                    new LambdaQueryWrapper<SkillFilePO>().eq(SkillFilePO::getSkillId, skillId));
            for (SkillFilePO f : files) {
                String entryName = normalizeZipEntryName(f.getFilePath());
                ZipEntry fe = new ZipEntry(entryName);
                zos.putNextEntry(fe);
                String body = f.getContent() != null ? f.getContent() : "";
                zos.write(body.getBytes(StandardCharsets.UTF_8));
                zos.closeEntry();
            }
        } catch (IOException e) {
            log.error("Failed to package Skill zip", e);
            throw new AnikAiException("Failed to package Skill zip: " + e.getMessage());
        }
    }

    private String normalizeZipEntryName(String path) {
        if (StrUtil.isBlank(path)) {
            throw new AnikAiException("Illegal zip entry path");
        }
        String p = path.replace('\\', '/').trim();
        if (p.startsWith("/")) {
            p = p.substring(1);
        }
        if (p.contains("..")) {
            throw new AnikAiException("Illegal zip entry path: " + path);
        }
        return p;
    }

    /**
     * When uploading a Skill, save the supporting file content to the database
     */
    private void saveSkillFilesFromZip(Long skillId, Path unzipPath) {
        try (var stream = Files.walk(unzipPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(p -> !p.getFileName().toString().equals(SKILL_MD))
                    .forEach(filePath -> {
                        try {
                            String relativePath = unzipPath.relativize(filePath).toString()
                                    .replace('\\', '/');
                            String content = Files.readString(filePath, StandardCharsets.UTF_8);
                            int fileSize = (int) Files.size(filePath);

                            SkillFilePO fileContent = SkillFilePO.builder()
                                    .skillId(skillId)
                                    .filePath(relativePath)
                                    .content(content)
                                    .fileSize(fileSize)
                                    .encoding("utf-8")
                                    .createdAt(LocalDateTime.now())
                                    .updatedAt(LocalDateTime.now())
                                    .build();
                            skillFileMapper.insert(fileContent);
                        } catch (IOException e) {
                            log.error("Failed to save file to database: {}", filePath, e);
                        }
                    });

        } catch (IOException e) {
            log.error("Failed to scan Skill file: {}", unzipPath, e);
        }
    }


    /**
     * Rebuild SKILL.md based on SkillPO (frontmatter + body)
     */
    private String rebuildSkillMd(SkillPO po) {
        StringBuilder sb = new StringBuilder();
        sb.append("---\n");
        sb.append("name: ").append(StrUtil.nullToDefault(po.getName(), "")).append("\n");
        sb.append("description: \"").append(StrUtil.nullToDefault(po.getDescription(), "")).append("\"\n");
        sb.append("---\n");
        sb.append(StrUtil.nullToDefault(po.getSkillContent(), ""));
        return sb.toString();
    }

    /**
     * Recalculate total file size of Skill (SKILL.md + all supporting files)
     */
    private long calculateTotalFileSize(SkillPO po) {
        long skillMdSize = rebuildSkillMd(po).getBytes(StandardCharsets.UTF_8).length;
        List<SkillFilePO> files = skillFileMapper.selectList(
                new LambdaQueryWrapper<SkillFilePO>().eq(SkillFilePO::getSkillId, po.getId()));
        long supportFilesSize = files.stream()
                .mapToLong(f -> f.getContent() != null ? f.getContent().getBytes(StandardCharsets.UTF_8).length : 0L)
                .sum();
        return skillMdSize + supportFilesSize;
    }

    // ==================== Private methods ====================

    private SkillResponseVO toResponseVO(SkillPO po) {
        return SkillResponseVO.builder()
                .id(po.getId())
                .name(po.getName())
                .description(po.getDescription())
                .fileName(po.getFileName())
                .fileSize(po.getFileSize())
                .createDt(po.getCreateDt())
                .updateDt(po.getUpdateDt())
                .build();
    }

    /**
     * Unzip zip files to prevent zip slip attacks
     * Use Apache Commons Compress to improve ZIP compatibility and support data descriptor
     */
    private void unzip(InputStream inputStream, Path targetDir) throws IOException {
        try (ZipArchiveInputStream zis = new ZipArchiveInputStream(inputStream, StandardCharsets.UTF_8.name(), false, true)) {
            ArchiveEntry entry;
            while ((entry = zis.getNextEntry()) != null) {
                Path entryPath = targetDir.resolve(entry.getName()).normalize();
                // Prevent zip slip
                if (!entryPath.startsWith(targetDir)) {
                    throw new AnikAiException("Illegal zip entry path: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(entryPath);
                } else {
                    Files.createDirectories(entryPath.getParent());
                    Files.copy(zis, entryPath, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }

    /**
     * Find the SKILL.md file in the directory
     */
    private Path findSkillMd(Path dir) throws IOException {
        // Check the root directory first
        Path root = dir.resolve("SKILL.md");
        if (Files.exists(root)) {
            return root;
        }
        // Check the first-level subdirectory (there may be a packaging directory inside the zip)
        try (var stream = Files.list(dir)) {
            Optional<Path> found = stream
                    .filter(Files::isDirectory)
                    .map(d -> d.resolve("SKILL.md"))
                    .filter(Files::exists)
                    .findFirst();
            return found.orElse(null);
        }
    }

    /**
     * Parse YAML frontmatter
     */
    private Map<String, Object> parseFrontmatter(String content) {
        if (!content.startsWith("---")) {
            return null;
        }
        int endIndex = content.indexOf("---", 3);
        if (endIndex == -1) {
            return null;
        }
        String frontmatterStr = content.substring(3, endIndex).trim();
        try {
            Yaml yaml = new Yaml();
            return yaml.load(frontmatterStr);
        } catch (Exception e) {
            log.error("Parsing YAML frontmatter failed", e);
            return null;
        }
    }

    /**
     * Remove frontmatter and return to text content
     */
    private String removeFrontmatter(String content) {
        if (!content.startsWith("---")) {
            return content;
        }
        int endIndex = content.indexOf("---", 3);
        if (endIndex == -1) {
            return content;
        }
        return content.substring(endIndex + 3).trim();
    }

    /**
     * copy directory
     */
    private void copyDirectory(Path source, Path target) throws IOException {
        try (var stream = Files.walk(source)) {
            stream.forEach(src -> {
                Path dest = target.resolve(source.relativize(src));
                try {
                    if (Files.isDirectory(src)) {
                        Files.createDirectories(dest);
                    } else {
                        Files.copy(src, dest, StandardCopyOption.REPLACE_EXISTING);
                    }
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        }
    }

    /**
     * delete directory and its contents
     */
    private void deleteDirectory(Path dir) throws IOException {
        if (!Files.exists(dir)) {
            return;
        }
        try (var stream = Files.walk(dir)) {
            stream.sorted(Comparator.reverseOrder())
                    .forEach(path -> {
                        try {
                            Files.deleteIfExists(path);
                        } catch (IOException e) {
                            log.warn("Failed to delete file: {}", path, e);
                        }
                    });
        }
    }

    /**
     * Determine whether the decompressed Skill directory contains files other than SKILL.md
     */
    private boolean hasExtraFiles(Path skillRootDir) {
        try (var stream = Files.walk(skillRootDir)) {
            return stream.filter(Files::isRegularFile)
                    .anyMatch(p -> !p.getFileName().toString().equals(SKILL_MD));
        } catch (IOException e) {
            log.warn("Failed to check support files: {}", skillRootDir, e);
            return false;
        }
    }


}
