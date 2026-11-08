package com.hstudio.controller;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.hstudio.model.Role;
import com.hstudio.model.User;
import com.hstudio.service.UserService;

/** V les regles acces aux ops sur les utilisateurs. */
@SpringBootTest
@AutoConfigureMockMvc
class UserControllerSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    /** V que la direction marketing peut consulter la liste des commerciaux. */
    @Test
    @WithMockUser(authorities = "Directeur_Marketing")
    void marketingDirectorCanAccessCommercialList() throws Exception {
        when(userService.findCommercials()).thenReturn(List.of(User.builder()
                .id(UUID.randomUUID())
                .firstName("Alice")
                .lastName("Commerciale")
                .email("alice@example.com")
                .role(Role.Commercial)
                .active(true)
                .build()));

        mockMvc.perform(get("/users/commerciaux"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Alice Commerciale"))
                .andExpect(jsonPath("$[0].role").value("Commercial"));
    }

    /** V qu un commercial ne peut pas consulter la liste complete. */
    @Test
    @WithMockUser(authorities = "Commercial")
    void commercialCannotAccessFullCommercialList() throws Exception {
        mockMvc.perform(get("/users/commerciaux"))
                .andExpect(status().isForbidden());
    }
}
