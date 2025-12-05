package vn.hbtplus.tax.income.services;

import vn.hbtplus.tax.income.models.response.IncomeTemplatesResponse;

import java.util.List;

public interface IncomeTemplateService {
    List<IncomeTemplatesResponse> getAll();
}
