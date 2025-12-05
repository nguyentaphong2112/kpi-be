package vn.hbtplus.controllers;

import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import vn.hbtplus.annotations.HasPermission;
import vn.hbtplus.annotations.Resource;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Scope;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.request.WbMaterialTypeRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WbMaterialTypeResponse;
import vn.hbtplus.services.WbMaterialTypeService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.MATERIAL_TYPE)
public class WbMaterialTypeController {
	private final WbMaterialTypeService wbMaterialTypeService;

	@GetMapping(value = "/v1/material-type", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.VIEW)
	public TableResponseEntity<WbMaterialTypeResponse> searchData(WbMaterialTypeRequest.SearchForm dto) {
		return wbMaterialTypeService.searchData(dto);
	}

	@PostMapping(value = "/v1/material-type", consumes = { MediaType.APPLICATION_JSON_VALUE })
	@HasPermission(scope = Scope.CREATE)
	public ResponseEntity saveData(@RequestBody @Valid WbMaterialTypeRequest.SubmitForm dto) throws BaseAppException {
		return wbMaterialTypeService.saveData(dto);
	}

	@DeleteMapping(value = "/v1/material-type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.DELETE)
	public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
		return wbMaterialTypeService.deleteData(id);
	}

	@GetMapping(value = "/v1/material-type/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.VIEW)
	public BaseResponseEntity<WbMaterialTypeResponse> getDataById(@PathVariable Long id)
			throws RecordNotExistsException {
		return wbMaterialTypeService.getDataById(id);
	}

	@GetMapping(value = "/v1/material-type/all", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.VIEW)
	public ListResponseEntity<WbMaterialTypeResponse> getAllWbMaterialType() throws Exception {
		return ResponseUtils.ok(wbMaterialTypeService.getAll());
	}
}
