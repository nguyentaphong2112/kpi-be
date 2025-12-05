package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryTypeDto;
import vn.hbtplus.models.request.CategoryTypeRequest;
import vn.hbtplus.models.response.CategoryTypeResponse;
import vn.hbtplus.repositories.entity.CategoryTypesEntity;
import vn.hbtplus.repositories.impl.CategoryTypeRepository;
import vn.hbtplus.repositories.jpa.CategoryTypesRepositoryJPA;
import vn.hbtplus.services.CategoryTypeService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CategoryTypeServiceImpl implements CategoryTypeService {
    private final CategoryTypeRepository categoryTypeRepository;
    private final CategoryTypesRepositoryJPA categoryTypesRepositoryJPA;
    @Override
    public List<CategoryTypeDto> getListCategoryType(String groupType) {
        return categoryTypeRepository.getListCategoryType(groupType);
    }

    @Override
    @Transactional(readOnly = true)
//    @Cacheable(cacheNames = BaseCachingConfiguration.ADMIN_CATEGORY_TYPE, key = "#categoryType", condition = "#categoryType != null")
    public CategoryTypeDto getCategoryType(String categoryType) {
        return categoryTypeRepository.getCategoryType(categoryType);
    }

    @Override
    public BaseDataTableDto<CategoryTypeResponse.SearchResult> searchData(CategoryTypeRequest.SearchForm dto) {
        return categoryTypeRepository.searchData(dto);
    }

    @Override
    public CategoryTypeResponse.DetailBean getDataById(Long id) {
        CategoryTypesEntity categoryTypesEntity = categoryTypeRepository.get(CategoryTypesEntity.class, id);
        if (categoryTypesEntity == null || StringUtils.endsWithIgnoreCase(categoryTypesEntity.getIsDeleted(), BaseConstants.STATUS.DELETED)) {
            throw new RecordNotExistsException(id, CategoryTypesEntity.class);
        }
        CategoryTypeResponse.DetailBean result = new CategoryTypeResponse.DetailBean();
        Utils.copyProperties(categoryTypesEntity, result);
        if (!Utils.isNullOrEmpty(result.getAttributes())){
            result.setListAttributes(Utils.fromJsonList(result.getAttributes(), CategoryTypeDto.AttributeDto.class)) ;
        }
        return result;
    }

    @Override
    public ResponseEntity saveData(CategoryTypeRequest.SubmitForm dto, Long categoryTypeId) {
        if (categoryTypeRepository.duplicate(CategoryTypesEntity.class, categoryTypeId, "code", dto.getCode())) {
            throw new BaseAppException("ERROR_CATEGORY_TYPE_CODE_DUPLICATE", I18n.getMessage("error.categoryType.duplicateCode"));
        }

        if (categoryTypeRepository.duplicate(CategoryTypesEntity.class, categoryTypeId, "name", dto.getName())) {
            throw new BaseAppException("ERROR_CATEGORY_TYPE_NAME_DUPLICATE", I18n.getMessage("error.categoryType.duplicateName"));
        }

        CategoryTypesEntity entity;
        Date curDate = new Date();
        String userName = Utils.getUserNameLogin();
        if (categoryTypeId != null && categoryTypeId > 0L) {
            entity = categoryTypesRepositoryJPA.getById(categoryTypeId);
            entity.setModifiedTime(curDate);
            entity.setModifiedBy(userName);
        } else {
            entity = new CategoryTypesEntity();
            entity.setCreatedTime(curDate);
            entity.setCreatedBy(userName);
        }
        Utils.copyProperties(dto, entity);
        entity.setAttributes(Utils.toJson(dto.getAttributes()));
        categoryTypesRepositoryJPA.save(entity);

        return ResponseUtils.ok(entity.getCategoryTypeId());
    }

    @Override
    public ResponseEntity<Object> exportData(CategoryTypeRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/loai_danh_muc.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = categoryTypeRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "loai_danh_muc.xlsx");
    }

    @Override
    public ResponseEntity deleteData(Long id) {
        Optional<CategoryTypesEntity> optional = categoryTypesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CategoryTypesEntity.class);
        }
        categoryTypeRepository.deActiveObject(CategoryTypesEntity.class, id);
        return ResponseUtils.ok(id);
    }
}
