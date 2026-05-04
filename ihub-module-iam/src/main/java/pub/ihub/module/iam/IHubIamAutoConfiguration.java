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
import org.springframework.context.annotation.Bean;

/**
 * IHub IAM 模块自动配置。
 *
 * <p>当 classpath 中引入 {@code ihub-module-iam} 且未自定义 {@link UserService} 时，
 * 自动注册内存实现的 {@link UserService}。
 *
 * <p>生产环境应提供持久化实现的 {@link UserService} Bean 以覆盖此默认实现。
 *
 * @author IHub
 * @since 0.1.0
 */
@AutoConfiguration
public class IHubIamAutoConfiguration {

    /**
     * 注册内存版 {@link UserService}（仅当未提供自定义实现时）。
     */
    @Bean
    @ConditionalOnMissingBean
    public UserService userService() {
        return new UserService();
    }
}
