package net.ensah.demo;

import net.ensah.demo.dtos.UserRequestDto;
import net.ensah.demo.dtos.UserResponseDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.http.*;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class DemoApplicationTests {

    @Container
    @ServiceConnection
    private static PostgreSQLContainer<?> postgreSQLContainer=new PostgreSQLContainer<>("postgres:latest");

    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate restTemplate;

    private String baseUrl() {
        return "http://localhost:" + port + "/api/v1/users";
    }

    @Test
    void getUsers_public_shouldReturn200AndList() {
        ResponseEntity<UserResponseDto[]> resp = restTemplate.getForEntity(baseUrl(), UserResponseDto[].class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(resp.getBody()).isNotNull();
    }

    @Test
    void create_withoutToken_shouldReturn401() {
        UserRequestDto req = new UserRequestDto("john","doe","john.doe@test.com","1234");
        ResponseEntity<String> resp = restTemplate.postForEntity(baseUrl(), req, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void create_withUserRole_shouldReturn403() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("user-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserRequestDto req = new UserRequestDto("alice","doe","alice.doe@test.com","1234");
        HttpEntity<UserRequestDto> entity = new HttpEntity<>(req, headers);
        ResponseEntity<String> resp = restTemplate.postForEntity(baseUrl(), entity, String.class);
        assertThat(resp.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
    }

    @Test
    void create_withAdminRole_shouldCreateAndReturn201AndPersist() {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth("admin-token");
        headers.setContentType(MediaType.APPLICATION_JSON);
        UserRequestDto req = new UserRequestDto("bob","martin","bob.martin@test.com","pw");
        HttpEntity<UserRequestDto> entity = new HttpEntity<>(req, headers);

        ResponseEntity<UserResponseDto> createResp = restTemplate.postForEntity(baseUrl(), entity, UserResponseDto.class);
        assertThat(createResp.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createResp.getBody()).isNotNull();
        assertThat(createResp.getBody().email()).isEqualTo("bob.martin@test.com");

        // Then GET should include the created user
        ResponseEntity<List> getResp = restTemplate.getForEntity(baseUrl(), List.class);
        assertThat(getResp.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(getResp.getBody()).isNotNull();
        assertThat(getResp.getBody().toString()).contains("bob.martin@test.com");
    }

    @TestConfiguration
    static class TestJwtConfig {
        @Bean
        JwtDecoder jwtDecoder() {
            return token -> {
                // simulate validating tokens and building Jwt with roles
                Map<String, Object> claims = new HashMap<>();
                Map<String, Object> headers = Map.of("alg", "none");
                if ("admin-token".equals(token)) {
                    claims.put("preferred_username", "admin");
                    claims.put("realm_access", Map.of("roles", List.of("ADMIN")));
                } else if ("user-token".equals(token)) {
                    claims.put("preferred_username", "user");
                    claims.put("realm_access", Map.of("roles", List.of("USER")));
                } else {
                    throw new BadJwtException("Invalid token");
                }
                Instant now = Instant.now();
                return new Jwt(token, now, now.plusSeconds(3600), headers, claims);
            };
        }
    }
}
