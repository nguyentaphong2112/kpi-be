package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constant.Constants;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.request.WbMaterialRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WbMaterialResponse;
import vn.hbtplus.repositories.entity.WbMaterialEntity;
import vn.hbtplus.repositories.impl.WbMaterialRepository;
import vn.hbtplus.repositories.jpa.WbMaterialRepositoryJPA;
import vn.hbtplus.services.WbMaterialService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WbMaterialServiceImpl implements WbMaterialService {
	private final WbMaterialRepository wbMaterialRepository;
	private final WbMaterialRepositoryJPA wbMaterialRepositoryJPA;

	@Override
	@Transactional(readOnly = true)
	public TableResponseEntity<WbMaterialResponse> searchData(WbMaterialRequest.SearchForm dto) {
//		List<WbCategoryEntity> listCompany = wbCategoryRepository.findAllByProperties(WbCategoryEntity.class, "typeCode", "COMPANY");
//		List<WbCategoryEntity> listCountry = wbCategoryRepository.findAllByProperties(WbCategoryEntity.class, "typeCode", "COUNTRY");
//		List<WbCategoryEntity> listUnit = wbCategoryRepository.findAllByProperties(WbCategoryEntity.class, "typeCode", "UNIT");
		BaseDataTableDto<WbMaterialResponse> table = wbMaterialRepository.searchData(dto);
		table.getListData().forEach(e -> {
//			listCompany.forEach(item -> {
//				if(item.getValue().equals(e.getCompanyId())) {
//					e.setCompanyName(item.getValueName());
//				}
//			});
//			listCountry.forEach(item -> {
//				if(item.getValue().equals(e.getCountryId())) {
//					e.setCountryName(item.getValueName());
//				}
//			});
//			listUnit.forEach(item -> {
//				if(item.getValue().equals(e.getUnitId())) {
//					e.setUnitName(item.getValueName());
//				}
//			});
		});
		return ResponseUtils.ok(table);
	}

	@Override
	@Transactional
	public ResponseEntity saveData(WbMaterialRequest.SubmitForm dto) throws BaseAppException {
		WbMaterialEntity entity;
		boolean isDuplicate = wbMaterialRepository.duplicate(WbMaterialEntity.class, dto.getWbMaterialId(), "name",
				dto.getName());
		if (isDuplicate) {
			throw new BaseAppException(HttpStatus.BAD_REQUEST, Constants.ERROR_CODE.RECORD_CONFLICT,
					"Đã tồn tại tên vật tư");
		}

		if (dto.getWbMaterialId() != null && dto.getWbMaterialId() > 0L) {
			entity = wbMaterialRepositoryJPA.getById(dto.getWbMaterialId());
			entity.setModifiedTime(new Date());
			entity.setModifiedBy(Utils.getUserNameLogin());
		} else {
			entity = new WbMaterialEntity();
			entity.setCode(getCode());
			entity.setCreatedTime(new Date());
			entity.setCreatedBy(Utils.getUserNameLogin());
		}
		Utils.copyProperties(dto, entity);
		entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
		wbMaterialRepositoryJPA.save(entity);
		return ResponseUtils.ok(entity.getWbMaterialId());
	}

	@Override
	@Transactional
	public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
		Optional<WbMaterialEntity> optional = wbMaterialRepositoryJPA.findById(id);
		if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
			throw new RecordNotExistsException(id, WbMaterialEntity.class);
		}
		wbMaterialRepository.deActiveObject(WbMaterialEntity.class, id);
		return ResponseUtils.ok(id);
	}

	@Override
	@Transactional(readOnly = true)
	public BaseResponseEntity<WbMaterialResponse> getDataById(Long id) throws RecordNotExistsException {
		Optional<WbMaterialEntity> optional = wbMaterialRepositoryJPA.findById(id);
		if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
			throw new RecordNotExistsException(id, WbMaterialEntity.class);
		}
		WbMaterialResponse dto = new WbMaterialResponse();
		Utils.copyProperties(optional.get(), dto);
		return ResponseUtils.ok(dto);
	}

	@Override
	@Transactional(readOnly = true)
	public List<WbMaterialResponse> getAll() {
		return wbMaterialRepository.getAll();
	}

	@Override
	@Transactional(readOnly = true)
	public String getCode() {
		WbMaterialEntity entity = wbMaterialRepositoryJPA.findFirstByOrderByWbMaterialIdDesc();
		Long number = Long.parseLong(entity.getCode());
		number++;
		String str = number.toString();
		while(str.length() < 6) {
			str = "0" + str;
		}
		return str;
	}
}
