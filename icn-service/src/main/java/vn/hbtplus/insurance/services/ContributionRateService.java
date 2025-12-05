package vn.hbtplus.insurance.services;

import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.insurance.models.request.ContributionRateRequest;
import vn.hbtplus.insurance.models.response.ContributionRateResponse;

public interface ContributionRateService {
    BaseDataTableDto<ContributionRateResponse> search(ContributionRateRequest.SearchForm request);

    Object exportData(ContributionRateRequest.SearchForm request);

    Long saveData(ContributionRateRequest.SubmitForm request, Long id) throws BaseAppException;

    Boolean deleteById(Long id) throws RecordNotExistsException;

    ContributionRateResponse getById(Long id) throws RecordNotExistsException;
}
