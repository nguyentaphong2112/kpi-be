package vn.hbtplus.services.impl;

import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.ImageType;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.models.request.CardObjectRequest;
import vn.hbtplus.models.request.PartnersRequest;
import vn.hbtplus.models.response.CardObjectResponse;
import vn.hbtplus.models.response.TableResponseEntity;
import vn.hbtplus.repositories.impl.CardObjectRepository;
import vn.hbtplus.services.CardObjectService;
import vn.hbtplus.services.CommonUtilsService;
import vn.hbtplus.utils.ExportExcel;
import vn.hbtplus.utils.I18n;
import vn.hbtplus.utils.ResponseUtils;
import vn.hbtplus.utils.Utils;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

@Service
@RequiredArgsConstructor
public class CardObjectServiceImpl implements CardObjectService {
    private final CardObjectRepository cardObjectRepository;
    private final CommonUtilsService commonUtilsService;
    private final Environment environment;

    @Override
    public TableResponseEntity<CardObjectResponse.SearchResult> searchData(CardObjectRequest.SearchForm dto) {
        return ResponseUtils.ok(cardObjectRepository.searchData(dto));
    }

    @Override
    public ResponseEntity<Object> exportData(CardObjectRequest.SearchForm dto) throws Exception {
        String pathTemplate = "template/export/danh_cskh_sinh_nhat.xlsx";
        ExportExcel dynamicExport = new ExportExcel(pathTemplate, 2, true);
        List<Map<String, Object>> listDataExport = cardObjectRepository.getListExport(dto);
        if (Utils.isNullOrEmpty(listDataExport)) {
            throw new BaseAppException(I18n.getMessage("global.notFound"));
        }

        for (Map<String, Object> mapData : listDataExport) {
            Object daysUntilBirthday = mapData.get("daysUntilBirthday");
            if (daysUntilBirthday != null) {
                String daysUntilBirthdayStr = String.valueOf(daysUntilBirthday).equals("Hôm nay") ? "Hôm nay" : (daysUntilBirthday + " ngày");
                mapData.put("con_lai", daysUntilBirthdayStr);
            } else {
                mapData.put("con_lai", "");
            }

            Object productDetail = mapData.get("productDetail");
            if (productDetail != null) {
                mapData.put("productName", productDetail.toString().split("#")[0]);
                mapData.put("productPrice", productDetail.toString().split("#")[1]);
            } else {
                mapData.put("productName", "");
                mapData.put("productPrice", "");
            }
            Long totalOrderAmount = 0L;
            Long paidAmount = 0L;
            if (mapData.get("total_order_amount") != null) {
                totalOrderAmount = Long.valueOf(mapData.get("total_order_amount").toString());
            }
            if (mapData.get("paid_amount") != null) {
                paidAmount = Double.valueOf(mapData.get("paid_amount").toString()).longValue();
            }
            mapData.put("owedAmount", totalOrderAmount - paidAmount);

            Object totalAmount = mapData.get("totalAmount");
            if (totalAmount != null && Double.parseDouble(totalAmount.toString()) > 0) {
                Object totalPayment = mapData.get("totalPayment");
                if (totalPayment != null) {
                    mapData.put("so_tien_con_lai", Double.parseDouble(totalAmount.toString()) - Double.parseDouble(totalPayment.toString()));
                } else {
                    mapData.put("so_tien_con_lai", totalAmount);
                }
            } else {
                mapData.put("so_tien_con_lai", "");
            }
        }

        dynamicExport.replaceKeys(listDataExport);
        return ResponseUtils.ok(dynamicExport, "danh_sach_cskh_sinh_nhat.xlsx", true);
    }

    @Override
    public List<CardObjectResponse.DetailBean> getListObject(String objType, Long objId) {
        return cardObjectRepository.getListObject(objType, objId, null);
    }

