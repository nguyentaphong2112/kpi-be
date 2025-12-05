package vn.kpi.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.kpi.models.response.ScopeResponse;
import vn.kpi.repositories.impl.ScopeRepository;
import vn.kpi.services.ScopeService;

import java.util.List;

@Service
@RequiredArgsConstructor

public class ScopeServiceImpl implements ScopeService {
    private final ScopeRepository scopeRepository;

    @Override
    public List<ScopeResponse> getScopes() {
        return scopeRepository.getScopes();
    }
}
