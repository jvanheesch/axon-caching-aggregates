package io.github.jvanheesch.axon.aggregate.caching;

import net.sf.ehcache.Ehcache;
import org.axonframework.common.caching.EhCacheAdapter;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.context.annotation.Bean;

@EnableCaching
@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public EhCacheAdapter axonCache(EhCacheCacheManager ehCacheCacheManager) {
        Object sampleCache2 = ehCacheCacheManager.getCache("sampleCache2").getNativeCache();
        return new EhCacheAdapter((Ehcache) sampleCache2);
    }
}
