package com.orchestra.api.mapper;

import com.orchestra.api.dto.response.OpenApiUploadResponse;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.media.Schema;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class OpenApiMapper {

    public OpenApiUploadResponse toResponse(UUID id, OpenAPI openAPI) {
        if (openAPI == null || openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            throw new IllegalArgumentException("No paths found in OpenAPI spec");
        }

        Map<String, Map<String, OpenApiUploadResponse.Item>> byPath = new LinkedHashMap<>();
        openAPI.getPaths().forEach((path, pathItem) -> {
            if (pathItem == null || pathItem.readOperationsMap() == null) {
                return;
            }

            Map<String, OpenApiUploadResponse.Item> methods =
                    byPath.computeIfAbsent(path, key -> new LinkedHashMap<>());

            pathItem.readOperationsMap().forEach((method, operation) -> {
                if (operation == null) {
                    return;
                }

                OpenApiUploadResponse.Item item = new OpenApiUploadResponse.Item();
                item.method = method.name();
                item.requestSchema = extractRequestSchema(operation, openAPI);
                item.responses = extractResponses(operation);
                methods.put(method.name(), item);
            });
        });

        List<String> priority = List.of("POST", "PUT", "GET", "DELETE", "PATCH", "OPTIONS", "HEAD");
        Map<String, OpenApiUploadResponse.Item> flat = new LinkedHashMap<>();
        byPath.forEach((path, methods) -> {
            OpenApiUploadResponse.Item chosen = null;
            for (String method : priority) {
                if (methods.containsKey(method)) {
                    chosen = methods.get(method);
                    break;
                }
            }
            if (chosen == null && !methods.isEmpty()) {
                chosen = methods.values().iterator().next();
            }
            if (chosen != null) {
                flat.put(path, chosen);
            }
        });

        OpenApiUploadResponse response = new OpenApiUploadResponse();
        response.setId(id);
        response.setApiList(flat);
        return response;
    }

    private Map<String, Object> extractRequestSchema(Operation operation, OpenAPI openAPI) {
        Map<String, Object> schema = new LinkedHashMap<>();
        if (operation.getRequestBody() == null || operation.getRequestBody().getContent() == null) {
            return schema;
        }

        Schema<?> requestSchema = null;
        if (operation.getRequestBody().getContent().get("application/json") != null) {
            requestSchema = operation.getRequestBody().getContent().get("application/json").getSchema();
        }
        if (requestSchema == null) {
            var firstContent = operation.getRequestBody().getContent().values().stream().findFirst();
            if (firstContent.isPresent()) {
                requestSchema = firstContent.get().getSchema();
            }
        }
        if (requestSchema != null) {
            schema.putAll(flattenSchema(requestSchema, openAPI));
        }
        return schema;
    }

    private Map<String, String> extractResponses(Operation operation) {
        Map<String, String> responses = new LinkedHashMap<>();
        if (operation.getResponses() == null) {
            return responses;
        }
        operation.getResponses().forEach((code, response) ->
                responses.put(code, response != null && response.getDescription() != null ? response.getDescription() : "")
        );
        return responses;
    }

    private Map<String, Object> flattenSchema(Schema<?> schema, OpenAPI openAPI) {
        Map<String, Object> output = new LinkedHashMap<>();
        if (schema == null) {
            return output;
        }

        if (schema.get$ref() != null) {
            output.put("$ref", schema.get$ref());
            String name = schema.get$ref().substring(schema.get$ref().lastIndexOf('/') + 1);
            if (openAPI != null && openAPI.getComponents() != null && openAPI.getComponents().getSchemas() != null) {
                Schema<?> target = openAPI.getComponents().getSchemas().get(name);
                if (target != null && target.getType() != null) {
                    output.put("type", target.getType());
                }
            }
            return output;
        }

        if (schema instanceof io.swagger.v3.oas.models.media.ArraySchema arraySchema) {
            Map<String, Object> items = flattenSchema(arraySchema.getItems(), openAPI);
            output.put("type", "array");
            if (!items.isEmpty()) {
                output.put("items", items);
            }
            return output;
        }

        if (schema.getProperties() != null && !schema.getProperties().isEmpty()) {
            schema.getProperties().forEach((key, value) -> {
                Map<String, Object> child = flattenSchema((Schema<?>) value, openAPI);
                if (child.isEmpty() && value.getType() != null) {
                    child.put("type", value.getType());
                }
                output.put(key, child.isEmpty() ? (value.getType() != null ? value.getType() : "object") : child);
            });
            return output;
        }

        if (schema instanceof io.swagger.v3.oas.models.media.ComposedSchema composedSchema) {
            if (composedSchema.getOneOf() != null && !composedSchema.getOneOf().isEmpty()) {
                output.put("oneOf", composedSchema.getOneOf().stream().map(item -> flattenSchema(item, openAPI)).toList());
            }
            if (composedSchema.getAnyOf() != null && !composedSchema.getAnyOf().isEmpty()) {
                output.put("anyOf", composedSchema.getAnyOf().stream().map(item -> flattenSchema(item, openAPI)).toList());
            }
            if (composedSchema.getAllOf() != null && !composedSchema.getAllOf().isEmpty()) {
                output.put("allOf", composedSchema.getAllOf().stream().map(item -> flattenSchema(item, openAPI)).toList());
            }
            if (schema.getType() != null) {
                output.putIfAbsent("type", schema.getType());
            }
            return output;
        }

        if (schema.getType() != null) {
            output.put("type", schema.getType());
        }
        return output;
    }
}
