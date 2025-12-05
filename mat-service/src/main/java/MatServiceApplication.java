import com.aspose.words.FontSettings;
import net.javacrumbs.shedlock.spring.annotation.EnableSchedulerLock;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import vn.hbtplus.annotations.EnableJasypt;

import javax.annotation.PostConstruct;
import java.time.ZoneId;
import java.util.TimeZone;

@SpringBootApplication
@ComponentScan(basePackages = {"vn.hbtplus"})
@EnableJpaRepositories(basePackages = "vn.hbtplus")
@EntityScan("vn.hbtplus")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableFeignClients(basePackages = "vn.hbtplus")
@EnableJasypt(key = "vcc@#1232023")
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "20m")
public class MatServiceApplication {

    @Value("${server.timezone}")
    protected String severTimezone;

    private static String fontFolder;
    @Value("${report.fontFolder}")
    public void setFontFolder(String fontFolder) {
        MatServiceApplication.fontFolder = fontFolder;
    }

    @PostConstruct
    void init() {
        TimeZone.setDefault(TimeZone.getTimeZone(ZoneId.of(severTimezone)));
    }

    public static void main(String[] args) {
        SpringApplication.run(MatServiceApplication.class, args);
        FontSettings.setFontsFolder(fontFolder, false);
    }
}
