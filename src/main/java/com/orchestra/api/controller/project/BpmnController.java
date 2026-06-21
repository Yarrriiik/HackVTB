package com.orchestra.api.controller.project;

import com.orchestra.api.dto.response.BpmnDiagramResponse;
import com.orchestra.api.service.project.BpmnService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/bpmn")
public class BpmnController {

    private final BpmnService bpmnService;

    public BpmnController(BpmnService bpmnService) {
        this.bpmnService = bpmnService;
    }

    @PostMapping("/upload")
    public ResponseEntity<?> uploadBpmn(@RequestParam("file") MultipartFile file) {
        try {
            BpmnDiagramResponse response = bpmnService.processUploadedDiagram(file);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Failed to parse BPMN file: " + e.getMessage());
        }
    }
}
