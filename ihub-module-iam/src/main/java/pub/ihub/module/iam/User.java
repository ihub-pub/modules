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
import java.util.List;

/**
 * 用户账号领域对象。
 *
 * @param userId    用户唯一 ID
 * @param username  用户名（全局唯一）
 * @param email     邮箱地址（可选）
 * @param status    账号状态
 * @param roles     已分配角色 ID 列表
 * @param createdAt 创建时间
 * @author IHub
 * @since 0.1.0
 */
public record User(
        String userId,
        String username,
        String email,
        UserStatus status,
        List<String> roles,
        Instant createdAt
) {

    /**
     * 用户账号状态。
     */
    public enum UserStatus {
        /** 待激活（注册后未验证邮箱） */
        PENDING,
        /** 活跃（正常使用） */
        ACTIVE,
        /** 已禁用 */
        DISABLED,
        /** 已注销 */
        DELETED
    }
}
