package vn.hbtplus.configs;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.Cache;
import org.springframework.cache.interceptor.CacheErrorHandler;

public class IgnoreExceptionCacheErrorHandler implements CacheErrorHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(IgnoreExceptionCacheErrorHandler.class);

    public IgnoreExceptionCacheErrorHandler() {
        super();
    }

    @Override
    public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
        LOGGER.error(exception.getMessage(), exception);
    }

    @Override
    public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
        LOGGER.error(exception.getMessage(), exception);
    }

    @Override
    public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
        LOGGER.error(exception.getMessage(), exception);
    }

    @Override
    public void handleCacheClearError(RuntimeException exception, Cache cache) {
        LOGGER.error(exception.getMessage(), exception);
    }
}
