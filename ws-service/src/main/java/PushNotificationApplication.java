import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.EnableAspectJAutoProxy;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import vn.hbtplus.annotations.EnableJasypt;

@SpringBootApplication
@ComponentScan(basePackages = {"vn.hbtplus"})
@EnableJpaRepositories(basePackages = "vn.hbtplus")
@EntityScan("vn.hbtplus")
@EnableAspectJAutoProxy(proxyTargetClass = true)
@EnableFeignClients(basePackages = "vn.hbtplus")
@EnableJasypt(key = "vcc@#1232023")
@EnableScheduling
public class PushNotificationApplication {

	public static void main(String[] args) {
		SpringApplication.run(PushNotificationApplication.class, args);
	}

}
