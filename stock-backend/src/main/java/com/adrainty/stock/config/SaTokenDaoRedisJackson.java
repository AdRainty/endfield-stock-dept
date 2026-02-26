package com.adrainty.stock.config;

import cn.dev33.satoken.dao.SaTokenDao;
import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.util.SaFoxUtil;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * SaTokenDao Redis 实现（基于 Jackson 序列化）
 *
 * @author adrainty
 * @since 2026-02-26
 */
public class SaTokenDaoRedisJackson implements SaTokenDao {

    private final StringRedisTemplate redisTemplate;
    private final Jackson2JsonRedisSerializer<SaSession> sessionSerializer;
    private final Jackson2JsonRedisSerializer<Object> objectSerializer;

    private static final String LOGIN_TOKEN = "sat:token:";
    private static final String SESSION = "sat:session:";

    public SaTokenDaoRedisJackson(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.sessionSerializer = new Jackson2JsonRedisSerializer<>(SaSession.class);
        this.objectSerializer = new Jackson2JsonRedisSerializer<>(Object.class);
    }

    @Override
    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    @Override
    public void set(String key, String value, long timeout) {
        if (timeout == NOT_DATA_EXPIRE) {
            redisTemplate.opsForValue().set(key, value);
        } else {
            redisTemplate.opsForValue().set(key, value, timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public void update(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    @Override
    public void delete(String key) {
        redisTemplate.delete(key);
    }

    @Override
    public Long getTimeout(String key) {
        return redisTemplate.getExpire(key, TimeUnit.SECONDS);
    }

    @Override
    public void updateTimeout(String key, long timeout) {
        long remain = getTimeout(key);
        if (remain == NOT_DATA_EXPIRE) {
            return;
        }
        timeout = Math.min(remain, timeout);
        if (timeout > 0) {
            redisTemplate.expire(key, timeout, TimeUnit.SECONDS);
        }
    }

    @Override
    public void deleteMatch(String pattern) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys != null && !keys.isEmpty()) {
            redisTemplate.delete(keys);
        }
    }

    @Override
    public SaSession getSession(String id) {
        String key = SESSION + id;
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        return sessionSerializer.deserialize(json.getBytes());
    }

    @Override
    public void setSession(SaSession saSession) {
        String key = SESSION + saSession.getId();
        byte[] value = sessionSerializer.serialize(saSession);
        redisTemplate.opsForValue().set(key, new String(value));
    }

    @Override
    public void updateSession(String id) {
        // Redis 不需要额外操作
    }

    @Override
    public long searchData(String pattern, int start, int size, String sortBy) {
        Set<String> keys = redisTemplate.keys(pattern);
        if (keys == null || keys.isEmpty()) {
            return 0;
        }
        return keys.size();
    }

    @Override
    public <T> T getObject(String key) {
        String json = redisTemplate.opsForValue().get(key);
        if (json == null) {
            return null;
        }
        return (T) objectSerializer.deserialize(json.getBytes());
    }

    @Override
    public void setObject(String key, Object object, long timeout) {
        byte[] value = objectSerializer.serialize(object);
        String json = new String(value);
        if (timeout == NOT_DATA_EXPIRE) {
            redisTemplate.opsForValue().set(key, json);
        } else {
            redisTemplate.opsForValue().set(key, json, timeout, TimeUnit.SECONDS);
        }
    }
}
