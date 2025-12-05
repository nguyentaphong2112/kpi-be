package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.hbtplus.models.response.ScopeResponse;
import vn.hbtplus.repositories.impl.ScopeRepository;
import vn.hbtplus.services.ScopeService;

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
