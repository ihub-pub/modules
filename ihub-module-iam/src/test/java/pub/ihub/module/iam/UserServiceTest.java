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

    private static final String VALID_PASSWORD = "Passw0rd123";

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
    void createUserShortUsernameThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("ab", null, null));
    }

    @Test
    void createUserUsernameWithSpaceThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("has space", null, null));
    }

    @Test
    void createUserDuplicateUsernameThrows() {
        service.createUser("charlie", null, null);
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("charlie", null, null));
    }

    // ---- createUser（带密码）----

    @Test
    void createUserWithPasswordDoesNotExposePlaintext() {
        User user = service.createUser("dave", VALID_PASSWORD, null, null);
        assertNotNull(user.userId());
        // User 域对象不含密码字段（编译期由 record 结构保证）
        assertEquals(6, User.class.getRecordComponents().length);
    }

    @Test
    void createUserShortPasswordThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("dave", "Ab1", null, null));
    }

    @Test
    void createUserPasswordWithoutUpperThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("dave", "password123", null, null));
    }

    @Test
    void createUserPasswordWithoutDigitThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("dave", "PasswordAbc", null, null));
    }

    @Test
    void createUserDuplicateUsernameWithPasswordThrows() {
        service.createUser("dave", VALID_PASSWORD, null, null);
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("dave", VALID_PASSWORD, null, null));
    }

    @Test
    void createUserWithNullPasswordSkipsValidation() {
        User user = service.createUser("oscar", null, null, null);
        assertEquals("oscar", user.username());
        assertTrue(service.authenticate("oscar", VALID_PASSWORD).isEmpty());
    }

    @Test
    void createUserOverlongPasswordThrows() {
        assertThrows(IllegalArgumentException.class,
            () -> service.createUser("pete", "A1" + "b".repeat(72), null, null));
    }

    // ---- authenticate ----

    @Test
    void authenticateSuccess() {
        User created = service.createUser("eve", VALID_PASSWORD, null, null);
        Optional<User> authenticated = service.authenticate("eve", VALID_PASSWORD);
        assertTrue(authenticated.isPresent());
        assertEquals(created.userId(), authenticated.get().userId());
    }

    @Test
    void authenticateWrongPasswordReturnsEmpty() {
        service.createUser("eve", VALID_PASSWORD, null, null);
        assertTrue(service.authenticate("eve", "WrongPass999").isEmpty());
    }

    @Test
    void authenticateUnknownUserReturnsEmpty() {
        assertTrue(service.authenticate("ghost", VALID_PASSWORD).isEmpty());
    }

    @Test
    void authenticateUserWithoutPasswordReturnsEmpty() {
        // 未设置密码的用户不能认证
        service.createUser("frank", null, null);
        assertTrue(service.authenticate("frank", VALID_PASSWORD).isEmpty());
    }

    @Test
    void authenticateNullArgumentsReturnEmpty() {
        service.createUser("grace", VALID_PASSWORD, null, null);
        assertTrue(service.authenticate(null, VALID_PASSWORD).isEmpty());
        assertTrue(service.authenticate("grace", null).isEmpty());
    }

    // ---- findById ----

    @Test
    void findByIdExists() {
        User created = service.createUser("henry", null, null);
        Optional<User> found = service.findById(created.userId());
        assertTrue(found.isPresent());
        assertEquals("henry", found.get().username());
    }

    @Test
    void findByIdNotFound() {
        assertTrue(service.findById("no-such-id").isEmpty());
    }

    // ---- findByUsername ----

    @Test
    void findByUsernameExists() {
        service.createUser("ivan", "ivan@example.com", null);
        Optional<User> found = service.findByUsername("ivan");
        assertTrue(found.isPresent());
        assertEquals("ivan@example.com", found.get().email());
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
        service.createUser("user1", null, null);
        service.createUser("user2", null, null);
        assertEquals(2, service.listAll().size());
    }

    // ---- updateProfile ----

    @Test
    void updateProfileChangesEmail() {
        User created = service.createUser("jack", null, null);
        User updated = service.updateProfile(created.userId(), "jack@example.com");
        assertEquals("jack@example.com", updated.email());
        assertEquals("jack@example.com", service.findById(created.userId()).orElseThrow().email());
    }

    @Test
    void updateProfileNotFoundThrows() {
        assertThrows(UserNotFoundException.class,
            () -> service.updateProfile("no-such-id", null));
    }

    // ---- assignRoles ----

    @Test
    void assignRolesAddsNewRoles() {
        User created = service.createUser("kate", null, List.of("ROLE_USER"));
        User updated = service.assignRoles(created.userId(), List.of("ROLE_ADMIN"));
        assertEquals(2, updated.roles().size());
        assertTrue(updated.roles().contains("ROLE_ADMIN"));
    }

    @Test
    void assignRolesSkipsDuplicates() {
        User created = service.createUser("leo", null, List.of("ROLE_USER"));
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
        User created = service.createUser("mona", null, null);
        User updated = service.changeStatus(created.userId(), User.UserStatus.DISABLED);
        assertEquals(User.UserStatus.DISABLED, updated.status());
    }

    @Test
    void changeStatusDeleted() {
        User created = service.createUser("nina", null, null);
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
