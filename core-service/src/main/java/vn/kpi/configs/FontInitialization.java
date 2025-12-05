package vn.kpi.configs;

import com.aspose.words.FontSettings;
import com.aspose.words.FontSourceBase;
import com.aspose.words.MemoryFontSource;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
@RequiredArgsConstructor
public class FontInitialization implements CommandLineRunner {
    private final ResourcePatternResolver resourceLoader;

    @Override
    public void run(String... args) throws Exception {
        initFontSources();
    }

    public void initFontSources() throws IOException {
        List<MemoryFontSource> inputStreams = new ArrayList<>();
        Resource[] resources = getResources("fonts");

        for (Resource resource : resources) {
            MemoryFontSource fontSource = new MemoryFontSource(resource.getInputStream().readAllBytes());
            inputStreams.add(fontSource);
        }
        FontSettings.setFontsSources(inputStreams.toArray(new FontSourceBase[]{}));
    }

    private Resource[] getResources(String folderPath) throws IOException {
        return resourceLoader.getResources("classpath:" + folderPath + "/*");
    }
}
