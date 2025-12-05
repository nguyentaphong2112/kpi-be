package vn.hbtplus.services;

import org.springframework.http.ResponseEntity;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.WbMaterialRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WbMaterialResponse;

import java.util.List;

public interface WbMaterialService {

	TableResponseEntity<WbMaterialResponse> searchData(WbMaterialRequest.SearchForm dto);

	ResponseEntity saveData(WbMaterialRequest.SubmitForm dto) throws BaseAppException;

	ResponseEntity deleteData(Long id) throws RecordNotExistsException;

	BaseResponseEntity<WbMaterialResponse> getDataById(Long id) throws RecordNotExistsException;

	List<WbMaterialResponse> getAll();
	
	String getCode();
}
