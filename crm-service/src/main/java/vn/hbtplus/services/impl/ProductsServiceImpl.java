/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.configs.MdcForkJoinPool;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.ErrorImportException;
import vn.hbtplus.models.request.ProductsRequest;
import vn.hbtplus.models.response.*;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.entity.PartnersEntity;
import vn.hbtplus.repositories.entity.ProductsEntity;
import vn.hbtplus.repositories.impl.ProductsRepository;
import vn.hbtplus.repositories.jpa.ProductsRepositoryJPA;
import vn.hbtplus.services.ObjectAttributesService;
import vn.hbtplus.services.ProductsService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.utils.*;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Lop impl service ung voi bang crm_products
 * @author tudd
 * @since 1.0
 * @version 1.0
 */

@Service
@RequiredArgsConstructor
public class ProductsServiceImpl implements ProductsService {

    private final ProductsRepository productsRepository;
    private final ProductsRepositoryJPA productsRepositoryJPA;
    private final MdcForkJoinPool forkJoinPool;
    private final ObjectAttributesService objectAttributesService;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<ProductsResponse> searchData(ProductsRequest.SearchForm dto) {
        return ResponseUtils.ok(productsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public ResponseEntity saveData(ProductsRequest.SubmitForm dto, Long id) throws BaseAppException {
        ProductsEntity entity;

        boolean isDuplicate = productsRepository.duplicate(ProductsEntity.class, id, "code", dto.getCode());
        if (isDuplicate) {
            throw new BaseAppException("ERROR_PRODUCT_DUPLICATE", I18n.getMessage("error.product.code.duplicate"));
        }

        if (id != null && id > 0L) {
            entity = productsRepositoryJPA.getById(id);
            entity.setModifiedTime(new Date());
            entity.setModifiedBy(Utils.getUserNameLogin());
        } else {
            entity = new ProductsEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
        }
        Utils.copyProperties(dto, entity);
        entity.setIsDeleted(BaseConstants.STATUS.NOT_DELETED);
        productsRepositoryJPA.save(entity);
        objectAttributesService.saveObjectAttributes(entity.getProductId(), dto.getListAttributes(), ProductsEntity.class, null);

        return ResponseUtils.ok(entity.getProductId());
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<ProductsEntity> optional = productsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ProductsEntity.class);
        }
        productsRepository.deActiveObject(ProductsEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<ProductsResponse> getDataById(Long id)  throws RecordNotExistsException {
        Optional<ProductsEntity> optional = productsRepositoryJPA.findById(id);
        if (!optional.isPresent() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, ProductsEntity.class);
        }
        ProductsResponse dto = new ProductsResponse();
        Utils.copyProperties(optional.get(), dto);
        dto.setListAttributes(objectAttributesService.getAttributes(id, Constant.ATTACHMENT.TABLE_NAMES.CRM_PRODUCTS));
        return ResponseUtils.ok(dto);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> exportData(ProductsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh_sach_san_pham.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 3, true);
        List<Map<String, Object>> listDataExport = productsRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }
        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh_sach_san_pham.xlsx");
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> downloadTemplate() throws Exception {
        ExportExcel exportExcel = new ExportExcel("template/import/BM_Import_danh-sach-san-pham.xlsx", 2, true);

        List<CompletableFuture<Object>> completableFutures = new ArrayList<>();
        // Đơn vị tính
        completableFutures.add(CompletableFuture.supplyAsync(() -> productsRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_DVT_SAN_PHAM, "orderNumber"), forkJoinPool));

        // Nhóm sản phẩm
        completableFutures.add(CompletableFuture.supplyAsync(() -> productsRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_NHOM_SAN_PHAM, "orderNumber"), forkJoinPool));

