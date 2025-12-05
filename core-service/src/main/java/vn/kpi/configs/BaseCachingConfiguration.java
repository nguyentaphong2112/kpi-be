package vn.kpi.configs;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurerSupport;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.interceptor.CacheErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import vn.kpi.configs.cache.ToggleableCacheManager;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
@EnableCaching
public class BaseCachingConfiguration extends CachingConfigurerSupport {
    public final static String ADMIN_MENU = "ADMIN_MENU";
    public final static String ADMIN_CATEGORY = "ADMIN_CATEGORY";
    public final static String ADMIN_CATEGORY_TYPE = "ADMIN_CATEGORY_TYPE";
    public final static String HR_SALARY_RANKS = "HR_SALARY_RANKS";
    public final static String HR_SALARY_GRADES = "HR_SALARY_GRADES";

    public final static String ADMIN_USER_ROLE = "ADMIN_USER_ROLE";
    public final static String AUTHORIZATION = "HCM-AUTHORIZATION";
    private final RedisConnectionFactory redisConnectionFactory;

    @Value("${spring.redis.host:N/A}")
    private String redisHost;

    @Bean
    public CacheManager cacheManager() {

        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1)); // TTL mặc định

        RedisCacheManager delegateManager = RedisCacheManager.RedisCacheManagerBuilder.fromConnectionFactory(redisConnectionFactory)
                .cacheDefaults(defaultConfig)
                .withCacheConfiguration(ADMIN_MENU, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(6L)))
                .withCacheConfiguration(ADMIN_CATEGORY, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(6L)))
                .withCacheConfiguration(ADMIN_CATEGORY_TYPE, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1L)))
                .withCacheConfiguration(ADMIN_USER_ROLE, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1L)))
                .withCacheConfiguration(HR_SALARY_RANKS, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1L)))
                .withCacheConfiguration(HR_SALARY_GRADES, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1L)))
                .withCacheConfiguration(AUTHORIZATION, RedisCacheConfiguration.defaultCacheConfig().entryTtl(Duration.ofHours(1L)))
                .build();
        return new ToggleableCacheManager(delegateManager, () -> !"N/A".equalsIgnoreCase(redisHost));
    }

    @Bean
    public CacheErrorHandler errorHandler() {
        return new IgnoreExceptionCacheErrorHandler();
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        return redisTemplate;
    }
    @Bean
    public RedisCacheConfiguration cacheConfiguration() {
        return RedisCacheConfiguration.defaultCacheConfig()
                .serializeValuesWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer())
                );
    }
}
