package com.orchestra.api.controller.project;

import com.orchestra.api.dto.response.SequenceDiagramResponse;
import com.orchestra.api.service.project.SequenceService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/sequence")
public class SequenceController {

    private final SequenceService service;

    public SequenceController(SequenceService service) { this.service = service; }

    @PostMapping(value = {"/uploadSequence", "/upload"}, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<SequenceDiagramResponse> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "format", required = false, defaultValue = "PLANTUML") String format) throws Exception {
        return ResponseEntity.ok(service.uploadSequence(name, format, file));
    }
}

