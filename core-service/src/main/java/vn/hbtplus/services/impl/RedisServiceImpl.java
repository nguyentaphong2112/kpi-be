package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import vn.hbtplus.configs.cache.ToggleableCache;
import vn.hbtplus.services.RedisService;

import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisServiceImpl implements RedisService {

    private final CacheManager cacheManager;
    private final RedisTemplate<String, String> redisTemplate;

    @Override
    public void deleteCacheByName(String cacheName, String cacheKey) {
        if (StringUtils.isBlank(cacheKey)) {
            clearCache(cacheName);
        } else {
            evictCache(cacheName, cacheKey);
        }
    }


    public String update(String cacheNames, String key, String value) {
        Cache cache = cacheManager.getCache(cacheNames);
        cache.put(key, value);
        return value;
    }

    public String getValue(String cacheName, String key) {
        Cache cache = cacheManager.getCache(cacheName);
        return (String) cache.get(key).get();
    }

    public Set<String> getAllKeys(String cacheName) {
        String pattern = cacheName + ":*";  // Adjust the pattern based on your cache key structure
        return redisTemplate.keys(pattern);
    }

    public void clearCache(String cacheName) {
        ToggleableCache cache = (ToggleableCache) cacheManager.getCache(cacheName);
        if (cache != null && cache.isEnabled()) {
            cache.clear();
        }
    }

    public void evictCacheByPrefix(String cacheName, String keyPrefix) {
        ToggleableCache cache = (ToggleableCache) cacheManager.getCache(cacheName);
        if (cache != null && cache.isEnabled()) {
            String pattern = cacheName + "::" + keyPrefix + "*"; // build pattern chuẩn
            Set<String> keys = redisTemplate.keys(pattern);
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
            }
        }
    }

    public void evictCache(String cacheName, String cacheKey) {
        ToggleableCache cache = (ToggleableCache) cacheManager.getCache(cacheName);

        if (cache != null && cache.isEnabled()) {
            cache.evict(cacheKey);
        }
    }
}
