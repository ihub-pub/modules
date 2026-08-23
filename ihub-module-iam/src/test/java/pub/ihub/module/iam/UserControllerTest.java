/*
 * Copyright (c) 2026 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package pub.ihub.module.iam;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;

import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.startsWith;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * {@link UserController} REST API 测试。
 *
 * @author IHub
 * @since 0.1.0
 */
class UserControllerTest {

    private static final String CREATE_BODY = """
        {"username": "alice", "password": "Passw0rd123", "email": "alice@example.com"}""";

    private MockMvc mockMvc;
    private UserService userService;

    @BeforeEach
    void setUp() {
        userService = new UserService();
        mockMvc = buildMockMvc(new IHubUserProperties(null));
    }

    /**
     * standalone MockMvc 默认不装配校验器，需显式注入 LocalValidatorFactoryBean；
     * ParameterMessageInterpolator 避免 EL 实现依赖（测试环境足够）。
     */
    private MockMvc buildMockMvc(IHubUserProperties properties) {
        LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setMessageInterpolator(new org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator());
        return MockMvcBuilders.standaloneSetup(new UserController(userService, properties))
            .setValidator(validator)
            .build();
    }

    private String createAlice() throws Exception {
        return mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(CREATE_BODY))
            .andExpect(status().isCreated())
            .andExpect(header().string("Location", startsWith("/api/users/")))
            .andExpect(jsonPath("$.username").value("alice"))
            .andExpect(jsonPath("$.status").value("ACTIVE"))
            // 响应体绝不能出现密码相关字段
            .andExpect(jsonPath("$.password").doesNotExist())
            .andReturn().getResponse().getContentAsString();
    }

    // ---- POST /api/users ----

    @Test
    void createUserReturns201WithLocation() throws Exception {
        createAlice();
    }

    @Test
    void createUserDuplicateReturns409() throws Exception {
        createAlice();
        mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON).content(CREATE_BODY))
            .andExpect(status().isConflict())
            .andExpect(jsonPath("$.detail").exists());
    }

    @Test
    void createUserWeakPasswordReturns400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON)
                .content("{\"username\": \"bob\", \"password\": \"weak\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasKey("password")));
    }

    @Test
    void createUserInvalidUsernameReturns400() throws Exception {
        mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON)
                .content("{\"username\": \"a b\", \"password\": \"Passw0rd123\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasKey("username")));
    }

    @Test
    void createUserAppliesDefaultRole() throws Exception {
        mockMvc = buildMockMvc(new IHubUserProperties("ROLE_USER"));
        mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON)
                .content("{\"username\": \"carol\", \"password\": \"Passw0rd123\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"));
    }

    @Test
    void createUserKeepsExplicitRolesAndSkipsDuplicateDefault() throws Exception {
        mockMvc = buildMockMvc(new IHubUserProperties("ROLE_USER"));
        mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON)
                .content("{\"username\": \"derek\", \"password\": \"Passw0rd123\", \"roles\": [\"ROLE_USER\", \"ROLE_ADMIN\"]}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.roles.length()").value(2))
            .andExpect(jsonPath("$.roles[0]").value("ROLE_USER"))
            .andExpect(jsonPath("$.roles[1]").value("ROLE_ADMIN"));
    }

    @Test
    void createUserBlankDefaultRoleConfigIsNoop() throws Exception {
        mockMvc = buildMockMvc(new IHubUserProperties(""));
        mockMvc.perform(post("/api/users").contentType(APPLICATION_JSON)
                .content("{\"username\": \"eric\", \"password\": \"Passw0rd123\"}"))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.roles.length()").value(0));
    }

    // ---- GET /api/users/{id} ----

    @Test
    void getUserReturns200() throws Exception {
        String userId = extractUserId(createAlice());
        mockMvc.perform(get("/api/users/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.userId").value(userId))
            .andExpect(jsonPath("$.email").value("alice@example.com"));
    }

    @Test
    void getUserNotFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/users/no-such-id"))
            .andExpect(status().isNotFound())
            .andExpect(jsonPath("$.detail").exists());
    }

    // ---- PUT /api/users/{id} ----

    @Test
    void updateUserEmailReturns200() throws Exception {
        String userId = extractUserId(createAlice());
        mockMvc.perform(put("/api/users/" + userId).contentType(APPLICATION_JSON)
                .content("{\"email\": \"new@example.com\"}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void updateUserInvalidEmailReturns400() throws Exception {
        String userId = extractUserId(createAlice());
        mockMvc.perform(put("/api/users/" + userId).contentType(APPLICATION_JSON)
                .content("{\"email\": \"not-an-email\"}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasKey("email")));
    }

    @Test
    void updateUserNotFoundReturns404() throws Exception {
        mockMvc.perform(put("/api/users/no-such-id").contentType(APPLICATION_JSON)
                .content("{\"email\": null}"))
            .andExpect(status().isNotFound());
    }

    // ---- DELETE /api/users/{id} ----

    @Test
    void deleteUserReturns204AndSoftDeletes() throws Exception {
        String userId = extractUserId(createAlice());
        mockMvc.perform(delete("/api/users/" + userId))
            .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/users/" + userId))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("DELETED"));
    }

    @Test
    void deleteUserNotFoundReturns404() throws Exception {
        mockMvc.perform(delete("/api/users/no-such-id"))
            .andExpect(status().isNotFound());
    }

    // ---- POST /api/users/{id}/roles ----

    @Test
    void assignRolesReturns200() throws Exception {
        String userId = extractUserId(createAlice());
        mockMvc.perform(post("/api/users/" + userId + "/roles").contentType(APPLICATION_JSON)
                .content("{\"roleIds\": [\"ROLE_ADMIN\"]}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.roles[0]").value("ROLE_ADMIN"));
    }

    @Test
    void assignRolesMissingBodyReturns400() throws Exception {
        String userId = extractUserId(createAlice());
        mockMvc.perform(post("/api/users/" + userId + "/roles").contentType(APPLICATION_JSON)
                .content("{}"))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.errors", hasKey("roleIds")));
    }

    // ---- GET /api/users ----

    @Test
    void listUsersPaged() throws Exception {
        for (int i = 1; i <= 3; i++) {
            userService.createUser("user" + i, "Passw0rd" + i + "x", null, null);
        }
        mockMvc.perform(get("/api/users").param("page", "0").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)))
            .andExpect(jsonPath("$.page").value(0))
            .andExpect(jsonPath("$.size").value(2))
            .andExpect(jsonPath("$.total").value(3));
        mockMvc.perform(get("/api/users").param("page", "1").param("size", "2"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.total").value(3));
    }

    @Test
    void listUsersFilterByKeyword() throws Exception {
        userService.createUser("alice", "Passw0rd1a", "alice@example.com", null);
        userService.createUser("bob", "Passw0rd1b", "bob@example.com", null);
        mockMvc.perform(get("/api/users").param("keyword", "alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username").value("alice"));
    }

    @Test
    void listUsersFilterByRoleId() throws Exception {
        userService.createUser("alice", "Passw0rd1a", null, java.util.List.of("ROLE_ADMIN"));
        userService.createUser("bob", "Passw0rd1b", null, null);
        mockMvc.perform(get("/api/users").param("roleId", "ROLE_ADMIN"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username").value("alice"));
    }

    @Test
    void listUsersBlankRoleIdReturnsAll() throws Exception {
        userService.createUser("alice", "Passw0rd1a", null, null);
        userService.createUser("bob", "Passw0rd1b", null, null);
        mockMvc.perform(get("/api/users").param("roleId", "  "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(2)));
    }

    @Test
    void listUsersBlankKeywordReturnsAll() throws Exception {
        userService.createUser("alice", "Passw0rd1a", null, null);
        mockMvc.perform(get("/api/users").param("keyword", "  "))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)));
    }

    @Test
    void listUsersKeywordMatchesUsernameOfUserWithoutEmail() throws Exception {
        userService.createUser("alice", "Passw0rd1a", null, null);
        mockMvc.perform(get("/api/users").param("keyword", "alice"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(1)))
            .andExpect(jsonPath("$.content[0].username").value("alice"));
    }

    @Test
    void listUsersKeywordMatchesNothing() throws Exception {
        userService.createUser("alice", "Passw0rd1a", "alice@example.com", null);
        mockMvc.perform(get("/api/users").param("keyword", "nobody"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void listUsersPageSizeCappedAt100() throws Exception {
        mockMvc.perform(get("/api/users").param("size", "500"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.size").value(100));
    }

    @Test
    void listUsersEmpty() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content", hasSize(0)))
            .andExpect(jsonPath("$.total").value(0));
    }

    private static String extractUserId(String responseBody) throws Exception {
        return new com.fasterxml.jackson.databind.ObjectMapper()
            .readTree(responseBody).get("userId").asText();
    }
}
