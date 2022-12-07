package com.profatar.user.config;

import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import javax.annotation.PreDestroy;
import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Component
public class TemporaryFolderHandler {
    private static final Logger LOG = LoggerFactory.getLogger(TemporaryFolderHandler.class);
    @Bean
    public void createTmpDir() throws IOException {
        Path tmp = Files.createTempDirectory("tmp");
        String path = tmp.toFile().getAbsolutePath();
        LOG.info("Created a temporary folder at " + path);
    }
}
