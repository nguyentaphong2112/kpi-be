package vn.hbtplus.services.impl;

import com.aspose.words.Document;
import com.aspose.words.FontSettings;
import com.aspose.words.SaveFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.hbtplus.config.ApplicationConfig;
import vn.hbtplus.repositories.impl.EmployeeRepositoryImpl;
import vn.hbtplus.services.CommonUtilsService;
import vn.hbtplus.utils.FileMultipartFile;
import vn.hbtplus.utils.Utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * @author tudd
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CommonUtilsServiceImpl implements CommonUtilsService {

    private final ApplicationConfig applicationConfig;
    private final EmployeeRepositoryImpl employeeRepository;
    private final Logger LOGGER = LoggerFactory.getLogger(CommonUtilsServiceImpl.class);

    @Override
    @Transactional(readOnly = true)
    public String getFilePathExport(String fileName) {
        String userName = Utils.getUserNameLogin();
        return applicationConfig.getExportFolder() + Utils.convertDateTimeToString(new Date()) + "_" + userName + "_" + fileName;
    }

    @Override
    public String getOnlyFilePathExport(String fileName) {
        return applicationConfig.getExportFolder() + fileName;
    }


    @Override
    public Map<String, MultipartFile> readFileZip(MultipartFile inputFile) throws IOException {
        Map<String, MultipartFile> mapFile = new HashMap<>();
        if (inputFile == null) {
            return mapFile;
        } else if ("application/pdf".equals(inputFile.getContentType())) {
            String originalFileName = inputFile.getOriginalFilename();
            int index = Objects.requireNonNull(originalFileName).indexOf("_");
            index = index > 0 ? index : originalFileName.indexOf(".");
            if (index > 0) {
                String empCode = originalFileName.substring(0, index).toUpperCase();
                mapFile.put(empCode, inputFile);
            } else {
                mapFile.put(originalFileName, inputFile);
            }
            return mapFile;
        }

        ZipInputStream inputStream = null;
        try {
            inputStream = new ZipInputStream(inputFile.getInputStream());

            Path path = Paths.get(applicationConfig.getExportFolder());
            String strCurrentDate = Utils.convertDateTimeToString(new Date());
            ZipEntry entry;
            while ((entry = inputStream.getNextEntry()) != null) {
                Path resolvedPath;
                if (!entry.isDirectory()) {
                    int indexFolder = entry.getName().lastIndexOf("/");
                    if (indexFolder > -1) {
                        String folderName = entry.getName().substring(0, entry.getName().lastIndexOf("/"));
                        resolvedPath = path.resolve(entry.getName().replace(folderName, folderName + strCurrentDate));
                    } else {
                        resolvedPath = path.resolve(entry.getName());
                    }

                    Files.createDirectories(resolvedPath.getParent());
                    Files.copy(inputStream, resolvedPath, StandardCopyOption.REPLACE_EXISTING);
                    File f = resolvedPath.toFile();
                    MultipartFile multipartFilePdf;
                    FileInputStream input = null;
                    try {
                        input = new FileInputStream(f);
                        multipartFilePdf = new FileMultipartFile(f.getName(), f.getName(), "application/pdf", IOUtils.toByteArray(input));
                    } finally {
                        if (input != null) {
                            input.close();
                        }
                    }
                    // xu ly lay ma nhan vien
                    String pdfFileName = Utils.NVL(multipartFilePdf.getOriginalFilename());
                    int index = pdfFileName.indexOf("_");
                    index = index > 0 ? index : pdfFileName.indexOf(".");
                    if (index > 0) {
                        String empCode = pdfFileName.substring(0, index).toUpperCase();
                        mapFile.put(empCode, multipartFilePdf);
                    } else {
                        mapFile.put(pdfFileName, multipartFilePdf);
                    }

                    boolean isDelete = f.delete();
                    if (!isDelete) {
                        LOGGER.info("readFileZip|delete file fail|" + f.getName());
                    }
                } else {
                    String entryName = entry.getName().substring(0, entry.getName().length() - 1);
                    resolvedPath = path.resolve(entryName + strCurrentDate + "/");
                    Files.createDirectories(resolvedPath);
                }
            }
        } catch (Exception ex) {
            LOGGER.error("Unzip error", ex);
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
        return mapFile;
    }

    @Override
    public Long getEmpIdLogin() {
        return employeeRepository.getEmployeeIdLogin();
    }

    @Override
    public void savePdfFile(Document doc, String filePath) throws Exception {
        FontSettings.setFontsFolder(applicationConfig.getFontFolder(), false);
        doc.save(filePath, SaveFormat.PDF);
    }
}
