package com.likelion.dev_community.domain.attachment.service;

import com.likelion.dev_community.common.exception.CustomException;
import com.likelion.dev_community.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    private final Path rootDir;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.rootDir = Path.of(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(rootDir);
        } catch (IOException e) {
            throw new IllegalStateException("업로드 디렉토리를 생성할 수 없습니다: " + rootDir, e);
        }
    }

    public String store(MultipartFile file, String extension) {
        String storedFilename = UUID.randomUUID() + "." + extension;
        Path target = rootDir.resolve(storedFilename);
        try {
            file.transferTo(target);
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", storedFilename, e);
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
        return storedFilename;
    }

    public Resource loadAsResource(String storedFilename) {
        Path file = rootDir.resolve(storedFilename).normalize();
        if (!file.startsWith(rootDir)) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
        try {
            Resource resource = new UrlResource(file.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new CustomException(ErrorCode.NOT_FOUND);
            }
            return resource;
        } catch (MalformedURLException e) {
            throw new CustomException(ErrorCode.NOT_FOUND);
        }
    }

    public void delete(String storedFilename) {
        Path file = rootDir.resolve(storedFilename).normalize();
        if (!file.startsWith(rootDir)) {
            return;
        }
        try {
            Files.deleteIfExists(file);
        } catch (IOException e) {
            log.warn("파일 삭제 실패: {}", storedFilename, e);
        }
    }
}
