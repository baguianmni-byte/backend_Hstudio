package com.hstudio.dto;

import com.hstudio.model.Task;

import java.time.LocalDateTime;
import java.util.UUID;
/** Presente une t dans les r API. */
public record TaskResponse(
        UUID id,
        UUID demandeId,
        UserSummary assignedUser,
        LocalDateTime dueDate,
        String description,
        boolean completed,
        LocalDateTime createdAt
) {
    /** Convertit les donn metier en objet de r API. */
    public static TaskResponse from(Task task) {
        return new TaskResponse(
                task.getId(),
                task.getDemande().getId(),
                UserSummary.from(task.getAssignedUser()),
                task.getDueDate(),
                task.getDescription(),
                task.isCompleted(),
                task.getCreatedAt()
        );
    }
}
