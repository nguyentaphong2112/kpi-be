import com.aspose.cells.CellsHelper;
import com.aspose.words.FontSettings;
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
import vn.hbtplus.annotations.EnableJasypt;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;
import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@ComponentScan(basePackages = {"vn.hbtplus"})
@EnableJpaRepositories(basePackages = "vn.hbtplus")
@EntityScan("vn.hbtplus")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableFeignClients(basePackages = "vn.hbtplus.feigns")
@EnableJasypt(key = "vcc@#1232023")
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "20m")
public class CrmServiceApplication {

    @Value("${server.timezone}")
    protected String severTimezone;

    private static String fontFolder;
    @Value("${report.fontFolder}")
    public void setFontFolder(String fontFolder) {
        CrmServiceApplication.fontFolder = fontFolder;
    }

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(severTimezone)));
    }


    @Bean
    public LockProvider lockProvider(DataSource dataSource) {
        return new JdbcTemplateLockProvider(dataSource);
    }

    public static void main(String[] args) {
        SpringApplication.run(CrmServiceApplication.class, args);
        FontSettings.setFontsFolder(fontFolder, false);
        CellsHelper.setFontDir(fontFolder);
    }
}
