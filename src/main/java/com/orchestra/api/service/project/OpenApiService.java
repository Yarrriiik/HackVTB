package com.orchestra.api.service.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.api.dto.response.OpenApiUploadResponse;
import com.orchestra.api.entity.OpenApiSpecEntity;
import com.orchestra.api.mapper.OpenApiMapper;
import com.orchestra.api.repository.entity.OpenApiSpecRepository;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.parser.OpenAPIV3Parser;
import io.swagger.v3.parser.core.models.ParseOptions;
import io.swagger.v3.parser.core.models.SwaggerParseResult;
import jakarta.transaction.Transactional;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class OpenApiService {

    private final OpenApiSpecRepository specRepo;
    private final OpenApiMapper mapper;
    private final ObjectMapper json;

    public OpenApiService(OpenApiSpecRepository specRepo, OpenApiMapper mapper) {
        this.specRepo = specRepo;
        this.mapper = mapper;
        this.json = new ObjectMapper();
    }

    @Transactional
    public OpenApiUploadResponse upload(MultipartFile file, String name) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty file");
        }

        final String sourceText;
        try {
            sourceText = new String(file.getBytes(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot read file");
        }

        ParseOptions options = new ParseOptions();
        options.setResolve(false);
        SwaggerParseResult parseResult = new OpenAPIV3Parser().readContents(sourceText, null, options);
        OpenAPI openAPI = parseResult.getOpenAPI();
        if (openAPI == null || openAPI.getPaths() == null || openAPI.getPaths().isEmpty()) {
            String message = (parseResult.getMessages() != null && !parseResult.getMessages().isEmpty())
                    ? String.join("; ", parseResult.getMessages())
                    : "OpenAPI parse failed";
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }

        final String normalizedJson;
        try {
            normalizedJson = json.writeValueAsString(openAPI);
        } catch (Exception e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot serialize OpenAPI to JSON");
        }

        UUID id = UUID.randomUUID();
        OpenApiSpecEntity entity = new OpenApiSpecEntity();
        entity.setId(id);
        entity.setName(name != null ? name : file.getOriginalFilename());
        entity.setFileName(file.getOriginalFilename());
        entity.setSpecJson(normalizedJson);
        entity.setCreatedAt(LocalDateTime.now());
        specRepo.save(entity);

        return mapper.toResponse(id, openAPI);
    }
}
