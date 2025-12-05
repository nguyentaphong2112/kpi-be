package vn.kpi.configs.cache;

import org.springframework.cache.Cache;
import org.springframework.cache.support.SimpleValueWrapper;

import java.util.concurrent.Callable;
import java.util.function.Supplier;

public class ToggleableCache implements Cache {

    private final Cache delegate;
    private final Supplier<Boolean> enabled;

    public ToggleableCache(Cache delegate, Supplier<Boolean> enabled) {
        this.delegate = delegate;
        this.enabled = enabled;
    }

    @Override
    public String getName() {
        return delegate.getName();
    }

    @Override
    public Object getNativeCache() {
        return delegate.getNativeCache();
    }

    public boolean isEnabled() {
        return enabled.get();
    }

    @Override
    public ValueWrapper get(Object key) {
        if (!enabled.get()) {
            return null;
        }
        return delegate.get(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T get(Object key, Class<T> type) {
        if (!enabled.get()) {
            return null;
        }
        return delegate.get(key, type);
    }

    @Override
    public <T> T get(Object key, Callable<T> valueLoader) {
        if (!enabled.get()) {
            try {
                return valueLoader.call();
            } catch (Exception e) {
                throw new RuntimeException("Cache is disabled and valueLoader threw exception", e);
            }
        }
        return delegate.get(key, valueLoader);
    }

    @Override
    public void put(Object key, Object value) {
        if (!enabled.get()) {
            return;
        }
        delegate.put(key, value);
    }

    @Override
    public ValueWrapper putIfAbsent(Object key, Object value) {
        if (!enabled.get()) {
            return new SimpleValueWrapper(value);
        }
        return delegate.putIfAbsent(key, value);
    }

    @Override
    public void evict(Object key) {
        if (enabled.get()) {
            delegate.evict(key);
        }
    }

    @Override
    public void clear() {
        if (enabled.get()) {
            delegate.clear();
        }
    }
}
