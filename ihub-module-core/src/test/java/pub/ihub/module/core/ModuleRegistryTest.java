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
package pub.ihub.module.core;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ModuleRegistryTest {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ModuleRegistry registry = new ModuleRegistry(objectMapper);

    @Test
    void registerAndList() {
        ModuleDescriptor descriptor = new ModuleDescriptor(
            "iam-user", "用户管理", "User Management", "0.1.0",
            "iam", "用户注册、登录、档案管理", null,
            List.of(), List.of(), Map.of("group", "pub.ihub.module", "artifact", "ihub-module-iam"),
            "ihub-module-iam", "ihub.module.user", null,
            List.of("iam", "user"), ModuleDescriptor.ModuleStatus.planned, List.of()
        );

        registry.register(descriptor);

        assertEquals(1, registry.listAll().size());
        assertTrue(registry.findById("iam-user").isPresent());
        assertEquals(1, registry.findByDomain("iam").size());
        assertTrue(registry.findByDomain("org").isEmpty());
    }

    @Test
    void findByIdNotFound() {
        assertTrue(registry.findById("non-existent").isEmpty());
    }

    @Test
    void scanClasspathLoadsDescriptor() {
        // classpath 中有 src/test/resources/META-INF/ihub/module-descriptor.json
        registry.scanClasspath();

        assertTrue(registry.findById("test-module").isPresent());
        ModuleDescriptor m = registry.findById("test-module").get();
        assertEquals("测试模块", m.name());
        assertEquals("test", m.domain());
        assertEquals(ModuleDescriptor.ModuleStatus.experimental, m.status());
    }

    @Test
    void scanClasspathFindByDomain() {
        registry.scanClasspath();
        assertFalse(registry.findByDomain("test").isEmpty());
    }

    @Test
    void allModuleStatusValues() {
        for (ModuleDescriptor.ModuleStatus status : ModuleDescriptor.ModuleStatus.values()) {
            assertNotNull(status.name());
        }
    }

    @Test
    void allCapabilityTypes() {
        for (ModuleCapability.CapabilityType type : ModuleCapability.CapabilityType.values()) {
            assertNotNull(type.name());
        }
    }

    @Test
    void moduleDependencyFields() {
        ModuleDependency dep = new ModuleDependency("some-module", true, "需要认证能力");
        assertEquals("some-module", dep.moduleId());
        assertTrue(dep.required());
        assertFalse(new ModuleDependency("opt", false, "可选").required());
    }

    @Test
    void mcpToolDefinitionFields() {
        McpToolDefinition tool = new McpToolDefinition("createUser", "创建用户", Map.of(), Map.of());
        assertEquals("createUser", tool.name());
        assertEquals("创建用户", tool.description());
    }

    @Test
    void scanClasspathSingleDescriptorIoException() throws Exception {
        // 模拟单个描述符加载失败时不影响其他模块（通过 broken InputStream）
        ObjectMapper brokenMapper = new ObjectMapper() {
            @Override
            public <T> T readValue(java.io.InputStream src, Class<T> valueType) throws java.io.IOException {
                throw new java.io.IOException("Simulated parse error");
            }
        };
        ModuleRegistry reg = new ModuleRegistry(brokenMapper);
        // 不抛异常，仅打印错误日志，registry 仍为空
        assertDoesNotThrow(reg::scanClasspath);
        assertTrue(reg.listAll().isEmpty());
    }

    @Test
    void scanClasspathGetResourcesIoException() {
        // 模拟 ClassLoader.getResources 抛出 IOException
        ModuleRegistry reg = new ModuleRegistry(objectMapper) {
            @Override
            protected ClassLoader getClassLoader() {
                return new ClassLoader(Thread.currentThread().getContextClassLoader()) {
                    @Override
                    public java.util.Enumeration<java.net.URL> getResources(String name) throws java.io.IOException {
                        throw new java.io.IOException("Simulated ClassLoader error");
                    }
                };
            }
        };
        RuntimeException ex = assertThrows(RuntimeException.class, reg::scanClasspath);
        assertTrue(ex.getMessage().contains("Failed to scan module descriptors"));
    }
}
