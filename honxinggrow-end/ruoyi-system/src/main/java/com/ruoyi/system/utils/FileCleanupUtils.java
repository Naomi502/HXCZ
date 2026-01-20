package com.ruoyi.system.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.ruoyi.common.config.RuoYiConfig;
import com.ruoyi.system.domain.HxActivity;
import com.ruoyi.system.domain.HxActivityParticipant;
import com.ruoyi.system.domain.HxQa;
import com.ruoyi.system.domain.HxStory;
import com.ruoyi.system.domain.HxTreeHole;
import com.ruoyi.system.mapper.HxActivityMapper;
import com.ruoyi.system.mapper.HxActivityParticipantMapper;
import com.ruoyi.system.mapper.HxQaMapper;
import com.ruoyi.system.mapper.HxStoryMapper;
import com.ruoyi.system.mapper.HxTreeHoleMapper;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * 文件清理工具类
 * 用于定期清理未引用的文件，如图片、音频、视频等
 * 
 * @author Trae IDE
 */
@Component
public class FileCleanupUtils {
    private static final Logger logger = LoggerFactory.getLogger(FileCleanupUtils.class);
    
    @Autowired
    private HxActivityMapper hxActivityMapper;
    
    @Autowired
    private HxActivityParticipantMapper hxActivityParticipantMapper;
    
    @Autowired
    private HxStoryMapper hxStoryMapper;
    
    @Autowired
    private HxQaMapper hxQaMapper;
    
    @Autowired
    private HxTreeHoleMapper hxTreeHoleMapper;
    
    // 文件保留期（天）
    @Value("${file.cleanup.retention-days:7}")
    private int retentionDays;
    
    // 白名单目录
    @Value("${file.cleanup.whitelist:/profile/static/,/profile/default/}")
    private String whitelist;
    
    // 清理开关
    @Value("${file.cleanup.enabled:true}")
    private boolean enabled;
    
    // 日志开关
    @Value("${file.cleanup.log.enabled:true}")
    private boolean logEnabled;
    
    // 上传路径
    private String uploadPath = RuoYiConfig.getProfile();
    
    // 正则表达式：匹配HTML中的图片、视频、音频标签
    private static final String FILE_PATTERN_STRING = "<(img|video|audio)\\s+[^>]*?(src|data-src)=[\"']([^\"']+)[\"'][^>]*?>";
    private static final Pattern FILE_URL_PATTERN = Pattern.compile(FILE_PATTERN_STRING, Pattern.CASE_INSENSITIVE);
    
    /**
     * 扫描指定目录下的所有文件
     * 
     * @param directoryPath 目录路径
     * @return 文件路径列表
     */
    public List<String> scanDirectory(String directoryPath) {
        List<String> fileList = new ArrayList<>();
        try {
            Files.walk(Paths.get(directoryPath))
                 .filter(Files::isRegularFile)
                 .forEach(path -> fileList.add(path.toString()));
        } catch (IOException e) {
            logger.error("扫描目录失败: {}", directoryPath, e);
        }
        return fileList;
    }
    
