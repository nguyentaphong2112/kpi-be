package vn.hbtplus.utils;


import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;

/**
 * FileMultipartFile: phiên bản thay thế cho MockMultipartFile để dùng trong runtime
 * Không phụ thuộc spring-test.
 */
public class FileMultipartFile implements MultipartFile {

    private final String name;
    private final String originalFilename;
    private final String contentType;
    private final byte[] content;

    public FileMultipartFile(String name, String originalFilename,
                             String contentType, byte[] content) {
        this.name = name;
        this.originalFilename = originalFilename;
        this.contentType = contentType;
        this.content = (content != null ? content : new byte[0]);
    }

    public FileMultipartFile(File newFile, String contentType) {
        this.name = newFile.getName();
        this.originalFilename = newFile.getName();
        this.contentType = contentType;
        try (FileInputStream fis = new FileInputStream(newFile)) {
            this.content = fis.readAllBytes();
        } catch (IOException e) {
            throw new UncheckedIOException("Error reading file content: " + newFile.getAbsolutePath(), e);
        }
    }

    @Override
    public String getName() {
        return this.name;
    }

    @Override
    public String getOriginalFilename() {
        return this.originalFilename;
    }

    @Override
    public String getContentType() {
        return this.contentType;
    }

    @Override
    public boolean isEmpty() {
        return this.content.length == 0;
    }

    @Override
    public long getSize() {
        return this.content.length;
    }

    @Override
    public byte[] getBytes() {
        return this.content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(this.content);
    }

    @Override
    public void transferTo(File dest) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(dest)) {
            fos.write(this.content);
        }
    }
}
