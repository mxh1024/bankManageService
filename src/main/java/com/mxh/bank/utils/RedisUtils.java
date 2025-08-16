package com.mxh.bank.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisUtils {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    public void set(String key, Object value) {
        redisTemplate.opsForValue().set(key, value);
    }

    /**
     * 设置缓存并指定过期时间
     */
    public void set(String key, Object value, long time, TimeUnit unit) {
        redisTemplate.opsForValue().set(key, value, time, unit);
    }

    /**
     * 获取缓存
     */
    public <T> T get(String key, Class<T> clazz) {
        Object result = redisTemplate.opsForValue().get(key);
        if (result == null) {
            return null;
        }
        // 如果已经是对象类型，直接强制转换
        if (clazz.isInstance(result)) {
            return clazz.cast(result);
        }
        return null;
    }

    /**
     * 删除缓存
     */
    public Boolean delete(String key) {
        return redisTemplate.delete(key);
    }
}

