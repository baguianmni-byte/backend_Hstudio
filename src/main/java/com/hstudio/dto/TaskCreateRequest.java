package com.hstudio.dto;

import com.hstudio.model.Task;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
/** D les informations requises pour cr une t. */
public record TaskCreateRequest(
        @NotNull LocalDateTime dueDate,
        @NotBlank String description
) {
    /** Convertit les donn re en objet metier. */
    public Task toEntity() {
        Task task = new Task();
        task.setDueDate(dueDate);
        task.setDescription(description);
        return task;
    }
}
