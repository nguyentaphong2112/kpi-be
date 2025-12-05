package vn.kpi.configs;

import org.springframework.context.annotation.ImportSelector;
import org.springframework.core.type.AnnotationMetadata;
import vn.kpi.annotations.EnableJasypt;

import java.util.Map;

public class EnableJasyptImportSelector implements ImportSelector {
    public EnableJasyptImportSelector() {
    }

    public String[] selectImports(AnnotationMetadata annotationMetadata) {
        Map<String, Object> attributes = annotationMetadata.getAnnotationAttributes(EnableJasypt.class.getName(), true);
        String password = (String)attributes.get("key");
        System.setProperty("hbt-jasypt-pwd", password);
        return new String[0];
    }
}
