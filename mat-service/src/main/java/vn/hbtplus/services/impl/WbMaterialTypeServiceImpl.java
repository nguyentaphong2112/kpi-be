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
import vn.hbtplus.models.request.WbMaterialTypeRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.models.response.WbMaterialTypeResponse;
import vn.hbtplus.repositories.entity.WbMaterialTypeEntity;
import vn.hbtplus.repositories.impl.WbMaterialTypeRepository;
import vn.hbtplus.repositories.jpa.WbMaterialTypeRepositoryJPA;
import vn.hbtplus.services.WbMaterialTypeService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class WbMaterialTypeServiceImpl implements WbMaterialTypeService {
	
    private final WbMaterialTypeRepository wbMaterialTypeRepository;
    private final WbMaterialTypeRepositoryJPA wbMaterialTypeRepositoryJPA;
    
    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<WbMaterialTypeResponse> searchData(WbMaterialTypeRequest.SearchForm dto) {
        return ResponseUtils.ok(wbMaterialTypeRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(WbMaterialTypeRequest.SubmitForm dto) throws BaseAppException {
        WbMaterialTypeEntity entity;
        boolean isDuplicate = wbMaterialTypeRepository.duplicate(WbMaterialTypeEntity.class, dto.getWbMaterialTypeId(), "name", dto.getName());
        if (isDuplicate) {
            throw new BaseAppException(HttpStatus.BAD_REQUEST, Constants.ERROR_CODE.RECORD_CONFLICT, "Đã tồn tại tên kiểu vật tư");
        }

        boolean isDuplicateCode = wbMaterialTypeRepository.duplicate(WbMaterialTypeEntity.class, dto.getWbMaterialTypeId(), "code", dto.getCode());
        if (isDuplicateCode) {
            throw new BaseAppException(HttpStatus.BAD_REQUEST, Constants.ERROR_CODE.RECORD_CONFLICT, "Đã tồn tại mã kiểu vật tư");
        }

        if (dto.getWbMaterialTypeId() != null && dto.getWbMaterialTypeId() > 0L) {
            entity = wbMaterialTypeRepositoryJPA.getById(dto.getWbMaterialTypeId());
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new WbMaterialTypeEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        wbMaterialTypeRepositoryJPA.save(entity);
        return ResponseUtils.ok(entity.getWbMaterialTypeId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<WbMaterialTypeEntity> optional = wbMaterialTypeRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WbMaterialTypeEntity.class);
        }
        wbMaterialTypeRepository.deActiveObject(WbMaterialTypeEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<WbMaterialTypeResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<WbMaterialTypeEntity> optional = wbMaterialTypeRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, WbMaterialTypeEntity.class);
        }
        WbMaterialTypeResponse dto = new WbMaterialTypeResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public List<WbMaterialTypeResponse> getAll() {
        return wbMaterialTypeRepository.getAll();
    }
}
