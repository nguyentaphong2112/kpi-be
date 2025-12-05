/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.models.BaseDataTableDto;
import vn.hbtplus.models.dto.CategoryDto;
import vn.hbtplus.models.dto.CategoryTypeDto;
import vn.hbtplus.models.request.CategoryRequest;
import vn.hbtplus.models.response.CategoryResponse;
import vn.hbtplus.repositories.entity.CategoryAttributeEntity;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.EvaluationPeriodsEntity;
import vn.hbtplus.repositories.impl.CategoryRepository;
import vn.hbtplus.repositories.jpa.CategoryAttributeRepositoryJPA;
import vn.hbtplus.repositories.jpa.CategoryRepositoryJPA;
import vn.hbtplus.services.CategoryService;
import vn.hbtplus.services.CategoryTypeService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Lop impl service ung voi bang sys_categories
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoriesRepository;
    private final CategoryTypeService categoryTypeService;
    private final CategoryRepositoryJPA categoriesRepositoryJPA;
    private final CategoryAttributeRepositoryJPA categoryAttributeRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public BaseDataTableDto searchData(String categoryType, CategoryRequest.SearchForm dto) {
        return categoriesRepository.searchData(categoryType, dto);
    }

    @Override
    @Transactional
    public ResponseEntity saveData(String categoryType, CategoryRequest.SubmitForm dto, Long categoryId) throws BaseAppException {

        if (categoriesRepository.checkDuplicateValueCategory(categoryType, dto.getValue(), categoryId)) {
            throw new BaseAppException("ERROR_CATEGORY_VALUE_DUPLICATE", I18n.getMessage("error.category.duplicateCode"));
        }
        if (!Utils.isNullOrEmpty(dto.getCode()) && categoriesRepository.duplicate(CategoryEntity.class, categoryId, "code", dto.getCode(), "category_type", categoryType)) {
            throw new BaseAppException("ERROR_CATEGORY_CODE_DUPLICATE", I18n.getMessage("error.categories.code.duplicate"));
        }
//        boolean isDuplicateName = categoriesRepository.duplicate(CategoryEntity.class, categoryId, "name", dto.getName(), "category_type", categoryType);
//        if (isDuplicateName) {
//            throw new BaseAppException("ERROR_CATEGORY_NAME_DUPLICATE", I18n.getMessage("error.categories.name.duplicate"));
//        }

        CategoryTypeDto categoryTypeDto = categoryTypeService.getCategoryType(categoryType);
        if (categoryTypeDto == null) {
            throw new BaseAppException("ERROR_CATEGORY_TYPE", I18n.getMessage("error.category.categoryTypeInvalid"));
        }

        CategoryEntity entity;
        Date curDate = new Date();
        String userName = Utils.getUserNameLogin();
        if (categoryId != null && categoryId > 0L) {
            entity = categoriesRepositoryJPA.getById(categoryId);
            if (!categoryType.equals(entity.getCategoryType())) {
                throw new BaseAppException("ERROR_CATEGORY_TYPE", I18n.getMessage("error.category.categoryTypeInvalid"));
            }
            entity.setModifiedTime(curDate);
            entity.setModifiedBy(userName);
        } else {
            entity = new CategoryEntity();
            entity.setCreatedTime(curDate);
            entity.setCreatedBy(userName);
        }
        Utils.copyProperties(dto, entity);
        entity.setCategoryType(categoryType);
        if (BaseConstants.COMMON.YES.equalsIgnoreCase(categoryTypeDto.getIsAutoIncrease())
            && Utils.isNullOrEmpty(entity.getValue())
        ) {
            entity.setValue(categoriesRepository.getNextValueCategory(categoryType).toString());
        }
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        categoriesRepositoryJPA.save(entity);

        categoriesRepositoryJPA.flush();

        categoriesRepository.deleteAttributes(entity.getCategoryId());

        List<CategoryTypeDto.AttributeDto> attributeDtos = categoryTypeDto.getListAttributes();
        Map<String, CategoryTypeDto.AttributeDto> mapConfigs = new HashMap<>();
        attributeDtos.forEach(item -> {
            mapConfigs.put(item.getCode(), item);
        });

        if (!Utils.isNullOrEmpty(dto.getListAttributes())) {
            List<CategoryAttributeEntity> attributeEntityList = new ArrayList<>();
            for (CategoryRequest.AttributeDto attribute : dto.getListAttributes()) {
                CategoryTypeDto.AttributeDto configAttributeDto = mapConfigs.get(attribute.getAttributeCode());
                if (configAttributeDto == null) {
                    //loi attribute khong thuoc config
                    throw new BaseAppException("ERROR_CATEGORY_ATTRIBUTE_CODE", I18n.getMessage("error.category.attributeCodeInvalid"));
                }
                CategoryAttributeEntity attributeEntity;
                attributeEntity = new CategoryAttributeEntity();
                attributeEntity.setCreatedTime(curDate);
                attributeEntity.setCreatedBy(userName);
                Utils.copyProperties(attribute, attributeEntity);
                attributeEntity.setDataType(configAttributeDto.getDataType());
                attributeEntity.setCategoryId(entity.getCategoryId());
                attributeEntityList.add(attributeEntity);
            }
            categoryAttributeRepositoryJPA.saveAll(attributeEntityList);
        }
        categoriesRepositoryJPA.deleteOldValue(entity.getCategoryType(), entity.getValue());
        return ResponseUtils.ok(entity);
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<CategoryEntity> optional = categoriesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CategoryEntity.class);
        }
        categoriesRepository.deActiveObject(CategoryEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public CategoryResponse.DetailBean getDataById(Long id) throws RecordNotExistsException {
        Optional<CategoryEntity> optional = categoriesRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, CategoryEntity.class);
        }
        CategoryResponse.DetailBean dto = new CategoryResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        //Lay thong tin attributes
        dto.setListAttributes(categoriesRepository.getAttributeOfCategory(id));
        return dto;
    }

    @Override
    public ResponseEntity<Object> exportData(String categoryType, CategoryRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh-muc-dung-chung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = categoriesRepository.getListExport(categoryType, dto);

        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh-muc-dung-chung.xlsx");
    }

    @Override
    public List<CategoryDto> getListCategories(String categoryType, boolean isGetAttribute, String keyAttribute, boolean isActive) {
        List<CategoryDto> listCategory = categoriesRepository.getListCategories(categoryType, isActive);
        if (isGetAttribute && "sys_loai_mien_du_lieu".equals(categoryType)) {
            setAttributes(categoryType, listCategory, keyAttribute);
        }
        return listCategory;
    }

    @Override
    public List<CategoryDto> getListCategories(String categoryType, List<String> ids, boolean isGetAttribute) {
        List<CategoryDto> listCategory = categoriesRepository.getListCategories(categoryType, ids);
//        if (isGetAttribute) {
//            setAttributes(categoryType, listCategory, null);
//        }
        return listCategory;
    }

    private void setAttributes(String categoryType, List<CategoryDto> listCategory, String keyAttribute) {
        List<CategoryAttributeEntity> listAttributes = categoriesRepository.getAllAttributeByCategoryType(categoryType);
        Map<Long, Map<String, Object>> mapAttributes = new HashMap<>();
        listAttributes.forEach(item -> {
            mapAttributes.computeIfAbsent(item.getCategoryId(), k -> new HashMap<>());
            mapAttributes.get(item.getCategoryId()).put(item.getAttributeCode(), item.getAttributeValue());
        });
        if (!Utils.isNullOrEmpty(keyAttribute)) {
            Map<Integer, String> mapPeriod;
            if ("PERIOD_ID".equalsIgnoreCase(keyAttribute)) {
                mapPeriod = categoriesRepository.getMapData("evaluationPeriodId", "name", EvaluationPeriodsEntity.class, "evaluationType", 2);
            } else {
                mapPeriod = new HashMap<>();
            }
            listCategory.forEach(dto -> {
                dto.setAttributes(mapAttributes.get(dto.getCategoryId()));
                if ("PERIOD_ID".equalsIgnoreCase(keyAttribute)) {
                    dto.setName(mapPeriod.get(Integer.parseInt(Utils.getStringFromMap(dto.getAttributes(), keyAttribute.toUpperCase()))) + " - " + dto.getName());
                } else {
                    dto.setName(Utils.getStringFromMap(dto.getAttributes(), keyAttribute.toUpperCase()) + " - " + dto.getName());
                }
            });
        } else {
            listCategory.forEach(dto -> {
                dto.setAttributes(mapAttributes.get(dto.getCategoryId()));
            });
        }
    }

    @Override
    public BaseDataTableDto<CategoryDto> getPageable(String categoryType, CategoryRequest.PageableRequest request) {
        return categoriesRepository.getPageable(categoryType, request);
    }

    @Override
    public BaseDataTableDto<CategoryDto> getPageableKeyCheck(String categoryType, CategoryRequest.PageableRequest request) {
        return categoriesRepository.getPageableKeyCheck(categoryType, request);
    }

    @Override
    public List<CategoryDto> getListDistrict(boolean isGetAttribute) {
        return categoriesRepository.getListDistrict();
    }

    @Override
    public List<CategoryDto> getListDistrictByProvince(String provinceId, boolean isGetAttribute) {
        return categoriesRepository.getListDistrictByProvince(provinceId);
    }

    @Override
    public List<CategoryDto> getListWardByDistrict(String districtId, boolean isGetAttribute) {
        return categoriesRepository.getListWardByDistrict(districtId);
    }

    @Override
    public List<CategoryDto> getListCategoriesByParent(String categoryType, String parentTypeCode, String parentValue, boolean isGetAttribute) {
        List<CategoryDto> listCategory = categoriesRepository.getListCategoriesByParent(categoryType, parentTypeCode, parentValue);
        if (isGetAttribute) {
            setAttributes(categoryType, listCategory, null);
        }
        return listCategory;
    }

    @Override
    public List<CategoryDto> getListWardByProvince(String provinceId, boolean isActive) {
        return categoriesRepository.getListWardByProvince(provinceId, isActive);
    }

    @Override
    public CategoryResponse.DetailBean getDataByValue(String categoryType, String value) {
        Optional<CategoryEntity> optional = categoriesRepositoryJPA.findByCategoryTypeAndValue(categoryType, value);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new BaseAppException(String.format("Category %s - %s does not exist", categoryType,value));
        }
        CategoryResponse.DetailBean dto = new CategoryResponse.DetailBean();
        Utils.copyProperties(optional.get(), dto);
        //Lay thong tin attributes
        dto.setListAttributes(categoriesRepository.getAttributeOfCategory(dto.getCategoryId()));
        return dto;
    }

}
