package com.zzy.drai.service;

public record ExportedReport(
        String filename,
        String contentType,
        byte[] bytes
) {
}
