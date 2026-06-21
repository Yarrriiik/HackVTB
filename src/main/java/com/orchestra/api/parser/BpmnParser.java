package com.orchestra.api.parser;

import com.orchestra.api.model.core.BpmnElement;
import com.orchestra.api.model.project.BpmnDiagram;
import org.camunda.bpm.model.bpmn.Bpmn;
import org.camunda.bpm.model.bpmn.BpmnModelInstance;
import org.camunda.bpm.model.bpmn.instance.FlowNode;
import org.camunda.bpm.model.bpmn.instance.Process;
import org.camunda.bpm.model.bpmn.instance.SequenceFlow;
import org.springframework.stereotype.Component;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@Component
public class BpmnParser {

    public BpmnDiagram parse(InputStream inputStream) {
        BpmnModelInstance model = Bpmn.readModelFromStream(inputStream);

        String diagramName = extractDiagramName(model);
        List<BpmnElement> steps = extractSteps(model);
        extractTransitions(model, steps);

        return createBpmnDiagram(steps, diagramName);
    }

    private String extractDiagramName(BpmnModelInstance model) {
        Collection<Process> processes = model.getModelElementsByType(Process.class);
        if (processes.isEmpty()) {
            return "Unnamed Process";
        }

        Process process = processes.iterator().next();
        if (process.getName() != null && !process.getName().isEmpty()) {
            return process.getName();
        }
        return process.getId();
    }

    public List<BpmnElement> extractSteps(BpmnModelInstance model) {
        List<BpmnElement> steps = new ArrayList<>();
        Collection<FlowNode> nodes = model.getModelElementsByType(FlowNode.class);

        for (FlowNode node : nodes) {
            BpmnElement element = new BpmnElement();
            element.setStepId(node.getId());
            element.setName(node.getName());
            element.setNextSteps(new ArrayList<>());
            element.setActorFrom(null);
            element.setActorTo(null);
            element.setAction(extractAction(node.getName()));
            steps.add(element);
        }
        return steps;
    }

    private String extractAction(String name) {
        if (name == null) {
            return null;
        }

        String regex = "(GET|POST|PUT|DELETE|PATCH)\\s+[^\\s]+";
        var matcher = java.util.regex.Pattern.compile(regex).matcher(name);
        return matcher.find() ? matcher.group() : null;
    }

    public void extractTransitions(BpmnModelInstance model, List<BpmnElement> steps) {
        Collection<SequenceFlow> flows = model.getModelElementsByType(SequenceFlow.class);

        for (SequenceFlow flow : flows) {
            String sourceId = flow.getSource().getId();
            String targetId = flow.getTarget().getId();

            steps.stream()
                    .filter(e -> e.getStepId().equals(sourceId))
                    .findFirst()
                    .ifPresent(e -> e.getNextSteps().add(targetId));
        }
    }

    public BpmnDiagram createBpmnDiagram(List<BpmnElement> steps, String name) {
        BpmnDiagram diagram = new BpmnDiagram();
        diagram.setSteps(steps);
        diagram.setName(name);
        diagram.setType("BPMN");
        return diagram;
    }
}
