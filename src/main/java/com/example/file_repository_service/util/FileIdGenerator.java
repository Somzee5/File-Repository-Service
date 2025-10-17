package com.example.file_repository_service.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

public class FileIdGenerator {

    private static final SimpleDateFormat FORMATTER =
            new SimpleDateFormat("yyyyMMdd_HHmmss");


    public static String generate(Long tenantId) {
        String timestamp = FORMATTER.format(new Date());
        String randomPart = UUID.randomUUID()
                .toString()
                .replace("-", "")
                .substring(0, 6);

        return "CF_FR_" + tenantId + "_" + timestamp + "_" + randomPart;
    }

}