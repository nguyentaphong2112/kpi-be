package vn.kpi.configs.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import java.util.Collection;
import java.util.function.Supplier;

public class ToggleableCacheManager implements CacheManager {
    private final CacheManager delegate;
    private final Supplier<Boolean> enabled;

    public ToggleableCacheManager(CacheManager delegate, Supplier<Boolean> enabled) {
        this.delegate = delegate;
        this.enabled = enabled;
    }

    @Override
    public Cache getCache(String name) {
        Cache original = delegate.getCache(name);
        if (original == null) return null;
        return new ToggleableCache(original, enabled);
    }

    @Override
    public Collection<String> getCacheNames() {
        return delegate.getCacheNames();
    }
}