    @Override
    public ResponseEntity<Object> exportCard(PartnersRequest.PrintCard dto) throws Exception {

        if (Utils.isNullOrEmpty(dto.getListId()) || Utils.isNullOrEmpty(dto.getFileId())) {
            throw new BaseAppException("listId and fileId is required");
        }
        List<CardObjectResponse.DetailBean> listData = cardObjectRepository.getListObject(dto.getObjType(), null, dto.getListId());
        if (Utils.isNullOrEmpty(listData)) {
            throw new BaseAppException(I18n.getMessage("error.exportCard.objectCard.required"));
        }

        List<Map<String, Object>> listMapParams = new ArrayList<>();
        Date curDate = new Date();
        for (CardObjectResponse.DetailBean data : listData) {
            Map<String, Object> params = new HashMap<>();
            dto.getMapParams().forEach(param -> {
                String value = param.getValue();
                value = value.replace("{TEN_GOI}", data.getName());
                value = value.replace("{NICKNAME}", data.getAliasName());
                value = value.replace("{DANH_XUNG}", data.getAliasName());
                value = value.replace("{HO_TEN}", data.getFullName());
                if (data.getDateOfBirth() != null) {
                    int year = Utils.getYearByDate(curDate) - Utils.getYearByDate(data.getDateOfBirth());
                    value = value.replace("{TUOI}", Utils.formatNumber(year > 0 ? year : 1));
                }
                params.put(param.getCode(), value);
            });
            params.put("NICKNAME", data.getAliasName());
            params.put("DANH_XUNG", data.getAliasName());
            params.put("HO_TEN", data.getFullName());
            if (data.getDateOfBirth() != null) {
                int year = Utils.getYearByDate(curDate) - Utils.getYearByDate(data.getDateOfBirth());
                params.put("TUOI", year > 0 ? year : 1);
            }
            listMapParams.add(params);
        }

        String templatePath;
        if (StringUtils.equalsIgnoreCase(dto.getType(), Constant.CARD_TYPE.CHUNG_NHAN)) {
            templatePath = "template/export/in-thiep/BM_chung_nhan.docx";
        } else if (StringUtils.equalsIgnoreCase(dto.getType(), Constant.CARD_TYPE.SINH_NHAT)) {
            templatePath = "template/export/in-thiep/BM_chuc_mung_sn.docx";
        } else {
            templatePath = "template/export/in-thiep/BM_thu_moi.docx";
        }
        byte[] pdfFile = commonUtilsService.printCardPdf(dto.getListParameter(), listMapParams, dto.getFileId(), templatePath);
        if (pdfFile == null || pdfFile.length == 0) {
            throw new BaseAppException("The generated PDF is invalid or empty: " + environment.getProperty("service.properties.file-client-url"));
        }
        String fileName;
        String dateTime = Utils.formatDate(new Date(), BaseConstants.COMMON_EXPORT_DATE_TIME_FORMAT);
        if (listData.size() > 1) {
            fileName = "in_thiep_" + dateTime + ".zip";
            return ResponseUtils.getResponseFileEntity(pdfFile, fileName);
        } else {
            fileName = Utils.removeSign(listData.get(0).getFullName().replace(" ", "_")) + "_" + dateTime + ".pdf";
            try (PDDocument document = Loader.loadPDF(pdfFile)) {
                if (document.getNumberOfPages() > 0) {
                    PDFRenderer pdfRenderer = new PDFRenderer(document);
                    BufferedImage image = pdfRenderer.renderImageWithDPI(0, 300, ImageType.RGB);
                    String outputFileName = fileName.replace(".pdf", ".jpg");
                    saveBufferedImageToFile(image, outputFileName, "jpg");
                    image.flush();
                }
            }
            return ResponseUtils.getResponseFileEntity(pdfFile, fileName, true, true);
        }
    }


    private static void saveBufferedImageToFile(BufferedImage image, String outputFileName, String formatName) throws IOException {
        String exportFolder = Utils.getExportFolder();
        String jpgFilePath = Paths.get(exportFolder, outputFileName).toString();
        File outputFile = new File(jpgFilePath);
        File parentDir = outputFile.getParentFile();
        if (parentDir != null && !parentDir.exists()) {
            parentDir.mkdirs();
        }
        ImageIO.write(image, formatName, outputFile);
    }
}
