package vn.hbtplus.tax.income.services;

import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.tax.income.models.request.TaxCommitmentRequest;
import vn.hbtplus.tax.income.models.response.TaxCommitmentResponse;

public interface TaxCommitmentService {
    BaseDataTableDto<TaxCommitmentResponse> searchData(TaxCommitmentRequest.SearchForm dto);

    Long deleteById(Long id) throws Exception;

    Long saveTaxCommitment(TaxCommitmentRequest.UpdateForm form) throws BaseAppException, Exception;

    TaxCommitmentResponse getTaxCommitmentById(Long id) throws Exception;

    void importTaxCommitment(MultipartFile fileImport) throws Exception;

    ResponseEntity<Object> exportData(TaxCommitmentRequest.SearchForm dto) throws Exception;

    ResponseEntity<Object> downloadTemplate() throws Exception;
}
