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
 * MCP 工具定义。
 *
 * <p>描述业务模块暴露给 AI 工具的单个 MCP 操作。
 * AI 工具（Claude / Cursor 等）通过 MCP Server 发现并调用这些工具。
 *
 * @param name         工具名称（全局唯一，供 AI 调用）
 * @param description  工具描述（AI 理解工具用途的依据）
 * @param inputSchema  输入参数结构
 * @param outputSchema 输出结果结构
 * @author IHub
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record McpToolDefinition(
        String name,
        String description,
        @JsonProperty("input_schema") Map<String, Object> inputSchema,
        @JsonProperty("output_schema") Map<String, Object> outputSchema
) {}
