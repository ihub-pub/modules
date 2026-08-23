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

import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 用户服务（内存实现，供开发/测试环境使用）。
 *
 * <p>密码以 BCrypt 哈希存储，明文不落内存域对象。
 *
 * <p>生产环境应替换为持久化实现（如 MyBatis Plus）。
 *
 * @author IHub
 * @since 0.1.0
 */
public class UserService {

    private final PasswordEncoder passwordEncoder;
    private final Map<String, User> store = new ConcurrentHashMap<>();
    private final Map<String, String> usernameIndex = new ConcurrentHashMap<>();
    private final Map<String, String> passwordHashes = new ConcurrentHashMap<>();

    /**
     * 使用默认 BCrypt 编码器。
     */
    public UserService() {
        this(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder());
    }

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 创建用户（带初始密码）。
     *
     * @param username 用户名（不能重复，3-32 位字母/数字/下划线/中划线）
     * @param password 初始密码（至少 8 位，须包含大写、小写和数字）
     * @param email    邮箱（可为 null）
     * @param roles    初始角色列表
     * @return 创建成功的用户
     * @throws IllegalArgumentException 参数不合法或用户名已存在
     */
    public User createUser(String username, String password, String email, List<String> roles) {
        validateUsername(username);
        if (usernameIndex.containsKey(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        if (password != null) {
            validatePassword(password);
        }
        return doCreate(username, password, email, roles);
    }

    /**
     * 创建用户（无密码，无法通过 {@link #authenticate} 认证）。
     */
    public User createUser(String username, String email, List<String> roles) {
        validateUsername(username);
        if (usernameIndex.containsKey(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        return doCreate(username, null, email, roles);
    }

    private User doCreate(String username, String password, String email, List<String> roles) {
        String userId = UUID.randomUUID().toString();
        User user = new User(
            userId, username, email,
            User.UserStatus.ACTIVE,
            roles == null ? List.of() : List.copyOf(roles),
            Instant.now()
        );
        store.put(userId, user);
        usernameIndex.put(username, userId);
        if (password != null) {
            passwordHashes.put(userId, passwordEncoder.encode(password));
        }
        return user;
    }

    /**
     * 校验用户名/密码组合。
     *
     * @param username    用户名
     * @param rawPassword 明文密码
     * @return 匹配则返回用户，否则为空（含用户不存在、创建时未设置密码两种情形）
     */
    public Optional<User> authenticate(String username, String rawPassword) {
        if (username == null || rawPassword == null) {
            return Optional.empty();
        }
        String userId = usernameIndex.get(username);
        if (userId == null) {
            return Optional.empty();
        }
        String hash = passwordHashes.get(userId);
        return hash != null && passwordEncoder.matches(rawPassword, hash)
            ? Optional.ofNullable(store.get(userId))
            : Optional.empty();
    }

    /**
     * 更新用户档案（邮箱）。
     *
     * @param userId 用户 ID
     * @param email  新邮箱（null 表示清空）
     * @return 更新后的用户
     * @throws UserNotFoundException 用户不存在
     */
    public User updateProfile(String userId, String email) {
        User user = requireUser(userId);
        User updated = new User(user.userId(), user.username(), email,
            user.status(), user.roles(), user.createdAt());
        store.put(userId, updated);
        return updated;
    }

    /**
     * 按 ID 查找用户。
     */
    public Optional<User> findById(String userId) {
        return Optional.ofNullable(store.get(userId));
    }

    /**
     * 按用户名查找用户。
     */
    public Optional<User> findByUsername(String username) {
        String id = usernameIndex.get(username);
        return id != null ? Optional.ofNullable(store.get(id)) : Optional.empty();
    }

    /**
     * 列出所有用户。
     */
    public List<User> listAll() {
        return List.copyOf(store.values());
    }

    /**
     * 为用户分配角色。
     *
     * @param userId  用户 ID
     * @param roleIds 要追加的角色 ID 列表
     * @return 更新后的用户
     * @throws UserNotFoundException 用户不存在
     */
    public User assignRoles(String userId, List<String> roleIds) {
        User user = requireUser(userId);
        List<String> merged = new ArrayList<>(user.roles());
        roleIds.forEach(r -> {
            if (!merged.contains(r)) {
                merged.add(r);
            }
        });
        User updated = new User(user.userId(), user.username(), user.email(),
            user.status(), List.copyOf(merged), user.createdAt());
        store.put(userId, updated);
        return updated;
    }

    /**
     * 变更用户状态。
     *
     * @throws UserNotFoundException 用户不存在
     */
    public User changeStatus(String userId, User.UserStatus newStatus) {
        User user = requireUser(userId);
        User updated = new User(user.userId(), user.username(), user.email(),
            newStatus, user.roles(), user.createdAt());
        store.put(userId, updated);
        return updated;
    }

    private User requireUser(String userId) {
        User user = store.get(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
        return user;
    }

    private static void validateUsername(String username) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (!username.matches("[\\w-]{3,32}")) {
            throw new IllegalArgumentException("用户名须为 3-32 位字母/数字/下划线/中划线");
        }
    }

    /**
     * 密码策略：至少 8 位，包含大写字母、小写字母和数字。
     */
    private static void validatePassword(String password) {
        if (password.length() < 8 || password.length() > 72) {
            throw new IllegalArgumentException("密码长度须为 8-72 位");
        }
        if (!password.matches("(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*")) {
            throw new IllegalArgumentException("密码须同时包含大写字母、小写字母和数字");
        }
    }
}
