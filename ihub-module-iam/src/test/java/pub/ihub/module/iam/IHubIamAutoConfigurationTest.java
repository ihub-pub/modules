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

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link IHubIamAutoConfiguration} 集成测试。
 *
 * @author IHub
 * @since 0.1.0
 */
class IHubIamAutoConfigurationTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
        .withConfiguration(AutoConfigurations.of(IHubIamAutoConfiguration.class));

    @Test
    void registersUserServiceByDefault() {
        runner.run(ctx -> {
            assertTrue(ctx.containsBean("userService"));
            assertNotNull(ctx.getBean(UserService.class));
        });
    }

    @Test
    void doesNotOverrideCustomUserService() {
        runner.withBean("customUserService", UserService.class, UserService::new)
            .run(ctx -> {
                // Should have both (the auto-configured one is suppressed by @ConditionalOnMissingBean)
                // Actually @ConditionalOnMissingBean means auto-config bean is NOT created
                // when any UserService bean exists
                assertEquals(1, ctx.getBeansOfType(UserService.class).size());
            });
    }
}
