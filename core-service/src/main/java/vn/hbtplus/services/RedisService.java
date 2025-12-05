package vn.hbtplus.services;

import java.util.Set;

public interface RedisService {
    void deleteCacheByName(String cacheName, String cacheKey);
    void evictCacheByPrefix(String cacheName, String keyPrefix);
    String update(String cacheNames, String key, String value);
    String getValue(String cacheName, String key);
    Set<String> getAllKeys(String cacheName);
    void clearCache(String cacheName);
    void evictCache(String cacheName, String cacheKey);
}