    /**
     * 从所有表中提取文件引用URL
     * 
     * @return 引用的文件URL集合
     */
    public Set<String> extractAllReferencedUrls() {
        Set<String> referencedUrls = new HashSet<>();
        
        // 提取活动表中的文件引用
        List<HxActivity> activities = hxActivityMapper.selectHxActivityList(new HxActivity());
        for (HxActivity activity : activities) {
            if (activity.getCoverUrl() != null && !activity.getCoverUrl().isEmpty()) {
                referencedUrls.add(activity.getCoverUrl());
            }
            if (activity.getDescription() != null && !activity.getDescription().isEmpty()) {
                referencedUrls.addAll(extractUrlsFromHtml(activity.getDescription()));
            }
        }
        
        // 提取活动参与者表中的文件引用
        // 先查询所有活动，再查询每个活动的参与者
        List<HxActivity> allActivities = hxActivityMapper.selectHxActivityList(new HxActivity());
        for (HxActivity activity : allActivities) {
            List<HxActivityParticipant> participants = hxActivityParticipantMapper.selectByActivityId(activity.getActivityId());
            for (HxActivityParticipant participant : participants) {
                if (participant.getProofImageUrl() != null && !participant.getProofImageUrl().isEmpty()) {
                    referencedUrls.add(participant.getProofImageUrl());
                }
            }
        }
        
        // 提取故事表中的文件引用
        List<HxStory> stories = hxStoryMapper.selectHxStoryList(new HxStory());
        for (HxStory story : stories) {
            if (story.getCoverUrl() != null && !story.getCoverUrl().isEmpty()) {
                referencedUrls.add(story.getCoverUrl());
            }
            if (story.getContent() != null && !story.getContent().isEmpty()) {
                referencedUrls.addAll(extractUrlsFromHtml(story.getContent()));
            }
        }
        
        // 提取问答表中的文件引用
        List<HxQa> qas = hxQaMapper.selectHxQaList(new HxQa());
        for (HxQa qa : qas) {
            if (qa.getVideoUrl() != null && !qa.getVideoUrl().isEmpty()) {
                referencedUrls.add(qa.getVideoUrl());
            }
            if (qa.getContent() != null && !qa.getContent().isEmpty()) {
                referencedUrls.addAll(extractUrlsFromHtml(qa.getContent()));
            }
        }
        
        // 提取树洞表中的文件引用
        List<HxTreeHole> treeHoles = hxTreeHoleMapper.selectHxTreeHoleList(new HxTreeHole());
        for (HxTreeHole treeHole : treeHoles) {
            if (treeHole.getContent() != null && !treeHole.getContent().isEmpty()) {
                referencedUrls.addAll(extractUrlsFromHtml(treeHole.getContent()));
            }
        }
        
        return referencedUrls;
    }
    
    /**
     * 从HTML内容中提取文件URL
     * 
     * @param html HTML内容
     * @return 文件URL集合
     */
    public Set<String> extractUrlsFromHtml(String html) {
        Set<String> urls = new HashSet<>();
        Matcher matcher = FILE_URL_PATTERN.matcher(html);
        
        while (matcher.find()) {
            String url = matcher.group(3);
            urls.add(url);
        }
        
        return urls;
    }
    
    /**
     * 获取未引用的文件列表
     * 
     * @return 未引用的文件路径列表
     */
    public List<String> getUnusedFiles() {
        if (!enabled) {
            logger.info("文件清理功能已关闭");
            return new ArrayList<>();
        }
        
        // 扫描所有文件
        List<String> allFiles = scanDirectory(uploadPath);
        logger.info("扫描到文件总数: {}", allFiles.size());
        
        // 提取所有引用的URL
        Set<String> referencedUrls = extractAllReferencedUrls();
        logger.info("提取到引用URL总数: {}", referencedUrls.size());
        
        // 转换引用URL为绝对路径
        Set<String> referencedPaths = referencedUrls.stream()
                .map(this::urlToAbsolutePath)
                .collect(Collectors.toSet());
        
        // 过滤未引用的文件
        List<String> unusedFiles = allFiles.stream()
                .filter(filePath -> !referencedPaths.contains(filePath))
                .filter(this::isBeyondRetentionPeriod)
                .filter(filePath -> !isInWhitelist(filePath))
                .collect(Collectors.toList());
        
        logger.info("未引用文件总数: {}", unusedFiles.size());
        return unusedFiles;
    }
    
