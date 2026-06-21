package com.orchestra.api.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.orchestra.api.dto.response.BpmnDiagramResponse;
import com.orchestra.api.dto.response.StepResponse;
import com.orchestra.api.dto.response.TransitionResponse;
import com.orchestra.api.entity.ProcessDiagramEntity;
import com.orchestra.api.entity.ProcessStepEntity;
import com.orchestra.api.entity.ProcessTransitionEntity;
import com.orchestra.api.model.core.BpmnElement;
import com.orchestra.api.model.project.BpmnDiagram;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class BpmnMapper {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public ProcessDiagramEntity toEntity(BpmnDiagram model) {
        UUID id = model.getId() != null ? model.getId() : UUID.randomUUID();

        ProcessDiagramEntity diagramEntity = new ProcessDiagramEntity(
                id,
                model.getName(),
                model.getType(),
                "PARSED",
                LocalDateTime.now(),
                LocalDateTime.now(),
                new ArrayList<>(),
                new ArrayList<>(),
                new ArrayList<>()
        );

        Map<String, ProcessStepEntity> stepEntityMap = new HashMap<>();
        for (BpmnElement step : model.getSteps()) {
            ProcessStepEntity stepEntity = new ProcessStepEntity(
                    UUID.randomUUID(),
                    diagramEntity,
                    step.getStepId(),
                    step.getName(),
                    step.getActorFrom(),
                    step.getActorTo(),
                    step.getAction(),
                    serializeNextSteps(step.getNextSteps()),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>(),
                    new ArrayList<>()
            );
            stepEntityMap.put(step.getStepId(), stepEntity);
            diagramEntity.getSteps().add(stepEntity);
        }

        for (ProcessStepEntity fromStep : stepEntityMap.values()) {
            List<String> nextIds = deserializeNextSteps(fromStep.getNextSteps());
            for (String nextId : nextIds) {
                ProcessStepEntity toStep = stepEntityMap.get(nextId);
                if (toStep != null) {
                    ProcessTransitionEntity transition = new ProcessTransitionEntity(
                            UUID.randomUUID(),
                            diagramEntity,
                            fromStep,
                            toStep
                    );
                    diagramEntity.getTransitions().add(transition);
                }
            }
        }

        return diagramEntity;
    }

    private String serializeNextSteps(List<String> nextSteps) {
        if (nextSteps == null || nextSteps.isEmpty()) {
            return "[]";
        }
        try {
            return objectMapper.writeValueAsString(nextSteps);
        } catch (JsonProcessingException e) {
            return "[]";
        }
    }

    private List<String> deserializeNextSteps(String nextSteps) {
        if (nextSteps == null || nextSteps.isEmpty() || "[]".equals(nextSteps)) {
            return List.of();
        }
        try {
            return objectMapper.readValue(nextSteps, new TypeReference<List<String>>() {});
        } catch (JsonProcessingException e) {
            if (nextSteps.startsWith("[") && nextSteps.endsWith("]")) {
                String content = nextSteps.substring(1, nextSteps.length() - 1);
                if (content.isEmpty()) {
                    return List.of();
                }
                return Arrays.asList(content.split(","));
            }
            return Arrays.asList(nextSteps.split(","));
        }
    }

    public BpmnDiagramResponse toResponse(ProcessDiagramEntity diagram) {
        List<StepResponse> stepResponses = diagram.getSteps().stream()
                .map(this::toStepResponse)
                .toList();

        List<TransitionResponse> transitionResponses = diagram.getTransitions().stream()
                .map(this::toTransitionResponse)
                .toList();

        return new BpmnDiagramResponse(
                diagram.getId(),
                diagram.getName(),
                diagram.getType(),
                diagram.getStatus(),
                stepResponses,
                transitionResponses
        );
    }

    private StepResponse toStepResponse(ProcessStepEntity step) {
        return new StepResponse(
                step.getId(),
                step.getStepId(),
                step.getName(),
                step.getAction(),
                step.getActorFrom(),
                step.getActorTo(),
                deserializeNextSteps(step.getNextSteps())
        );
    }

    private TransitionResponse toTransitionResponse(ProcessTransitionEntity transition) {
        return new TransitionResponse(
                transition.getFromStep().getStepId(),
                transition.getToStep().getStepId(),
                transition.getCondition()
        );
    }
}
