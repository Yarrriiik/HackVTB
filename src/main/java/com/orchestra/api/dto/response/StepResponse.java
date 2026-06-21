package com.orchestra.api.dto.response;

import java.util.List;
import java.util.UUID;

public class StepResponse {
    private UUID id;
    private String stepId;
    private String name;
    private String action;
    private String actorFrom;
    private String actorTo;
    private List<String> nextSteps;

    public StepResponse() {}

    public StepResponse(
            UUID id,
            String stepId,
            String name,
            String action,
            String actorFrom,
            String actorTo,
            List<String> nextSteps
    ) {
        this.id = id;
        this.stepId = stepId;
        this.name = name;
        this.action = action;
        this.actorFrom = actorFrom;
        this.actorTo = actorTo;
        this.nextSteps = nextSteps;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public String getStepId() { return stepId; }
    public void setStepId(String stepId) { this.stepId = stepId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getAction() { return action; }
    public void setAction(String action) { this.action = action; }
    public String getActorFrom() { return actorFrom; }
    public void setActorFrom(String actorFrom) { this.actorFrom = actorFrom; }
    public String getActorTo() { return actorTo; }
    public void setActorTo(String actorTo) { this.actorTo = actorTo; }
    public List<String> getNextSteps() { return nextSteps; }
    public void setNextSteps(List<String> nextSteps) { this.nextSteps = nextSteps; }
}
