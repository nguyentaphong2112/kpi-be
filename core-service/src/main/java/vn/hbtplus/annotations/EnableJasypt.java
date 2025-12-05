package vn.hbtplus.annotations;

import org.springframework.context.annotation.Import;
import vn.hbtplus.configs.EnableJasyptImportSelector;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
@Documented
@Import({EnableJasyptImportSelector.class})
public @interface EnableJasypt {
    String key() default "password";
}
