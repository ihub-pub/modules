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
import org.springframework.boot.test.context.runner.WebApplicationContextRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

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

    private final WebApplicationContextRunner webRunner = new WebApplicationContextRunner()
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
                // @ConditionalOnMissingBean 意味着存在自定义 UserService 时不创建自动配置的 Bean
                assertEquals(1, ctx.getBeansOfType(UserService.class).size());
            });
    }

    @Test
    void registersBcryptPasswordEncoderByDefault() {
        runner.run(ctx -> {
            PasswordEncoder encoder = ctx.getBean(PasswordEncoder.class);
            String hash = encoder.encode("Passw0rd123");
            assertTrue(encoder.matches("Passw0rd123", hash));
            assertFalse(encoder.matches("wrong", hash));
        });
    }

    @Test
    void doesNotOverrideCustomPasswordEncoder() {
        runner.withBean(PasswordEncoder.class, org.springframework.security.crypto.factory.PasswordEncoderFactories::createDelegatingPasswordEncoder)
            .run(ctx -> assertEquals(1, ctx.getBeansOfType(PasswordEncoder.class).size()));
    }

    @Test
    void registersUserRestControllerInWebAppOnly() {
        // 非 Web 环境：不注册 REST 控制器
        runner.run(ctx -> assertFalse(ctx.containsBean("userController")));
        // Servlet Web 环境：注册 REST 控制器
        webRunner.run(ctx -> {
            assertTrue(ctx.containsBean("userController"));
            assertNotNull(ctx.getBean(UserController.class));
        });
    }

    @Test
    void bindsUserProperties() {
        webRunner.withPropertyValues("ihub.module.user.default-role=ROLE_USER")
            .run(ctx -> assertEquals("ROLE_USER", ctx.getBean(IHubUserProperties.class).defaultRole()));
    }
}
