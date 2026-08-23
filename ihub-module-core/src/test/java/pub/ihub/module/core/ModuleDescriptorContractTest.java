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

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 模块描述符反序列化契约测试。
 *
 * <p>直接以 ihub-module-iam 的真实描述符（含 {@code $schema}、
 * {@code required} 等模型外字段）验证容错读取，
 * 防止模型与描述符演进时互相破坏——消费方（agents MCP Server）依赖此契约。
 *
 * @author IHub
 * @since 0.1.0
 */
class ModuleDescriptorContractTest {

    private static final Path IAM_DESCRIPTOR = Path.of(
        "../ihub-module-iam/src/main/resources/META-INF/ihub/module-descriptor.json");

    /** 默认 ObjectMapper 即 FAIL_ON_UNKNOWN_PROPERTIES=true，容错由 @JsonIgnoreProperties 保证 */
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void parsesRealIamDescriptorWithUnknownFields() throws Exception {
        String json = Files.readString(IAM_DESCRIPTOR);

        ModuleDescriptor descriptor = objectMapper.readValue(json, ModuleDescriptor.class);

        assertEquals("iam-user", descriptor.id());
        assertEquals("用户管理模块", descriptor.name());
        assertEquals("User Management Module", descriptor.nameEn());
        assertEquals("iam", descriptor.domain());
        assertEquals(ModuleDescriptor.ModuleStatus.experimental, descriptor.status());
        assertEquals("ihub.module.user", descriptor.configPrefix());
        assertEquals(3, descriptor.mcpTools().size());
        assertEquals("createUser", descriptor.mcpTools().get(0).name());

        // capabilities：type 枚举映射 + input_schema
        assertEquals(3, descriptor.capabilities().size());
        assertEquals(ModuleCapability.CapabilityType.api, descriptor.capabilities().get(0).type());
        assertEquals(ModuleCapability.CapabilityType.event, descriptor.capabilities().get(2).type());
        assertTrue(descriptor.capabilities().get(0).inputSchema().containsKey("properties"));

        // dependencies：含模型外字段 required 之外的演进字段亦不破坏
        assertEquals(1, descriptor.dependencies().size());
        assertEquals("iam-role", descriptor.dependencies().get(0).moduleId());
        assertFalse(descriptor.dependencies().get(0).required());
    }

    @Test
    void toleratesFutureUnknownFieldsAtEveryLevel() throws Exception {
        String json = """
            {
              "id": "future-module",
              "schema_version": 2,
              "new_top_level_field": "x",
              "status": "stable",
              "capabilities": [
                {"id": "c1", "type": "api", "future_field": {"nested": true}}
              ],
              "dependencies": [
                {"moduleId": "dep-1", "required": true, "since": "9.9.9"}
              ],
              "mcp_tools": [
                {"name": "t1", "input_schema": {"type": "object"}, "tool_metadata": "y"}
              ]
            }
            """;

        ModuleDescriptor descriptor = objectMapper.readValue(json, ModuleDescriptor.class);

        assertEquals("future-module", descriptor.id());
        assertEquals(ModuleDescriptor.ModuleStatus.stable, descriptor.status());
        assertEquals(1, descriptor.capabilities().size());
        assertTrue(descriptor.dependencies().get(0).required());
        assertEquals(1, descriptor.mcpTools().size());
    }

    @Test
    void iamDescriptorFileExists() {
        assertTrue(Files.exists(IAM_DESCRIPTOR), "契约 fixture 缺失: " + IAM_DESCRIPTOR);
        Optional.of(IAM_DESCRIPTOR).orElseThrow();
    }
}
