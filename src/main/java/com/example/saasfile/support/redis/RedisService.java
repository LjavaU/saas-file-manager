package com.example.saasfile.support.redis;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    private final ObjectProvider<StringRedisTemplate> redisTemplateProvider;
    private final Map<String, Map<String, String>> localStore = new ConcurrentHashMap<>();

    public RedisService(ObjectProvider<StringRedisTemplate> redisTemplateProvider) {
        this.redisTemplateProvider = redisTemplateProvider;
    }

    public Map<String, String> hGetAll(String key) {
        StringRedisTemplate template = redisTemplateProvider.getIfAvailable();
        if (template != null) {
            HashOperations<String, Object, Object> hashOps = template.opsForHash();
            Map<Object, Object> data = hashOps.entries(key);
            Map<String, String> result = new LinkedHashMap<>();
            data.forEach((k, v) -> result.put(String.valueOf(k), v == null ? null : String.valueOf(v)));
            return result;
        }
        return new LinkedHashMap<>(localStore.getOrDefault(key, Collections.emptyMap()));
    }

    public void hSet(String key, String hashKey, String value, long seconds) {
        StringRedisTemplate template = redisTemplateProvider.getIfAvailable();
        if (template != null) {
            template.opsForHash().put(key, hashKey, value);
            template.expire(key, seconds, TimeUnit.SECONDS);
            return;
        }
        localStore.computeIfAbsent(key, ignored -> new ConcurrentHashMap<>()).put(hashKey, value);
    }

    public void hDel(String key, Object... hashKeys) {
        StringRedisTemplate template = redisTemplateProvider.getIfAvailable();
        if (template != null) {
            template.opsForHash().delete(key, hashKeys);
            return;
        }
        Map<String, String> bucket = localStore.get(key);
        if (bucket == null) {
            return;
        }
        for (Object hashKey : hashKeys) {
            bucket.remove(String.valueOf(hashKey));
        }
    }
}
