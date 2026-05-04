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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link UserService} 单元测试。
 *
 * @author IHub
 * @since 0.1.0
 */
class UserServiceTest {

    private UserService service;

    @BeforeEach
    void setUp() {
        service = new UserService();
    }

    // ---- createUser ----

    @Test
    void createUserSuccess() {
        User user = service.createUser("alice", "alice@example.com", List.of("ROLE_USER"));
        assertNotNull(user.userId());
        assertEquals("alice", user.username());
        assertEquals("alice@example.com", user.email());
        assertEquals(User.UserStatus.ACTIVE, user.status());
        assertEquals(List.of("ROLE_USER"), user.roles());
        assertNotNull(user.createdAt());
    }

    @Test
    void createUserWithNullRoles() {
        User user = service.createUser("bob", null, null);
        assertTrue(user.roles().isEmpty());
    }

    @Test
    void createUserBlankUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("  ", null, null));
    }

    @Test
    void createUserNullUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser(null, null, null));
    }

    @Test
    void createUserDuplicateUsernameThrows() {
        service.createUser("charlie", null, null);
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("charlie", null, null));
    }

    // ---- findById ----

    @Test
    void findByIdExists() {
        User created = service.createUser("dave", null, null);
        Optional<User> found = service.findById(created.userId());
        assertTrue(found.isPresent());
        assertEquals("dave", found.get().username());
    }

    @Test
    void findByIdNotFound() {
        assertTrue(service.findById("no-such-id").isEmpty());
    }

    // ---- findByUsername ----

    @Test
    void findByUsernameExists() {
        service.createUser("eve", "eve@example.com", null);
        Optional<User> found = service.findByUsername("eve");
        assertTrue(found.isPresent());
        assertEquals("eve@example.com", found.get().email());
    }

    @Test
    void findByUsernameNotFound() {
        assertTrue(service.findByUsername("ghost").isEmpty());
    }

    // ---- listAll ----

    @Test
    void listAllEmpty() {
        assertTrue(service.listAll().isEmpty());
    }

    @Test
    void listAllReturnsAll() {
        service.createUser("u1", null, null);
        service.createUser("u2", null, null);
        assertEquals(2, service.listAll().size());
    }

    // ---- assignRoles ----

    @Test
    void assignRolesAddsNewRoles() {
        User created = service.createUser("frank", null, List.of("ROLE_USER"));
        User updated = service.assignRoles(created.userId(), List.of("ROLE_ADMIN"));
        assertEquals(2, updated.roles().size());
        assertTrue(updated.roles().contains("ROLE_ADMIN"));
    }

    @Test
    void assignRolesSkipsDuplicates() {
        User created = service.createUser("grace", null, List.of("ROLE_USER"));
        User updated = service.assignRoles(created.userId(), List.of("ROLE_USER", "ROLE_ADMIN"));
        assertEquals(2, updated.roles().size());
    }

    @Test
    void assignRolesUserNotFoundThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.assignRoles("no-such-id", List.of("ROLE_USER")));
    }

    // ---- changeStatus ----

    @Test
    void changeStatusDisabled() {
        User created = service.createUser("henry", null, null);
        User updated = service.changeStatus(created.userId(), User.UserStatus.DISABLED);
        assertEquals(User.UserStatus.DISABLED, updated.status());
    }

    @Test
    void changeStatusDeleted() {
        User created = service.createUser("ivan", null, null);
        User updated = service.changeStatus(created.userId(), User.UserStatus.DELETED);
        assertEquals(User.UserStatus.DELETED, updated.status());
    }

    @Test
    void changeStatusUserNotFoundThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.changeStatus("no-such-id", User.UserStatus.DISABLED));
    }

    // ---- User record / UserStatus enum ----

    @Test
    void userStatusAllValues() {
        assertEquals(4, User.UserStatus.values().length);
    }
}
