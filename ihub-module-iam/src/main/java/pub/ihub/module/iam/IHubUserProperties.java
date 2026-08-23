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

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * 用户模块配置属性。
 *
 * @param defaultRole 新用户默认角色 ID（创建时未指定角色则追加）
 * @author IHub
 * @since 0.1.0
 */
@ConfigurationProperties(prefix = "ihub.module.user")
public record IHubUserProperties(String defaultRole) {

    /**
     * 默认角色列表（未配置时为空）。
     */
    public List<String> defaultRoles() {
        return defaultRole == null || defaultRole.isBlank() ? List.of() : List.of(defaultRole);
    }
}
