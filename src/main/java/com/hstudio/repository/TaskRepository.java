package com.hstudio.repository;

import com.hstudio.model.Task;
import com.hstudio.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

/** Fournit les recherches en base pour les t. */
public interface TaskRepository extends JpaRepository<Task, UUID> {
    /** Recherche les tâches attribuées à l’utilisateur indiqué. */
    List<Task> findByAssignedUser(User assignedUser);
}