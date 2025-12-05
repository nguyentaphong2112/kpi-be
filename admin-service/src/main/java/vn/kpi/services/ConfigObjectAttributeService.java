package vn.kpi.services;

import org.springframework.http.ResponseEntity;
import vn.kpi.exceptions.BaseAppException;
import vn.kpi.exceptions.RecordNotExistsException;
import vn.kpi.models.BaseDataTableDto;
import vn.kpi.models.request.ConfigObjectAttributeRequest;
import vn.kpi.models.response.ConfigObjectAttributeResponse;

import java.util.List;

public interface ConfigObjectAttributeService {
    BaseDataTableDto searchData(ConfigObjectAttributeRequest.SearchForm dto);


    ResponseEntity deleteData(Long id) throws RecordNotExistsException;


    ResponseEntity saveData(ConfigObjectAttributeRequest.SubmitForm dto, Long configObjectAttributeId) throws BaseAppException;

    ConfigObjectAttributeResponse.DetailBean getDataById(Long id) throws RecordNotExistsException;

    List<ConfigObjectAttributeResponse.ListTableName> getListTableData();

    ConfigObjectAttributeResponse.SearchByTableName getByTableName(String tableName, String functionCode);

    List getAttributes(String tableName, String functionCode);

    List<ConfigObjectAttributeResponse> getListConfigByCodes(String attributeValue);
}
