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
import vn.hbtplus.models.request.WbMaterialRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.ListResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WbMaterialResponse;
import vn.hbtplus.services.WbMaterialService;
import vn.hbtplus.utils.ResponseUtils;

import javax.validation.Valid;

@RestController
@RequestMapping(BaseConstants.REQUEST_MAPPING_PREFIX)
@RequiredArgsConstructor
//@Resource(value = Constants.RESOURCE.MATERIAL_TYPE)
public class WbMaterialController {
	private final WbMaterialService wbMaterialService;

	@GetMapping(value = "/v1/material", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.VIEW)
	public TableResponseEntity<WbMaterialResponse> searchData(WbMaterialRequest.SearchForm dto) {
		return wbMaterialService.searchData(dto);
	}

	@PostMapping(value = "/v1/material", consumes = { MediaType.APPLICATION_JSON_VALUE })
	@HasPermission(scope = Scope.CREATE)
	public ResponseEntity saveData(@RequestBody @Valid WbMaterialRequest.SubmitForm dto) throws BaseAppException {
		return wbMaterialService.saveData(dto);
	}

	@DeleteMapping(value = "/v1/material/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.DELETE)
	public ResponseEntity deleteData(@PathVariable Long id) throws RecordNotExistsException {
		return wbMaterialService.deleteData(id);
	}

	@GetMapping(value = "/v1/material/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.VIEW)
	public BaseResponseEntity<WbMaterialResponse> getDataById(@PathVariable Long id)
			throws RecordNotExistsException {
		return wbMaterialService.getDataById(id);
	}

	@GetMapping(value = "/v1/material/all", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.VIEW)
	public ListResponseEntity<WbMaterialResponse> getAllWbMaterialType() throws Exception {
		return ResponseUtils.ok(wbMaterialService.getAll());
	}
	
	@GetMapping(value = "/v1/material/get-code", produces = MediaType.APPLICATION_JSON_VALUE)
	@HasPermission(scope = Scope.VIEW)
	public ResponseEntity getCode() throws Exception {
		return ResponseUtils.ok(wbMaterialService.getCode());
	}
}
