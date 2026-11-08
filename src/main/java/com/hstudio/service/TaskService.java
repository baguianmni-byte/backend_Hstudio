package com.hstudio.service;

import com.hstudio.exception.ResourceNotFoundException;
import com.hstudio.model.Demande;
import com.hstudio.model.Task;
import com.hstudio.model.User;
import com.hstudio.repository.DemandeRepository;
import com.hstudio.repository.TaskRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/** G les t et leur etat. */
@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final DemandeRepository demandeRepository;

    /** Cr une t liee a une demande et son utilisateur. */
    @Transactional
    public Task creerTache(UUID demandeId, Task task, User utilisateur) {
        Demande demande = demandeRepository.findById(demandeId)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable avec l'ID : " + demandeId));
        
        task.setDemande(demande);
        task.setAssignedUser(utilisateur);
        task.setCompleted(false);
        return taskRepository.save(task);
    }

    /** Inverse etat de realisation de la t. */
    @Transactional
    public Task basculerStatutTache(UUID taskId) {
        Task task = taskRepository.findById(taskId)
                .orElseThrow(() -> new ResourceNotFoundException("Tâche introuvable avec l'ID : " + taskId));
        
        task.setCompleted(!task.isCompleted()); // Permet de marquer comme terminée ou de reprogrammer[cite: 1]
        return taskRepository.save(task);
    }

    /** Renvoie les t de utilisateur indique. */
    public List<Task> obtenirTachesUtilisateur(User utilisateur) {
        return taskRepository.findByAssignedUser(utilisateur);
    }
}