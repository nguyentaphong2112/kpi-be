/*
 * Copyright (C) 2023 HBTPlus. All rights reserved.
 * HBTPlus. Use is subject to license terms.
 */
package vn.hbtplus.services.impl;

import com.jxcell.CellException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.dto.LibraryParameterDto;
import vn.hbtplus.models.request.BookEditionDetailsRequest;
import vn.hbtplus.models.response.BaseResponseEntity;
import vn.hbtplus.models.response.BookEditionDetailsResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.entity.BookEditionDetailEntity;
import vn.hbtplus.repositories.impl.BookEditionDetailsRepository;
import vn.hbtplus.repositories.jpa.BookEditionDetailsRepositoryJPA;
import vn.hbtplus.services.BookEditionDetailService;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.services.ParameterService;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.exceptions.RecordNotExistsException;
import vn.hbtplus.utils.Utils;

import java.text.MessageFormat;
import java.util.*;

/**
 * Lop impl service ung voi bang lib_book_edition_details
 *
 * @author tudd
 * @version 1.0
 * @since 1.0
 */

@Service
@RequiredArgsConstructor
public class BookEditionDetailServiceImpl implements BookEditionDetailService {

    private final BookEditionDetailsRepository bookEditionDetailsRepository;
    private final ParameterService parameterService;
    private final BookEditionDetailsRepositoryJPA bookEditionDetailsRepositoryJPA;

    @Override
    @Transactional(readOnly = true)
    public TableResponseEntity<BookEditionDetailsResponse> searchData(BookEditionDetailsRequest.SearchForm dto) {
        return ResponseUtils.ok(bookEditionDetailsRepository.searchData(dto));
    }

    @Override
    @Transactional
    public boolean saveData(Long bookEditionId, List<String> bookNos) throws BaseAppException {
        //check xem ma sach nao da ton tai
        List<String> existedNo = bookEditionDetailsRepository.getListExisted(bookNos);
        if (!existedNo.isEmpty()) {
            throw new BaseAppException("BOOK_NO_ALREADY_EXISTED", "error.bookNo.existed");
        }
        bookNos.forEach(str -> {
            BookEditionDetailEntity entity = new BookEditionDetailEntity();
            entity.setCreatedTime(new Date());
            entity.setCreatedBy(Utils.getUserNameLogin());
            entity.setStatus(BookEditionDetailEntity.STATUS.HIEN_CO);
            entity.setBookEditionId(bookEditionId);
            entity.setBookNo(str);
            bookEditionDetailsRepositoryJPA.save(entity);
        });

        return true;
    }

    @Override
    @Transactional
    public ResponseEntity deleteData(Long id) throws RecordNotExistsException {
        Optional<BookEditionDetailEntity> optional = bookEditionDetailsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookEditionDetailEntity.class);
        }
        bookEditionDetailsRepository.deActiveObject(BookEditionDetailEntity.class, id);
        return ResponseUtils.ok(id);
    }

    @Override
    @Transactional(readOnly = true)
    public BaseResponseEntity<BookEditionDetailsResponse> getDataById(Long id) throws RecordNotExistsException {
        Optional<BookEditionDetailEntity> optional = bookEditionDetailsRepositoryJPA.findById(id);
        if (optional.isEmpty() || BaseConstants.STATUS.DELETED.equals(optional.get().getIsDeleted())) {
            throw new RecordNotExistsException(id, BookEditionDetailEntity.class);
        }
        BookEditionDetailsResponse dto = new BookEditionDetailsResponse();
        Utils.copyProperties(optional.get(), dto);
        return ResponseUtils.ok(dto);
    }

    @Override
    public TableResponseEntity<BookEditionDetailsResponse.EditionDetail> getDataByEditionId(BookEditionDetailsRequest.SearchForm dto) {
        return ResponseUtils.ok(bookEditionDetailsRepository.searchByEditionId(dto));
    }

    @Override
    public ResponseEntity<Object> exportData(BookEditionDetailsRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/BM_Xuat_DS_tuyen_dung.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = bookEditionDetailsRepository.getListExport(dto);

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "BM_Xuat_DS_tuyen_dung.xlsx");
    }

    @Override
    public BookEditionDetailsResponse.EditionDetail getDataByBookNo(String bookNo) throws BaseAppException {
        BookEditionDetailsResponse.EditionDetail result = bookEditionDetailsRepository.getDataByBookNo(bookNo);
        if (result == null) {
            throw new BaseAppException("BOOK_NO_INVALID", "error.bookNo.invalid");
        }
        return result;
    }

    @Override
    public ExportExcel incrementBookNo(Integer total) throws BaseAppException, InstantiationException, IllegalAccessException, CellException {
        LibraryParameterDto parameterDto = parameterService.getConfig(LibraryParameterDto.class, new Date());
        //Lay ma lon nhat
        String maxBookNo = bookEditionDetailsRepository.getMaxBookNo(parameterDto.getPrefixBookNo());
        String maxNumber;
        if (Utils.isNullOrEmpty(maxBookNo)) {
            maxNumber = "0";
        } else {
            maxNumber = maxBookNo.replace(parameterDto.getPrefixBookNo(), "");
        }
        if (!maxNumber.matches("\\d+")) {
            throw new BaseAppException("BOOK_NO_INVALID", "error.bookNo.increment.notIsNumber");
        }
        int startNumber = Integer.valueOf(maxNumber) + 1;
        List<Map<String, Object>> results = new ArrayList<>();
        String pathTemplate = "template/export/BM_Danh_sach_ma_sach_moi.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);

        for (int i = 0; i < total; i++) {
            Map map = new HashMap();
            map.put("ma_sach", MessageFormat.format("{0}{1}", parameterDto.getPrefixBookNo(),
                    String.format("%0" + (parameterDto.getBookNoLength() - parameterDto.getPrefixBookNo().length()) + "d", startNumber)));
            map.put("ghi_chu", "");
            results.add(map);
            startNumber++;
        }
        dynamicExport.replaceKeys(results);
        return dynamicExport;
    }

}
