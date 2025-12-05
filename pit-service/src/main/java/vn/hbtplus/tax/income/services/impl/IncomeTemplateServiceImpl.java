package vn.hbtplus.tax.income.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.tax.income.models.response.IncomeTemplatesResponse;
import vn.hbtplus.tax.income.repositories.impl.IncomeTemplateRepository;
import vn.hbtplus.tax.income.services.IncomeTemplateService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IncomeTemplateServiceImpl implements IncomeTemplateService {

    private final IncomeTemplateRepository incomeTemplateRepository;

    @Override
    @Transactional(readOnly = true)
    public List<IncomeTemplatesResponse> getAll() {
        return incomeTemplateRepository.getAll();
    }
}
