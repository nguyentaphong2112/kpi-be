package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.hbtplus.models.bean.StkEquipmentBean;
import vn.hbtplus.models.request.StockReportRequest;
import vn.hbtplus.repositories.entity.WarehousesEntity;
import vn.hbtplus.repositories.impl.RStkWarehouseRepository;
import vn.hbtplus.services.StockReportService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockReportServiceImpl implements StockReportService {
    private final RStkWarehouseRepository rStkWarehouseRepository;

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> equipmentExport(StockReportRequest dto) throws Exception {
        String pathTemplate = "template/export/BM_BC_Ton_kho_vat_tu.xlsx";
        int startDataRow = 8;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        //so luong vat tu xuat
        Date prePeriodDate = Utils.getLastDay(DateUtils.addMonths(dto.getFromDate(), -1));
        List<StkEquipmentBean> listTonKho = rStkWarehouseRepository.getHistoryByEquipment(dto.getWarehouseId(), null, prePeriodDate);
        List<StkEquipmentBean> listNhapKhoTruocNgay = rStkWarehouseRepository.getIncomingByEquipment(dto.getWarehouseId(), null, DateUtils.addDays(prePeriodDate, 1), DateUtils.addDays(dto.getFromDate(), -1));
        List<StkEquipmentBean> listXuatKhoTruocNgay = rStkWarehouseRepository.getOutgoingByEquipment(dto.getWarehouseId(), null, DateUtils.addDays(prePeriodDate, 1), DateUtils.addDays(dto.getFromDate(), -1));

        List<StkEquipmentBean> listNhapTrongKy = rStkWarehouseRepository.getIncomingByEquipment(dto.getWarehouseId(), null, dto.getFromDate(), dto.getToDate());
        List<StkEquipmentBean> listXuatTrongKy = rStkWarehouseRepository.getOutgoingByEquipment(dto.getWarehouseId(), null, dto.getFromDate(), dto.getToDate());

        List<Long> equipmentTypeIds = new ArrayList<>();
        List<Long> unitIds = new ArrayList<>();
        Map<String, StkEquipmentBean> mapTonKho = new HashMap<>();
        Map<String, StkEquipmentBean> mapNhapTrongky = new HashMap<>();
        Map<String, StkEquipmentBean> mapXuatTrongky = new HashMap<>();
        initDataToReport(listTonKho, listNhapKhoTruocNgay, listXuatKhoTruocNgay, listNhapTrongKy, listXuatTrongKy, equipmentTypeIds, unitIds, mapTonKho, mapNhapTrongky, mapXuatTrongky);
        List<String> keys = new ArrayList<>();
        keys.addAll(mapTonKho.keySet());
        keys.addAll(mapNhapTrongky.keySet());
        keys.addAll(mapXuatTrongky.keySet());
        keys.sort(String::compareTo);

        keys = keys.stream().distinct().toList();

        int stt = 1;
        for (String key : keys) {
            StkEquipmentBean stkEquipmentBean = Utils.NVL(mapTonKho.get(key), Utils.NVL(mapNhapTrongky.get(key), mapXuatTrongky.get(key)));

            int col = 0;
            dynamicExport.setEntry(String.valueOf(stt++), col++);
            dynamicExport.setText(stkEquipmentBean.getEquipmentName(), col++);
            dynamicExport.setText(stkEquipmentBean.getEquipmentCode(), col++);
            dynamicExport.setText(stkEquipmentBean.getEquipmentTypeName(), col++);
            dynamicExport.setText(stkEquipmentBean.getEquipmentUnitName(), col++);
            StkEquipmentBean tonKho = mapTonKho.get(key);
            dynamicExport.setEntry(Utils.formatNumber((tonKho == null || tonKho.getQuantity() == null) ? 0d : tonKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber((tonKho == null || tonKho.getAmountMoney() == null) ? 0d : tonKho.getAmountMoney()), col++);
            StkEquipmentBean nhapKho = mapNhapTrongky.get(key);
            dynamicExport.setEntry(Utils.formatNumber((nhapKho == null || nhapKho.getQuantity() == null) ? 0d : nhapKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber((nhapKho == null || nhapKho.getAmountMoney() == null) ? 0d : nhapKho.getAmountMoney()), col++);
            StkEquipmentBean xuatKho = mapXuatTrongky.get(key);
            dynamicExport.setEntry(Utils.formatNumber((xuatKho == null || xuatKho.getQuantity() == null) ? 0d : xuatKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber((xuatKho == null || xuatKho.getAmountMoney() == null) ? 0d : xuatKho.getAmountMoney()), col++);
            dynamicExport.setFormula("F7+H7-J7".replace("7", String.valueOf(dynamicExport.getLastRow() + 1)), col++);
            dynamicExport.setFormula("G7+I7-K7".replace("7", String.valueOf(dynamicExport.getLastRow() + 1)), col);
            dynamicExport.increaseRow();
        }
        dynamicExport.replaceText("${tu_ngay}", Utils.formatDate(dto.getFromDate()));
        dynamicExport.replaceText("${den_ngay}", Utils.formatDate(dto.getToDate()));
        dynamicExport.replaceText("${ten_kho}", rStkWarehouseRepository.get(WarehousesEntity.class, dto.getWarehouseId()).getName());
        dynamicExport.setCellFormat(7, 0, dynamicExport.getLastRow() - 1, 12, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM_BC_Ton_kho_theo_vat_tu.xlsx", true);
    }

    private static void initDataToReport(List<StkEquipmentBean> listTonKho,
                                         List<StkEquipmentBean> listNhapKhoTruocNgay,
                                         List<StkEquipmentBean> listXuatKhoTruocNgay,
                                         List<StkEquipmentBean> listNhapTrongKy,
                                         List<StkEquipmentBean> listXuatTrongKy,
                                         List<Long> equipmentTypeIds,
                                         List<Long> unitIds,
                                         Map<String, StkEquipmentBean> mapTonKho,
                                         Map<String, StkEquipmentBean> mapNhapTrongky,
                                         Map<String, StkEquipmentBean> mapXuatTrongky) {
        listTonKho.forEach(item -> {
            if (Utils.NVL(item.getQuantity()) > 0) {
                if (!equipmentTypeIds.contains(item.getEquipmentTypeId())) {
                    equipmentTypeIds.add(item.getEquipmentTypeId());
                }
                if (!unitIds.contains(item.getEquipmentUnitId())) {
                    unitIds.add(item.getEquipmentUnitId());
                }
                if (mapTonKho.get(item.getKey()) == null) {
                    mapTonKho.put(item.getKey(), item);
                } else {
                    mapTonKho.get(item.getKey()).add(item);
                }
            }
        });
        listNhapKhoTruocNgay.forEach(item -> {
            if (Utils.NVL(item.getQuantity()) > 0) {
                if (!equipmentTypeIds.contains(item.getEquipmentTypeId())) {
                    equipmentTypeIds.add(item.getEquipmentTypeId());
                }
                if (!unitIds.contains(item.getEquipmentUnitId())) {
                    unitIds.add(item.getEquipmentUnitId());
                }
                if (mapTonKho.get(item.getKey()) == null) {
                    mapTonKho.put(item.getKey(), item);
                } else {
                    mapTonKho.get(item.getKey()).add(item);
                }
            }
        });
        listXuatKhoTruocNgay.forEach(item -> {
            if (Utils.NVL(item.getQuantity()) > 0) {
                if (!equipmentTypeIds.contains(item.getEquipmentTypeId())) {
                    equipmentTypeIds.add(item.getEquipmentTypeId());
                }
                if (!unitIds.contains(item.getEquipmentUnitId())) {
                    unitIds.add(item.getEquipmentUnitId());
                }
                if (mapTonKho.get(item.getKey()) == null) {
                    item.setQuantity(0 - Utils.NVL(item.getQuantity()));
                    mapTonKho.put(item.getKey(), item);
                } else {
                    mapTonKho.get(item.getKey()).remove(item);
                }
            }
        });

        listNhapTrongKy.forEach(item -> {
            if (Utils.NVL(item.getQuantity()) > 0) {
                if (!equipmentTypeIds.contains(item.getEquipmentTypeId())) {
                    equipmentTypeIds.add(item.getEquipmentTypeId());
                }
                if (!unitIds.contains(item.getEquipmentUnitId())) {
                    unitIds.add(item.getEquipmentUnitId());
                }
                if (mapNhapTrongky.get(item.getKey()) == null) {
                    mapNhapTrongky.put(item.getKey(), item);
                } else {
                    mapNhapTrongky.get(item.getKey()).add(item);
                }
            }
        });
        listXuatTrongKy.forEach(item -> {
            if (Utils.NVL(item.getQuantity()) > 0) {
                if (!equipmentTypeIds.contains(item.getEquipmentTypeId())) {
                    equipmentTypeIds.add(item.getEquipmentTypeId());
                }
                if (!unitIds.contains(item.getEquipmentUnitId())) {
                    unitIds.add(item.getEquipmentUnitId());
                }
                if (mapXuatTrongky.get(item.getKey()) == null) {
                    mapXuatTrongky.put(item.getKey(), item);
                } else {
                    mapXuatTrongky.get(item.getKey()).add(item);
                }
            }
        });
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> equipmentTypeExport(StockReportRequest dto) throws Exception {
        String pathTemplate = "template/export/BM_BC_Ton_kho_loai_vat_tu.xlsx";
        int startDataRow = 8;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        Date prePeriodDate = Utils.getLastDay(DateUtils.addMonths(dto.getFromDate(), -1));
        List<StkEquipmentBean> listTonKho = rStkWarehouseRepository.getHistoryByEquipmentType(dto.getWarehouseId(), prePeriodDate);
        List<StkEquipmentBean> listNhapKhoTruocNgay = rStkWarehouseRepository.getIncomingByEquipmentType(dto.getWarehouseId(), DateUtils.addDays(prePeriodDate, 1), DateUtils.addDays(dto.getFromDate(), -1));
        List<StkEquipmentBean> listXuatKhoTruocNgay = rStkWarehouseRepository.getOutgoingByEquipmentType(dto.getWarehouseId(), DateUtils.addDays(prePeriodDate, 1), DateUtils.addDays(dto.getFromDate(), -1));

        List<StkEquipmentBean> listNhapTrongKy = rStkWarehouseRepository.getIncomingByEquipmentType(dto.getWarehouseId(), dto.getFromDate(), dto.getToDate());
        List<StkEquipmentBean> listXuatTrongKy = rStkWarehouseRepository.getOutgoingByEquipmentType(dto.getWarehouseId(), dto.getFromDate(), dto.getToDate());

        List<Long> equipmentTypeIds = new ArrayList<>();
        List<Long> unitIds = new ArrayList<>();
        Map<String, StkEquipmentBean> mapTonKho = new HashMap<>();
        Map<String, StkEquipmentBean> mapNhapTrongky = new HashMap<>();
        Map<String, StkEquipmentBean> mapXuatTrongky = new HashMap<>();
        initDataToReport(listTonKho, listNhapKhoTruocNgay, listXuatKhoTruocNgay, listNhapTrongKy, listXuatTrongKy, equipmentTypeIds, unitIds, mapTonKho, mapNhapTrongky, mapXuatTrongky);
        List<String> keys = new ArrayList<>();
        keys.addAll(mapTonKho.keySet());
        keys.addAll(mapNhapTrongky.keySet());
        keys.addAll(mapXuatTrongky.keySet());
        keys.sort(String::compareTo);

        keys = keys.stream().distinct().collect(Collectors.toList());

        int stt = 1;
        for (String key : keys) {
            StkEquipmentBean stkEquipmentBean = Utils.NVL(mapTonKho.get(key), Utils.NVL(mapNhapTrongky.get(key), mapXuatTrongky.get(key)));

            int col = 0;
            dynamicExport.setEntry(String.valueOf(stt++), col++);
            dynamicExport.setText(stkEquipmentBean.getEquipmentTypeName(), col++);
            dynamicExport.setText(stkEquipmentBean.getEquipmentUnitName(), col++);
            StkEquipmentBean tonKho = mapTonKho.get(key);
            dynamicExport.setEntry(Utils.formatNumber((tonKho == null || tonKho.getQuantity() == null) ? 0d : tonKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber((tonKho == null || tonKho.getAmountMoney() == null) ? 0d : tonKho.getAmountMoney()), col++);
            StkEquipmentBean nhapKho = mapNhapTrongky.get(key);
            dynamicExport.setEntry(Utils.formatNumber((nhapKho == null || nhapKho.getQuantity() == null) ? 0d : nhapKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber((nhapKho == null || nhapKho.getAmountMoney() == null) ? 0d : nhapKho.getAmountMoney()), col++);
            StkEquipmentBean xuatKho = mapXuatTrongky.get(key);
            dynamicExport.setEntry(Utils.formatNumber((xuatKho == null || xuatKho.getQuantity() == null) ? 0d : xuatKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber((xuatKho == null || xuatKho.getAmountMoney() == null) ? 0d : xuatKho.getAmountMoney()), col++);
            dynamicExport.setFormula("D7+F7-H7".replace("7", String.valueOf(dynamicExport.getLastRow() + 1)), col++);
            dynamicExport.setFormula("E7+G7-I7".replace("7", String.valueOf(dynamicExport.getLastRow() + 1)), col++);
            dynamicExport.increaseRow();
        }
        dynamicExport.replaceText("${tu_ngay}", Utils.formatDate(dto.getFromDate()));
        dynamicExport.replaceText("${den_ngay}", Utils.formatDate(dto.getToDate()));
        dynamicExport.replaceText("${ten_kho}", rStkWarehouseRepository.get(WarehousesEntity.class, dto.getWarehouseId()).getName());
        dynamicExport.setCellFormat(7, 0, dynamicExport.getLastRow(), 10, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM_BC_Ton_kho_loai_vat_tu.xlsx", true);
    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> equipmentDepartmentExport(StockReportRequest dto) throws Exception {
        String pathTemplate = "template/export/BM_BC_Ton_kho_vat_tu_don_vi_quan_ly.xlsx";
        int startDataRow = 8;
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        //so luong vat tu xuat
        Date prePeriodDate = Utils.getLastDay(DateUtils.addMonths(dto.getFromDate(), -1));
        List<StkEquipmentBean> listTonKho = rStkWarehouseRepository.getHistoryByOrganization(dto.getDepartmentId(), dto.getWarehouseId(), prePeriodDate);
        List<StkEquipmentBean> listNhapKhoTruocNgay = rStkWarehouseRepository.getIncomingByOrganization(dto.getDepartmentId(), dto.getWarehouseId(), DateUtils.addDays(prePeriodDate, 1), DateUtils.addDays(dto.getFromDate(), -1));
        List<StkEquipmentBean> listXuatKhoTruocNgay = rStkWarehouseRepository.getOutgoingByOrganization(dto.getDepartmentId(), dto.getWarehouseId(), DateUtils.addDays(prePeriodDate, 1), DateUtils.addDays(dto.getFromDate(), -1));

        List<StkEquipmentBean> listNhapTrongKy = rStkWarehouseRepository.getIncomingByOrganization(dto.getDepartmentId(), dto.getWarehouseId(), dto.getFromDate(), dto.getToDate());
        List<StkEquipmentBean> listXuatTrongKy = rStkWarehouseRepository.getOutgoingByOrganization(dto.getDepartmentId(), dto.getWarehouseId(), dto.getFromDate(), dto.getToDate());

        List<Long> equipmentTypeIds = new ArrayList<>();
        List<Long> unitIds = new ArrayList<>();
        Map<String, StkEquipmentBean> mapTonKho = new HashMap<>();
        Map<String, StkEquipmentBean> mapNhapTrongky = new HashMap<>();
        Map<String, StkEquipmentBean> mapXuatTrongky = new HashMap<>();
        initDataToReport(listTonKho, listNhapKhoTruocNgay, listXuatKhoTruocNgay, listNhapTrongKy, listXuatTrongKy, equipmentTypeIds, unitIds, mapTonKho, mapNhapTrongky, mapXuatTrongky);
        List<String> keys = new ArrayList<>();
        keys.addAll(mapTonKho.keySet());
        keys.addAll(mapNhapTrongky.keySet());
        keys.addAll(mapXuatTrongky.keySet());
        keys.sort(String::compareTo);

        keys = keys.stream().distinct().collect(Collectors.toList());

        int stt = 1;

        int col = 0;
        for (String key : keys) {
            StkEquipmentBean bean = Utils.NVL(mapTonKho.get(key), Utils.NVL(mapNhapTrongky.get(key), mapXuatTrongky.get(key)));

            col = 0;
            dynamicExport.setEntry(String.valueOf(stt++), col++);
            dynamicExport.setText(bean.getDepartmentName(), col++);
            dynamicExport.setText(bean.getWarehouseName(), col++);
            dynamicExport.setText(bean.getEquipmentName(), col++);
            dynamicExport.setText(bean.getEquipmentCode(), col++);
            dynamicExport.setText(bean.getEquipmentTypeName(), col++);
            dynamicExport.setText(bean.getEquipmentUnitName(), col++);
            StkEquipmentBean tonKho = mapTonKho.get(key);
            dynamicExport.setEntry(Utils.formatNumber(tonKho == null || tonKho.getQuantity() == null ? 0d : tonKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber(tonKho == null || tonKho.getAmountMoney() == null ? 0d : tonKho.getAmountMoney()), col++);
            StkEquipmentBean nhapKho = mapNhapTrongky.get(key);
            dynamicExport.setEntry(Utils.formatNumber(nhapKho == null || nhapKho.getQuantity() == null ? 0d : nhapKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber(nhapKho == null || nhapKho.getAmountMoney() == null ? 0d : nhapKho.getAmountMoney()), col++);
            StkEquipmentBean xuatKho = mapXuatTrongky.get(key);
            dynamicExport.setEntry(Utils.formatNumber(xuatKho == null || xuatKho.getQuantity() == null ? 0d : xuatKho.getQuantity()), col++);
            dynamicExport.setEntry(Utils.formatNumber(xuatKho == null || xuatKho.getAmountMoney() == null ? 0d : xuatKho.getAmountMoney()), col++);
            dynamicExport.setFormula("H7+J7-L7".replace("7", String.valueOf(dynamicExport.getLastRow() + 1)), col++);
            dynamicExport.setFormula("I7+K7-M7".replace("7", String.valueOf(dynamicExport.getLastRow() + 1)), col);
            dynamicExport.increaseRow();
        }
        dynamicExport.replaceText("${tu_ngay}", Utils.formatDate(dto.getFromDate()));
        dynamicExport.replaceText("${den_ngay}", Utils.formatDate(dto.getToDate()));
//        dynamicExport.replaceText("${ten_don_vi}", dto.getDepartmentId() == null ? " " : rStkWarehouseRepository.get(OrganizationEntity.class, dto.getDepartmentId()).getName());
        dynamicExport.setCellFormat(7, 0, dynamicExport.getLastRow(), col, ExportExcel.BORDER_FORMAT);

        return ResponseUtils.ok(dynamicExport, "BM_BC_Ton_kho_vat_tu_don_vi_quan_ly.xlsx", true);

    }

    @Override
    @Transactional(readOnly = true)
    public ResponseEntity<Object> equipmentDetailExport(StockReportRequest dto) throws Exception {
        String pathTemplate = "template/export/BM_BC_Chi_tiet_vat_tu.xlsx";
        int startDataRow = 8;
        WarehousesEntity warehousesEntity = null;
        if (dto.getWarehouseId() != null) {
            warehousesEntity = rStkWarehouseRepository.get(WarehousesEntity.class, dto.getWarehouseId());
        }
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, startDataRow, true);
        Date prePeriodDate = Utils.getLastDay(DateUtils.addMonths(dto.getFromDate(), -1));
        List<StkEquipmentBean> listTonKho = rStkWarehouseRepository.getHistoryByEquipment(dto.getWarehouseId(), dto.getEquipmentId(), prePeriodDate);
        List<StkEquipmentBean> listNhapKhoTruocNgay = rStkWarehouseRepository.getIncomingByEquipment(dto.getWarehouseId(), dto.getEquipmentId(), DateUtils.addDays(prePeriodDate, 1), DateUtils.addDays(dto.getFromDate(), -1));
        List<StkEquipmentBean> listXuatKhoTruocNgay = rStkWarehouseRepository.getOutgoingByEquipment(dto.getWarehouseId(), dto.getEquipmentId(), DateUtils.addDays(prePeriodDate, 1), DateUtils.addDays(dto.getFromDate(), -1));

        int col = 0;
        int stt = 1;
        dynamicExport.setEntry(stt++, col++);
        dynamicExport.setText(Utils.formatDate(dto.getFromDate()), col++);
        col++;
        col++;
        dynamicExport.setText("Tồn đầu kỳ", col++);
        dynamicExport.setText(Utils.formatDate(dto.getFromDate()), col++);
        col++;
        col++;
        col++;
        Double soLuongTon = listTonKho.isEmpty() ? 0 : listTonKho.get(0).getQuantity();
        soLuongTon += listNhapKhoTruocNgay.isEmpty() ? 0 : listNhapKhoTruocNgay.get(0).getQuantity();
        soLuongTon = soLuongTon - (listXuatKhoTruocNgay.isEmpty() ? 0 : listXuatKhoTruocNgay.get(0).getQuantity());
        dynamicExport.setEntry(Utils.formatNumber(soLuongTon), col);

        List<StkEquipmentBean> listPhieu = rStkWarehouseRepository.getListIncomingShipment(dto.getWarehouseId(), dto.getEquipmentId(), dto.getFromDate(), dto.getToDate());
        listPhieu.addAll(rStkWarehouseRepository.getListOutgoingShipment(dto.getWarehouseId(), dto.getEquipmentId(), dto.getFromDate(), dto.getToDate()));

        Collections.sort(listPhieu);
        dynamicExport.increaseRow();

        for (StkEquipmentBean stkEquipmentBean : listPhieu) {
            col = 0;
            dynamicExport.setEntry(stt++, col++);
            dynamicExport.setText(Utils.formatDate(stkEquipmentBean.getPickingDate()), col++);
            dynamicExport.setText("Y".equalsIgnoreCase(stkEquipmentBean.getIsIncoming()) ? stkEquipmentBean.getPickingNo() : null, col++);
            dynamicExport.setText("Y".equalsIgnoreCase(stkEquipmentBean.getIsIncoming()) ? null : stkEquipmentBean.getPickingNo(), col++);
            dynamicExport.setText(stkEquipmentBean.getTypeName(), col++);
            dynamicExport.setText(Utils.formatDate(stkEquipmentBean.getPickingDate()), col++);
            dynamicExport.setText(stkEquipmentBean.getEquipmentUnitName(), col++);
            dynamicExport.setEntry(Utils.formatNumber("Y".equalsIgnoreCase(stkEquipmentBean.getIsIncoming()) ? stkEquipmentBean.getQuantity() : null), col++);
            dynamicExport.setEntry(Utils.formatNumber("Y".equalsIgnoreCase(stkEquipmentBean.getIsIncoming()) ? null : stkEquipmentBean.getQuantity()), col++);
            dynamicExport.setFormula("Jstart+Hend-Iend".replace("start", String.valueOf(dynamicExport.getLastRow()))
                    .replace("end", String.valueOf(dynamicExport.getLastRow() + 1)), col++);
            dynamicExport.increaseRow();
        }
        dynamicExport.replaceText("${ten_kho}", warehousesEntity == null ? "" : warehousesEntity.getName());
        dynamicExport.replaceText("${tu_ngay}", Utils.formatDate(dto.getFromDate()));
        dynamicExport.replaceText("${den_ngay}", Utils.formatDate(dto.getToDate()));
        dynamicExport.setCellFormat(startDataRow - 1, 0, dynamicExport.getLastRow() - 1, col, ExportExcel.BORDER_FORMAT);
        return ResponseUtils.ok(dynamicExport, "BM_BC_Chi_tiet_vat_tu.xlsx", true);

    }
}
