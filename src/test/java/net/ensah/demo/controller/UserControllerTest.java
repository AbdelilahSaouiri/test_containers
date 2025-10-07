package net.ensah.demo.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import net.ensah.demo.dtos.UserRequestDto;
import net.ensah.demo.dtos.UserResponseDto;
import net.ensah.demo.sec.SecurityConfig;
import net.ensah.demo.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import java.util.List;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    @WithMockUser(username = "user1",roles = {"USER"})
    void shouldReturnAllUsers() throws Exception {
        List<UserResponseDto> users = List.of(
                new UserResponseDto(1L, "abdelilah", "saouiri", "abdelilah@gmail.com"),
                new UserResponseDto(2L, "salma", "fes", "salma@gmail.com")
        );
        when(userService.getAllUsers()).thenReturn(users);
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(users.size()))
                .andExpect(jsonPath("$[0].email").value("abdelilah@gmail.com"));
    }

    @Test
    @WithMockUser(username = "user1",authorities = {"ADMIN"})
    void ShouldCreateUser() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("abdelilah","saouiri","abdelilah@gmail.com","1234");
        UserResponseDto userResponseDto=new UserResponseDto(1L, "abdelilah", "saouiri", "abdelilah@gmail.com");
        when(userService.addUser(userRequestDto)).thenReturn(userResponseDto);
        mockMvc.perform(post("/api/v1/users")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(userRequestDto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value(userResponseDto.email()))
                .andExpect(jsonPath("$.firstName").value(userResponseDto.firstName()))
                .andExpect(jsonPath("$.lastName").value(userResponseDto.lastName()));
    }

    @Test
    @WithMockUser(username = "user1",authorities = {"USER"})
    void shouldForbidNonAdminToCreateUser() throws Exception {
        UserRequestDto userRequestDto = new UserRequestDto("abdelilah","saouiri","abdelilah@gmail.com","1234");
        mockMvc.perform(post("/api/v1/users")
                .contentType("application/json")
                .content(objectMapper.writeValueAsString(userRequestDto))
        ).andExpect(status().isForbidden());

    }
}