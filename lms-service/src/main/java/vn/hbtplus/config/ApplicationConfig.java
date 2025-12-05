package vn.hbtplus.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class ApplicationConfig {

    @Value("${contractProcess.contractType.pause}")
    private String contractTypePause;

}
