package vn.kpi.configs;

import org.springframework.data.domain.AuditorAware;
import vn.kpi.utils.Utils;

import java.util.Optional;

public class SpringSecurityAuditorAware implements AuditorAware<String> {
    @Override
    public Optional<String> getCurrentAuditor() {
        return Optional.of(Utils.getUserNameLogin());
    }
}
