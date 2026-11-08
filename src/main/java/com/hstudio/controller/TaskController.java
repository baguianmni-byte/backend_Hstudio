package com.hstudio.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.hstudio.dto.TaskCreateRequest;
import com.hstudio.dto.TaskResponse;
import com.hstudio.model.User;
import com.hstudio.service.TaskService;
import com.hstudio.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/** Expose la cr et le suivi des t. */
@RestController
@RequestMapping("/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final UserService userService;

    /** Cr une t liee a une demande et son utilisateur. */
    @PostMapping("/demandes/{demandeId}")
    public ResponseEntity<TaskResponse> creerTache(
            @PathVariable UUID demandeId,
            @Valid @RequestBody TaskCreateRequest request,
            @AuthenticationPrincipal UserDetails userDetails
    ) {
        User utilisateur = userService.findByEmail(userDetails.getUsername());

        TaskResponse nouvelleTache = TaskResponse.from(
                taskService.creerTache(demandeId, request.toEntity(), utilisateur)
        );
        return new ResponseEntity<>(nouvelleTache, HttpStatus.CREATED);
    }

    /** Renvoie les t associ au compte connect. */
    @GetMapping
    public ResponseEntity<List<TaskResponse>> listerMesTaches(@AuthenticationPrincipal UserDetails userDetails) {
        User utilisateur = userService.findByEmail(userDetails.getUsername());

        List<TaskResponse> taches = taskService.obtenirTachesUtilisateur(utilisateur).stream()
                .map(TaskResponse::from)
                .toList();
        return ResponseEntity.ok(taches);
    }

    /** Change etat de la t indiquee. */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<TaskResponse> basculerTache(@PathVariable UUID id) {
        TaskResponse tacheMaj = TaskResponse.from(taskService.basculerStatutTache(id));
        return ResponseEntity.ok(tacheMaj);
    }
}