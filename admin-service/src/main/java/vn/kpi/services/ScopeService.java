package vn.kpi.services;

import vn.kpi.models.response.ScopeResponse;

import java.util.List;

public interface ScopeService {
    List<ScopeResponse> getScopes();
}
