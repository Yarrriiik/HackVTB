package com.orchestra.api.service.project;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.api.dto.response.SequenceDiagramResponse;
import com.orchestra.api.entity.ProcessDiagramEntity;
import com.orchestra.api.entity.ProcessStepEntity;
import com.orchestra.api.entity.ProcessTransitionEntity;
import com.orchestra.api.entity.SequenceDiagramRawEntity;
import com.orchestra.api.parser.SequenceParser;
import com.orchestra.api.repository.entity.ProcessDiagramRepository;
import com.orchestra.api.repository.entity.ProcessStepRepository;
import com.orchestra.api.repository.entity.ProcessTransitionRepository;
import com.orchestra.api.repository.entity.SequenceDiagramRawRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

@Service
public class SequenceService {

    private final SequenceDiagramRawRepository rawRepo;
    private final ProcessDiagramRepository diagramRepo;
    private final ProcessStepRepository stepRepo;
    private final ProcessTransitionRepository transitionRepo;
    private final SequenceParser parser;

    public SequenceService(
            SequenceDiagramRawRepository rawRepo,
            ProcessDiagramRepository diagramRepo,
            ProcessStepRepository stepRepo,
            ProcessTransitionRepository transitionRepo,
            SequenceParser parser
    ) {
        this.rawRepo = rawRepo;
        this.diagramRepo = diagramRepo;
        this.stepRepo = stepRepo;
        this.transitionRepo = transitionRepo;
        this.parser = parser;
    }

    @Transactional
    public SequenceDiagramResponse uploadSequence(String name, String format, MultipartFile file) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Empty file");
        }

        String effectiveName = (name != null && !name.isBlank())
                ? name
                : (file.getOriginalFilename() != null && !file.getOriginalFilename().isBlank()
                ? file.getOriginalFilename()
                : "Sequence");

        UUID rawId = UUID.randomUUID();
        SequenceDiagramRawEntity raw = new SequenceDiagramRawEntity();
        raw.setId(rawId);
        raw.setName(effectiveName);
        raw.setFormat(format);
        raw.setRawContent(new String(file.getBytes(), StandardCharsets.UTF_8));
        raw.setCreatedAt(LocalDateTime.now());
        rawRepo.save(raw);

        SequenceParser.ParsedSequence parsed = parser.parse(raw.getRawContent());

        UUID diagramId = UUID.randomUUID();
        ProcessDiagramEntity diagram = new ProcessDiagramEntity();
        diagram.setId(diagramId);
        diagram.setName(effectiveName);
        diagram.setType("SEQUENCE");
        diagram.setStatus("READY");
        diagram.setCreatedAt(LocalDateTime.now());
        diagram.setUpdatedAt(LocalDateTime.now());
        diagram.setSteps(new ArrayList<>());
        diagram.setTransitions(new ArrayList<>());
        diagramRepo.save(diagram);

        Map<String, ProcessStepEntity> stepsById = new LinkedHashMap<>();
        for (SequenceDiagramResponse.Step step : parsed.steps()) {
            ProcessStepEntity entity = new ProcessStepEntity();
            entity.setId(UUID.randomUUID());
            entity.setDiagram(diagram);
            entity.setStepId(step.stepId);
            entity.setName(step.name);
            entity.setActorFrom(step.from);
            entity.setActorTo(step.to);
            entity.setAction(step.action);
            entity.setNextSteps(new ObjectMapper().writeValueAsString(step.next));
            stepRepo.save(entity);
            stepsById.put(step.stepId, entity);
            diagram.getSteps().add(entity);
        }

        for (SequenceDiagramResponse.Step step : parsed.steps()) {
            for (String nextStepId : step.next) {
                ProcessTransitionEntity transition = new ProcessTransitionEntity();
                transition.setId(UUID.randomUUID());
                transition.setDiagram(diagram);
                transition.setFromStep(stepsById.get(step.stepId));
                transition.setToStep(stepsById.get(nextStepId));
                transitionRepo.save(transition);
                diagram.getTransitions().add(transition);
            }
        }

        SequenceDiagramResponse response = new SequenceDiagramResponse();
        response.setId(diagramId);
        response.setName(diagram.getName());
        response.setType("SEQUENCE");
        response.setActors(parsed.actors());
        response.setSteps(parsed.steps());
        return response;
    }
}
