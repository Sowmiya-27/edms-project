package com.edms.edms.dto;

public class DocumentTypeStatsDTO {

    private String type;
    private long count;

    public DocumentTypeStatsDTO(String type, long count) {
        this.type = type;
        this.count = count;
    }

    public String getType() {
        return type;
    }

    public long getCount() {
        return count;
    }
}