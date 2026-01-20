package com.ruoyi.system.task;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.ruoyi.system.utils.FileCleanupUtils;

/**
 * 文件清理定时任务
 * 用于定期清理未引用的文件
 * 
 * @author Trae IDE
 */
@Component
public class FileCleanupTask {
    private static final Logger logger = LoggerFactory.getLogger(FileCleanupTask.class);
    
    @Autowired
    private FileCleanupUtils fileCleanupUtils;
    
    /**
     * 定期清理未引用的文件
     * 每天凌晨2点执行
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void cleanupUnusedFiles() {
        logger.info("开始执行定期文件清理任务");
        
        try {
            FileCleanupUtils.CleanupResult result = fileCleanupUtils.cleanupUnusedFiles();
            
            logger.info("文件清理任务执行完成，结果：");
            logger.info("状态：{}", result.getStatus());
            logger.info("扫描文件数：{}", result.getTotalScanned());
            logger.info("删除成功数：{}", result.getDeletedCount());
            logger.info("删除失败数：{}", result.getFailedCount());
            logger.info("释放空间：{} 字节", result.getDeletedSize());
            
            if (result.getDeletedFiles() != null && !result.getDeletedFiles().isEmpty()) {
                logger.info("删除文件列表：");
                for (String filePath : result.getDeletedFiles()) {
                    logger.info("  - {}", filePath);
                }
            }
            
            if (result.getFailedFiles() != null && !result.getFailedFiles().isEmpty()) {
                logger.info("删除失败文件列表：");
                for (String filePath : result.getFailedFiles()) {
                    logger.error("  - {}", filePath);
                }
            }
            
        } catch (Exception e) {
            logger.error("文件清理任务执行异常", e);
        }
    }
}