package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.WbMaterialTypeRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WbMaterialTypeResponse;

import java.util.List;

public interface WbMaterialTypeService {
	
	TableResponseEntity<WbMaterialTypeResponse> searchData(WbMaterialTypeRequest.SearchForm dto);

	ResponseEntity saveData(WbMaterialTypeRequest.SubmitForm dto) throws BaseAppException;

	ResponseEntity deleteData(Long id) throws RecordNotExistsException;

	BaseResponseEntity<WbMaterialTypeResponse> getDataById(Long id) throws RecordNotExistsException;

	List<WbMaterialTypeResponse> getAll();
	
}
