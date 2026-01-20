package com.ruoyi.framework.aspectj;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import com.ruoyi.common.utils.ip.IpUtils;

/**
 * 管理后台IP白名单切面
 * 用于限制只有白名单中的IP才能访问管理后台
 * 
 * @author ruoyi
 */
@Aspect
@Component
public class AdminIpWhiteListAspect {
    private static final Logger log = LoggerFactory.getLogger(AdminIpWhiteListAspect.class);
    
    /**
     * 管理后台IP白名单配置
     * 从配置文件中读取，多个IP用分号分隔，支持通配符和网段
     */
    @Value("${sys.admin.whiteIPList:}")
    private String adminWhiteIpList;
    
    /**
     * 定义切点，匹配所有管理后台的控制器方法
     * 这里假设管理后台的控制器都在特定的包下
     */
    @Pointcut("execution(* com.ruoyi.web.controller..*.*(..)) || execution(* com.ruoyi.system.controller..*.*(..))")
    public void adminAccessPointcut() {}
    
    /**
     * 前置通知，在方法执行前检查IP是否在白名单中
     */
    @Before("adminAccessPointcut()")
    public void checkAdminIpWhiteList() {
        // 如果白名单为空，则不限制访问
        if (adminWhiteIpList == null || adminWhiteIpList.trim().isEmpty()) {
            return;
        }
        
        // 获取当前访问的IP地址
        String currentIp = IpUtils.getIpAddr();
        
        // 检查IP是否在白名单中
        if (!IpUtils.isMatchedIp(adminWhiteIpList, currentIp)) {
            log.warn("IP {} 不在管理后台白名单中，访问被拒绝", currentIp);
            throw new SecurityException("IP地址不在允许列表中，访问被拒绝");
        }
        
        log.debug("IP {} 在管理后台白名单中，允许访问", currentIp);
    }
}