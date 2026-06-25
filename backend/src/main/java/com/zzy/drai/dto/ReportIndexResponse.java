package com.zzy.drai.dto;

public record ReportIndexResponse(
        long reportId,
        int chunksStored,
        String status
) {
}
