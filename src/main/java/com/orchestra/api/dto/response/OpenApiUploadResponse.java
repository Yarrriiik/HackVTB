package com.orchestra.api.dto.response;

import java.util.Map;
import java.util.UUID;

public class OpenApiUploadResponse {
    private UUID id;
    private Map<String, Item> apiList;

    public static class Item {
        public String method;
        public Map<String, Object> requestSchema;
        public Map<String, String> responses;
    }

    public OpenApiUploadResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public Map<String, Item> getApiList() { return apiList; }
    public void setApiList(Map<String, Item> apiList) { this.apiList = apiList; }
}
