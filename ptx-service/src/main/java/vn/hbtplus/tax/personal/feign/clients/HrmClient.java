package vn.hbtplus.tax.personal.feign.clients;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import vn.hbtplus.tax.personal.repositories.entity.HrDependentPersonsEntity;
import vn.hbtplus.tax.personal.repositories.entity.HrEmployeesEntity;

import java.util.List;

@FeignClient(value = "hrm-client", url = "${custom.properties.hrm-url}")
public interface HrmClient {
    @PostMapping(value = "/v1/employees/update-tax-info", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> updateEmployeeTaxInfo(@RequestHeader HttpHeaders httpHeaders,
                                                 @RequestBody List<HrEmployeesEntity> listSave);

    @PostMapping(value = "/v1/dependent-persons/save-batch", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Object> saveDependentPersons(@RequestHeader HttpHeaders httpHeaders,
                                                @RequestBody List<HrDependentPersonsEntity> listSave);
}
