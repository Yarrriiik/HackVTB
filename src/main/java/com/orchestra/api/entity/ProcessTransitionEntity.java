package com.orchestra.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.util.UUID;

@Entity
@Table(name = "process_transition")
public class ProcessTransitionEntity {

    @Id
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "diagram_id")
    private ProcessDiagramEntity diagram;

    @ManyToOne
    @JoinColumn(name = "from_step")
    private ProcessStepEntity fromStep;

    @ManyToOne
    @JoinColumn(name = "to_step")
    private ProcessStepEntity toStep;

    public ProcessTransitionEntity() {}

    public ProcessTransitionEntity(
            UUID id,
            ProcessDiagramEntity diagram,
            ProcessStepEntity fromStep,
            ProcessStepEntity toStep
    ) {
        this.id = id;
        this.diagram = diagram;
        this.fromStep = fromStep;
        this.toStep = toStep;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public ProcessDiagramEntity getDiagram() { return diagram; }
    public void setDiagram(ProcessDiagramEntity diagram) { this.diagram = diagram; }
    public ProcessStepEntity getFromStep() { return fromStep; }
    public void setFromStep(ProcessStepEntity fromStep) { this.fromStep = fromStep; }
    public ProcessStepEntity getToStep() { return toStep; }
    public void setToStep(ProcessStepEntity toStep) { this.toStep = toStep; }
    public String getCondition() { return null; }
}
