package com.ruoyi.framework.aspectj;

import java.lang.reflect.Method;
import java.util.concurrent.TimeUnit;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import com.ruoyi.common.annotation.RateLimiter;
import com.ruoyi.common.core.redis.RedisCache;
import com.ruoyi.common.enums.LimitType;
import com.ruoyi.common.exception.ServiceException;
import com.ruoyi.common.utils.MessageUtils;
import com.ruoyi.common.utils.ServletUtils;
import com.ruoyi.common.utils.StringUtils;
import com.ruoyi.common.utils.ip.IpUtils;

/**
 * 限流处理
 * 
 * @author ruoyi
 */
@Aspect
@Component
public class RateLimiterAspect
{
    private static final Logger log = LoggerFactory.getLogger(RateLimiterAspect.class);

    @Autowired
    private RedisCache redisCache;

    @Before(value = "@annotation(rateLimiter)")
    public void doBefore(JoinPoint point, RateLimiter rateLimiter) throws Throwable
    {
        String key = rateLimiter.key();
        int time = rateLimiter.time();
        int count = rateLimiter.count();

        String combineKey = getCombineKey(rateLimiter, point);
        try
        {
            long number = redisCache.redisTemplate.opsForValue().increment(combineKey, 1);
            if (number == 1)
            {
                redisCache.expire(combineKey, time, TimeUnit.SECONDS);
            }
            if (number > count)
            {
                log.warn("限制请求:{}, 当前请求次数:{}, 限制次数:{}", combineKey, number, count);
                throw new ServiceException(MessageUtils.message("common.rate.limit"));
            }
        }
        catch (ServiceException e)
        {
            throw e;
        }
        catch (Exception e)
        {
            throw new ServiceException(MessageUtils.message("common.rate.limit"));
        }
    }

    /**
     * 拼接限流key
     */
    public String getCombineKey(RateLimiter rateLimiter, JoinPoint point)
    {
        StringBuffer stringBuffer = new StringBuffer(rateLimiter.key());
        if (rateLimiter.limitType() == LimitType.IP)
        {
            stringBuffer.append(IpUtils.getIpAddr(ServletUtils.getRequest())).append("-");
        }
        MethodSignature signature = (MethodSignature) point.getSignature();
        Method method = signature.getMethod();
        Class<?> targetClass = method.getDeclaringClass();
        stringBuffer.append(targetClass.getName()).append("-").append(method.getName());
        return stringBuffer.toString();
    }
}
