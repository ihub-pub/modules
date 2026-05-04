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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Map;

/**
 * 模块能力描述。
 *
 * <p>能力是模块对外提供的单个功能单元，可以是 REST API、领域事件、定时任务或 MCP 工具。
 *
 * @param id          能力唯一 ID，如 {@code "user.create"}
 * @param name        能力名称
 * @param type        能力类型
 * @param description AI 可读描述
 * @param inputSchema 输入参数结构（JSON Schema 片段）
 * @param outputSchema 输出结果结构（JSON Schema 片段）
 * @author IHub
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ModuleCapability(
        String id,
        String name,
        CapabilityType type,
        String description,
        @JsonProperty("input_schema") Map<String, Object> inputSchema,
        @JsonProperty("output_schema") Map<String, Object> outputSchema
) {

    /**
     * 能力类型。
     */
    public enum CapabilityType {
        /** REST API */
        api,
        /** 领域事件（Spring ApplicationEvent / 消息队列） */
        event,
        /** 定时任务 */
        schedule,
        /** MCP 工具（AI 可直接调用） */
        mcp_tool
    }
}
