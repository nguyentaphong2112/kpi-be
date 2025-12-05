package vn.kpi.utils;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Slf4j
public class ZipFileUtils {
    public static Map<String, List<MultipartFile>> splitFileByEmpCode(List<MultipartFile> files) {
        List<MultipartFile> multipartFiles = new ArrayList<>();
        if (Utils.isNullOrEmpty(files)) {
            return new HashMap<>();
        }
        files.forEach(item -> {
            multipartFiles.addAll(unzipFile(item, ""));
        });
        Map<String, List<MultipartFile>> mapResult = new HashMap();
        multipartFiles.forEach(item -> {
            String key = splitEmpCode(item.getOriginalFilename());
            if (mapResult.get(key) == null) {
                mapResult.put(key, new ArrayList<>());
            }
            mapResult.get(key).add(item);
        });
        return mapResult;
    }

    private static String splitEmpCode(String originalFilename) {
        Pattern pattern = Pattern.compile("^\\d{5}");
        Matcher matcher = pattern.matcher(originalFilename);

        if (matcher.find()) {
            String extracted = matcher.group();
            // Check if the total number of digits at the beginning is exactly 5
            if (originalFilename.startsWith(extracted) && extracted.length() == 5) {
                return extracted;
            }
        }
        return "all";
    }

    public static List<MultipartFile> unzipFile(MultipartFile file, String destDir) {
        List<MultipartFile> extractedFiles = new ArrayList<>();
        if (file.getOriginalFilename().toLowerCase().endsWith(".zip") || file.getOriginalFilename().toLowerCase().endsWith(".rar")) {
            File destDirFile = new File(destDir);
            // Create destination directory if it doesn't exist
            if (!destDirFile.exists()) {
                destDirFile.mkdirs();
            }

            try (ZipInputStream zipInputStream = new ZipInputStream(file.getInputStream())) {
                ZipEntry entry;
                while ((entry = zipInputStream.getNextEntry()) != null) {
                    File newFile = new File(destDir, entry.getName());
                    if (entry.isDirectory()) {
                        // Create directories as needed
                        newFile.mkdirs();
                    } else {
                        // Ensure parent directories exist
                        newFile.getParentFile().mkdirs();
                        // Write the entry to a temporary file
                        try (BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(newFile))) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = zipInputStream.read(buffer)) > 0) {
                                bos.write(buffer, 0, bytesRead);
                            }
                        }
                        // Convert the file to a MultipartFile
                        try (InputStream inputStream = new FileInputStream(newFile)) {
                            MultipartFile multipartFile =
                                    new FileMultipartFile(
                                    newFile.getName(), newFile.getName(), "application/octet-stream", inputStream.readAllBytes()
                            );
                            extractedFiles.add(multipartFile);
                        }
                    }
                    zipInputStream.closeEntry();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            extractedFiles.add(file);
        }
        return extractedFiles;
    }
}
