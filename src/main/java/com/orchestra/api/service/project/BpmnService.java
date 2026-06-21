package com.orchestra.api.service.project;

import com.orchestra.api.dto.response.BpmnDiagramResponse;
import com.orchestra.api.entity.ProcessDiagramEntity;
import com.orchestra.api.entity.ProcessStepEntity;
import com.orchestra.api.entity.ProcessTransitionEntity;
import com.orchestra.api.mapper.BpmnMapper;
import com.orchestra.api.model.project.BpmnDiagram;
import com.orchestra.api.parser.BpmnParser;
import com.orchestra.api.repository.entity.ProcessDiagramRepository;
import com.orchestra.api.repository.entity.ProcessStepRepository;
import com.orchestra.api.repository.entity.ProcessTransitionRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
public class BpmnService {

    private final BpmnParser parser;
    private final BpmnMapper mapper;
    private final ProcessDiagramRepository diagramRepo;
    private final ProcessStepRepository stepRepo;
    private final ProcessTransitionRepository transitionRepo;

    public BpmnService(
            BpmnParser parser,
            BpmnMapper mapper,
            ProcessDiagramRepository diagramRepo,
            ProcessStepRepository stepRepo,
            ProcessTransitionRepository transitionRepo
    ) {
        this.parser = parser;
        this.mapper = mapper;
        this.diagramRepo = diagramRepo;
        this.stepRepo = stepRepo;
        this.transitionRepo = transitionRepo;
    }

    @Transactional
    public BpmnDiagramResponse processUploadedDiagram(MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Uploaded file is empty");
        }

        BpmnDiagram model = parser.parse(file.getInputStream());
        ProcessDiagramEntity diagramEntity = mapper.toEntity(model);
        diagramEntity = diagramRepo.save(diagramEntity);

        List<ProcessStepEntity> stepEntities = diagramEntity.getSteps();
        if (stepEntities != null && !stepEntities.isEmpty()) {
            stepRepo.saveAll(stepEntities);
        }

        List<ProcessTransitionEntity> transitionEntities = diagramEntity.getTransitions();
        if (transitionEntities != null && !transitionEntities.isEmpty()) {
            transitionRepo.saveAll(transitionEntities);
        }

        return mapper.toResponse(diagramEntity);
    }
}
