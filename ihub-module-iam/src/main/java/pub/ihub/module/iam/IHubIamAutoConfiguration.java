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

import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * IHub IAM 模块自动配置。
 *
 * <p>当 classpath 中引入 {@code ihub-module-iam} 时：
 * <ul>
 *   <li>注册 {@link PasswordEncoder}（默认 BCrypt，可自定义 Bean 覆盖）</li>
 *   <li>注册内存实现的 {@link UserService}（可自定义 Bean 覆盖为持久化实现）</li>
 *   <li>Servlet Web 应用中注册 {@link UserController}（{@code /api/users}）</li>
 * </ul>
 *
 * @author IHub
 * @since 0.1.0
 */
@AutoConfiguration
@EnableConfigurationProperties(IHubUserProperties.class)
public class IHubIamAutoConfiguration {

    /**
     * 注册默认 BCrypt 密码编码器（仅当未提供自定义实现时）。
     */
    @Bean
    @ConditionalOnMissingBean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }

    /**
     * 注册内存版 {@link UserService}（仅当未提供自定义实现时）。
     */
    @Bean
    @ConditionalOnMissingBean
    public UserService userService(PasswordEncoder passwordEncoder) {
        return new UserService(passwordEncoder);
    }

    /**
     * Servlet Web 应用中注册用户管理 REST API（仅当未自定义时）。
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
    public UserController userController(UserService userService, IHubUserProperties properties) {
        return new UserController(userService, properties);
    }
}
