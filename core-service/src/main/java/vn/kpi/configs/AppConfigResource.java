package vn.kpi.configs;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ResourceBundleMessageSource;

import java.util.concurrent.ForkJoinPool;

@Configuration
@Data
public class AppConfigResource {
    @Value("${service.white-list.ignore-token.urls:}")
    String urlIgnoreToken;

    @Value("${app.security.sso-enabled:false}")
    Boolean ssoEnabled;


    @Bean(name = "messageSource")
    public ResourceBundleMessageSource messageSource() {
        ResourceBundleMessageSource source = new ResourceBundleMessageSource();
        source.setBasenames("messages", "language");
        source.setDefaultEncoding("UTF-8");
        source.setUseCodeAsDefaultMessage(true);
        return source;
    }

    @Bean(name = "mdcForkJoinPool")
    public MdcForkJoinPool mdcForkJoinPool() {
        return new MdcForkJoinPool(10, ForkJoinPool.defaultForkJoinWorkerThreadFactory, null, true);
    }
}