    /**
     * 执行文件清理操作
     * 
     * @return 清理结果
     */
    public CleanupResult cleanupUnusedFiles() {
        CleanupResult result = new CleanupResult();
        
        if (!enabled) {
            logger.info("文件清理功能已关闭");
            result.setStatus("disabled");
            return result;
        }
        
        logger.info("开始执行文件清理任务");
        
        // 获取未引用的文件
        List<String> unusedFiles = getUnusedFiles();
        
        // 执行删除操作
        List<String> deletedFiles = new ArrayList<>();
        List<String> failedFiles = new ArrayList<>();
        long deletedSize = 0;
        
        for (String filePath : unusedFiles) {
            File file = new File(filePath);
            try {
                if (file.exists() && file.isFile()) {
                    deletedSize += file.length();
                    if (file.delete()) {
                        deletedFiles.add(filePath);
                        if (logEnabled) {
                            logger.info("删除文件成功: {}", filePath);
                        }
                    } else {
                        failedFiles.add(filePath);
                        logger.error("删除文件失败: {}", filePath);
                    }
                }
            } catch (Exception e) {
                failedFiles.add(filePath);
                logger.error("删除文件异常: {}", filePath, e);
            }
        }
        
        result.setTotalScanned(unusedFiles.size());
        result.setDeletedCount(deletedFiles.size());
        result.setFailedCount(failedFiles.size());
        result.setDeletedSize(deletedSize);
        result.setDeletedFiles(deletedFiles);
        result.setFailedFiles(failedFiles);
        result.setStatus("success");
        
        logger.info("文件清理任务完成: 扫描{}个, 删除成功{}个, 删除失败{}个, 释放空间{}字节", 
                unusedFiles.size(), deletedFiles.size(), failedFiles.size(), deletedSize);
        
        return result;
    }
    
    /**
     * 将URL转换为绝对路径
     * 
     * @param url 文件URL
     * @return 绝对路径
     */
    private String urlToAbsolutePath(String url) {
        if (url.startsWith("http://") || url.startsWith("https://")) {
            // 完整URL，跳过
            return url;
        } else if (url.startsWith("/")) {
            // 相对路径，转换为绝对路径
            return uploadPath + url;
        } else {
            // 其他情况，直接返回
            return url;
        }
    }
    
    /**
     * 判断文件是否超过保留期
     * 
     * @param filePath 文件路径
     * @return 是否超过保留期
     */
    private boolean isBeyondRetentionPeriod(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            return false;
        }
        
        // 获取文件创建时间
        LocalDateTime fileTime = null;
        try {
            java.nio.file.attribute.FileTime lastModified = Files.getLastModifiedTime(file.toPath());
            fileTime = LocalDateTime.ofInstant(
                    lastModified.toInstant(),
                    ZoneId.systemDefault()
            );
        } catch (IOException e) {
            logger.error("获取文件修改时间失败: {}", filePath, e);
            return false;
        }
        
        // 计算保留期截止时间
        LocalDateTime retentionTime = LocalDateTime.now().minusDays(retentionDays);
        
        return fileTime.isBefore(retentionTime);
    }
    
    /**
     * 判断文件是否在白名单中
     * 
     * @param filePath 文件路径
     * @return 是否在白名单中
     */
    private boolean isInWhitelist(String filePath) {
        if (whitelist == null || whitelist.isEmpty()) {
            return false;
        }
        
        String[] whitelistDirs = whitelist.split(",");
        for (String dir : whitelistDirs) {
            String absoluteDir = uploadPath + dir;
            if (filePath.startsWith(absoluteDir)) {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * 清理结果类
     */
    public static class CleanupResult {
        private String status;
        private int totalScanned;
        private int deletedCount;
        private int failedCount;
        private long deletedSize;
        private List<String> deletedFiles;
        private List<String> failedFiles;
        
        // getter and setter methods
        public String getStatus() {
            return status;
        }
        
        public void setStatus(String status) {
            this.status = status;
        }
        
        public int getTotalScanned() {
            return totalScanned;
        }
        
        public void setTotalScanned(int totalScanned) {
            this.totalScanned = totalScanned;
        }
        
        public int getDeletedCount() {
            return deletedCount;
        }
        
        public void setDeletedCount(int deletedCount) {
            this.deletedCount = deletedCount;
        }
        
        public int getFailedCount() {
            return failedCount;
        }
        
        public void setFailedCount(int failedCount) {
            this.failedCount = failedCount;
        }
        
        public long getDeletedSize() {
            return deletedSize;
        }
        
        public void setDeletedSize(long deletedSize) {
            this.deletedSize = deletedSize;
        }
        
        public List<String> getDeletedFiles() {
            return deletedFiles;
        }
        
        public void setDeletedFiles(List<String> deletedFiles) {
            this.deletedFiles = deletedFiles;
        }
        
        public List<String> getFailedFiles() {
            return failedFiles;
        }
        
        public void setFailedFiles(List<String> failedFiles) {
            this.failedFiles = failedFiles;
        }
    }
}