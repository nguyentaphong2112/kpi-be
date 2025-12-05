package vn.hbtplus.services.impl;

import com.aspose.words.SaveFormat;
import com.itextpdf.kernel.geom.PageSize;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import vn.hbtplus.constants.BaseConstants;
import vn.hbtplus.constants.Constant;
import vn.hbtplus.exceptions.BaseAppException;
import vn.hbtplus.feigns.FileStorageFeignClient;
import vn.hbtplus.repositories.entity.CategoryEntity;
import vn.hbtplus.repositories.impl.PartnersRepository;
import vn.hbtplus.services.CommonUtilsService;
import vn.hbtplus.utils.ExportWorld;
import vn.hbtplus.utils.Utils;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class CommonUtilsServiceImpl implements CommonUtilsService {
    private final PartnersRepository partnersRepository;
    private final FileStorageFeignClient storageFeignClient;
    private final HttpServletRequest request;
    @Override
    public String getFullAddress(String currentAddress, String wardId, String districtId, String provinceId) {
        String xa = null;
        if (!Utils.isNullOrEmpty(wardId)) {
            CategoryEntity categoryWard = partnersRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.XA, "value", wardId);
            xa = categoryWard == null ? null : categoryWard.getName();
        }
        String huyen = null;
        if (!Utils.isNullOrEmpty(districtId)) {
            CategoryEntity categoryDistrict = partnersRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.HUYEN, "value", districtId);
            huyen = categoryDistrict == null ? null : categoryDistrict.getName();
        }
        String tinh = null;

        if (!Utils.isNullOrEmpty(provinceId)) {
            CategoryEntity categoryProvince = partnersRepository.get(CategoryEntity.class, "categoryType", Constant.CATEGORY_TYPES.TINH, "value", provinceId);
            tinh = categoryProvince == null ? null : categoryProvince.getName();
        }
        return Utils.join(", ", currentAddress, xa, huyen, tinh);
    }

    @Override
    public byte[] printCardPdf(List<String> listParams, List<Map<String, Object>> listMapParams, String fileId, String templatePath) throws Exception {
        log.info("[printCardPdf] start");
        byte[] fileBytes = storageFeignClient.downloadFile(Utils.getRequestHeader(request), Constant.ATTACHMENT.MODULE, fileId);
        if(fileBytes == null || fileBytes.length == 0){
            throw new BaseAppException("Error downloading file: "+ fileId);
        }
        boolean isTypeDoc = Utils.checkMagicHeaderFile(fileBytes, BaseConstants.DOCX_MAGIC_NUMBER) || Utils.checkMagicHeaderFile(fileBytes, BaseConstants.DOC_MAGIC_NUMBER);

        ByteArrayOutputStream zipOutputStream = new ByteArrayOutputStream();
        try (ZipOutputStream zipOut = new ZipOutputStream(zipOutputStream)) {
            List<Map<String, Object>> listMap = new ArrayList<>();
            if (!Utils.isNullOrEmpty(listParams)) {
                listParams.forEach(item -> {
                    Map<String, Object> mapData = new HashMap<>();
                    mapData.put("params", item);
                    listMap.add(mapData);
                });
            }

            int i = 1;
            for (Map<String, Object> mapParams : listMapParams) {
                ExportWorld exportWorld;
                if (isTypeDoc) {
                    try (InputStream inputStream = new ByteArrayInputStream(fileBytes)) {
                        exportWorld = new ExportWorld(inputStream);
                    }
                } else {
                    exportWorld = new ExportWorld((templatePath));
                    exportWorld.addImageToWord(fileBytes, PageSize.A4.getHeight(), PageSize.A4.getWidth(), 0, 0);
                }

                exportWorld.replaceKeysV2(listMap);
                exportWorld.replaceKeys(mapParams);

                try (ByteArrayOutputStream pdfOutputStream = new ByteArrayOutputStream()) {

                    if (listMapParams.size() == 1) {
                        String fileName = mapParams.get("HO_TEN") != null ? mapParams.get("HO_TEN").toString() : ("inThiep" + i);
                        fileName = Utils.removeSign(fileName.replace(" ", "_")) + ".pdf";
                        exportWorld.saveFile(fileName, SaveFormat.PDF);
                        File filePdf = new File(fileName);
                        byte[] dataContent = Files.readAllBytes(filePdf.toPath());
                        Files.deleteIfExists(filePdf.toPath());
                        return dataContent;
                    } else {
                        String fileName = mapParams.get("HO_TEN") != null ? mapParams.get("HO_TEN").toString() : ("inThiep" + i);
                        ZipEntry zipEntry = new ZipEntry(Utils.removeSign(fileName.replace(" ", "_")) + ".pdf");
                        zipOut.putNextEntry(zipEntry);
                        zipOut.write(pdfOutputStream.toByteArray());
                        zipOut.closeEntry();
                    }
                }
                i++;
            }
        }
        byte[] byteData = zipOutputStream.toByteArray();
        zipOutputStream.close();
        log.info("[printCardPdf] end length: {}", byteData.length);
        return byteData;
    }

}
