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
 * <p>生产环境应替换为持久化实现（如 MyBatis Plus）。
 *
 * @author IHub
 * @since 0.1.0
 */
public class UserService {

    private final Map<String, User> store = new ConcurrentHashMap<>();
    private final Map<String, String> usernameIndex = new ConcurrentHashMap<>();

    /**
     * 创建用户。
     *
     * @param username 用户名（不能重复）
     * @param email    邮箱（可为 null）
     * @param roles    初始角色列表
     * @return 创建成功的用户
     * @throws IllegalArgumentException 用户名已存在
     */
    public User createUser(String username, String email, List<String> roles) {
        if (username == null || username.isBlank()) {
            throw new IllegalArgumentException("用户名不能为空");
        }
        if (usernameIndex.containsKey(username)) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        String userId = UUID.randomUUID().toString();
        User user = new User(
            userId, username, email,
            User.UserStatus.ACTIVE,
            roles == null ? List.of() : List.copyOf(roles),
            Instant.now()
        );
        store.put(userId, user);
        usernameIndex.put(username, userId);
        return user;
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
     * @throws IllegalArgumentException 用户不存在
     */
    public User assignRoles(String userId, List<String> roleIds) {
        User user = store.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }
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
     */
    public User changeStatus(String userId, User.UserStatus newStatus) {
        User user = store.get(userId);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在: " + userId);
        }
        User updated = new User(user.userId(), user.username(), user.email(),
            newStatus, user.roles(), user.createdAt());
        store.put(userId, updated);
        return updated;
    }
}
