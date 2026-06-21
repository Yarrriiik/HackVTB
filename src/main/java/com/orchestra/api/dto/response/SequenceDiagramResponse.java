package com.orchestra.api.dto.response;

import java.util.List;
import java.util.UUID;

public class SequenceDiagramResponse {
    private UUID id;
    private String name;
    private String type;
    private List<String> actors;
    private List<Step> steps;

    public static class Step {
        public String stepId;
        public String name;
        public String from;
        public String to;
        public String action;
        public List<String> next;
    }

    public SequenceDiagramResponse() {}

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public List<String> getActors() { return actors; }
    public void setActors(List<String> actors) { this.actors = actors; }
    public List<Step> getSteps() { return steps; }
    public void setSteps(List<Step> steps) { this.steps = steps; }
}
