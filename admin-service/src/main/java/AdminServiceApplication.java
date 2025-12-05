import net.javacrumbs.shedlock.core.LockProvider;
import net.javacrumbs.shedlock.provider.jdbctemplate.JdbcTemplateLockProvider;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import vn.kpi.annotations.EnableJasypt;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@ComponentScan(basePackages = {"vn.kpi"})
@EnableJpaRepositories(basePackages = "vn.kpi")
@EntityScan("vn.hbtplus")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableFeignClients(basePackages = "vn.kpi")
@EnableJasypt(key = "vcc@#1232023")
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "20m")
public class AdminServiceApplication {

    @Value("${server.timezone}")
    protected String severTimezone;
    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(severTimezone)));
    }

    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

    public static void main(String[] args) {
        SpringApplication.run(AdminServiceApplication.class, args);
    }
}
