package vn.hbtplus.configs;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Data
public class FontConfig {
    @Value("${font.time:-}")
    private String times;

    @Value("${font.time.bold:-}")
    private String timeBold;

    @Value("${font.time.italic:-}")
    private String timeItalic;
}