        // Trạng thái
        completableFutures.add(CompletableFuture.supplyAsync(() -> productsRepository.getListMapObjectByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_TRANG_THAI_SAN_PHAM, "orderNumber"), forkJoinPool));

        CompletableFuture<Void> allReturns = CompletableFuture.allOf(completableFutures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<Object>> allFutures = allReturns.thenApply(v -> completableFutures.stream().map(CompletableFuture::join).collect(Collectors.toList()));
        List<Object> objs = allFutures.get();

        int activeSheet = 1;
        for (Object obj : objs) {
            exportExcel.setActiveSheet(activeSheet++);
            List<Map<String, Object>> listMapData = (List<Map<String, Object>>) obj;
            if (!Utils.isNullOrEmpty(listMapData)) {
                for (int row = 1; row <= listMapData.size(); row++) {
                    exportExcel.setText(String.valueOf(row), 0, row);
                    Object name = listMapData.get(row - 1).get("name");
                    exportExcel.setText(name != null ? name.toString() : "", 1, row);
                }
            }

        }

        exportExcel.setActiveSheet(0);
        return ResponseUtils.ok(exportExcel, "BM_Import_danh-sach-san-pham.xlsx", false);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity importProcess(MultipartFile file) throws IOException {
        ImportExcel importExcel = new ImportExcel("template/import/BM_Import_danh-sach-san-pham.xml");
        List<Object[]> dataList = new ArrayList<>();
        if (importExcel.validateCommon(file.getInputStream(), dataList)) {

            Map<String, String> mapCategoryName = new HashMap<>();
            List<CategoryEntity> listProductGroup = productsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_NHOM_SAN_PHAM);
            listProductGroup.forEach(item -> mapCategoryName.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapUnitId = new HashMap<>();
            List<CategoryEntity> listUnit = productsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_DVT_SAN_PHAM);
            listUnit.forEach(item -> mapUnitId.put(item.getName().toLowerCase(), item.getValue()));

            Map<String, String> mapStatusId = new HashMap<>();
            List<CategoryEntity> listStatus = productsRepository.findByProperties(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.CRM_TRANG_THAI_SAN_PHAM);
            listStatus.forEach(item -> mapStatusId.put(item.getName().toLowerCase(), item.getValue()));

            List<ProductsEntity> listInsert = new ArrayList<>();
            String userName = Utils.getUserNameLogin();
            Date curDate = new Date();

            int row = 0;
            for (Object[] obj : dataList) {
                ProductsEntity entity = new ProductsEntity();
                entity.setCreatedBy(userName);
                entity.setCreatedTime(curDate);

                int col = 1;

                String code = (String) obj[col];
                boolean isDuplicate = productsRepository.duplicate(ProductsEntity.class, null, "code", code);
                if (isDuplicate) {
                    importExcel.addError(row, col, I18n.getMessage("error.product.import.code"), code);
                } else {
                    entity.setCode(code);
                }
                col++;

                entity.setName(((String) obj[col++]).trim());

                String unitName = Utils.NVL(((String) obj[col])).trim();
                String unitId = mapUnitId.get(unitName.toLowerCase());
                if (!Utils.isNullOrEmpty(unitName) && unitId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), unitName);
                } else {
                    entity.setUnitId(unitId);
                }
                col++;

                entity.setUnitPrice((Double) obj[col++]);

                String categoryName = Utils.NVL(((String) obj[col])).trim();
                String genderId = mapCategoryName.get(categoryName.toLowerCase());
                if (!Utils.isNullOrEmpty(categoryName) && genderId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), categoryName);
                } else {
                    entity.setCategoryId(genderId);
                }
                col++;

                String statusName = Utils.NVL(((String) obj[col])).trim();
                String statusId = mapStatusId.get(statusName.toLowerCase());
                if (!Utils.isNullOrEmpty(statusName) && genderId == null) {
                    importExcel.addError(row, col, I18n.getMessage("error.import.category.invalid"), statusName);
                } else {
                    entity.setStatusId(statusId);
                }

                listInsert.add(entity);
                row++;
            }

            if (importExcel.hasError()) {
                throw new ErrorImportException(file, importExcel);
            } else {
                productsRepository.insertBatch(ProductsEntity.class, listInsert, userName);
            }
        } else {
            throw new ErrorImportException(file, importExcel);
        }
        return ResponseUtils.ok(true);
    }

}
