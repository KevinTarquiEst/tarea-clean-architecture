package org.cat.usercleanarchitecture.infraestructure.adapters.input;

import org.cat.usercleanarchitecture.aplication.ports.input.IUserUseCase;
import org.cat.usercleanarchitecture.domain.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IUserUseCase userUseCase;

    @Test
    void returnsMatchingUsersWhenEmailFilterMatches() throws Exception {
        when(userUseCase.findByEmailDomain("gmail.com"))
                .thenReturn(List.of(new User("Ana", "Gomez", "ana@gmail.com", "0000000")));

        mockMvc.perform(get("/v1/users/by-email").param("email", "gmail.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].email").value("ana@gmail.com"));
    }

    @Test
    void returnsEmptyListWhenNoUserMatches() throws Exception {
        when(userUseCase.findByEmailDomain("yahoo.com")).thenReturn(List.of());

        mockMvc.perform(get("/v1/users/by-email").param("email", "yahoo.com"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void returnsBadRequestWhenEmailFilterIsBlank() throws Exception {
        when(userUseCase.findByEmailDomain(anyString()))
                .thenThrow(new IllegalArgumentException("El parámetro 'email' es requerido y no puede estar vacío"));

        mockMvc.perform(get("/v1/users/by-email").param("email", "   "))
                .andExpect(status().isBadRequest());
    }

    @Test
    void returnsBadRequestWhenEmailFilterIsMissing() throws Exception {
        mockMvc.perform(get("/v1/users/by-email"))
                .andExpect(status().isBadRequest());
    }
}
