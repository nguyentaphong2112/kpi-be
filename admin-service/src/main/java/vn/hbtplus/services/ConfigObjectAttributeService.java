package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.ConfigObjectAttributeRequest;
import vn.hbtplus.models.response.ConfigObjectAttributeResponse;

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
