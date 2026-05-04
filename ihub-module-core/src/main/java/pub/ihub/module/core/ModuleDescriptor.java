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

import java.util.List;
import java.util.Map;

/**
 * 业务模块描述符。
 *
 * <p>每个 IHub 业务模块在 classpath 根目录下提供 {@code module-descriptor.json}，
 * 描述其能力、依赖和 MCP 工具接口。AI 工具通过 MCP Server 读取并编排这些模块。
 *
 * @param id            模块唯一标识，如 {@code "iam-user"}
 * @param name          模块名称
 * @param nameEn        英文名称
 * @param version       模块版本
 * @param domain        所属业务领域，如 {@code "iam"}
 * @param description   一句话描述
 * @param aiContext     面向 LLM 的详细上下文（集成方式、API 清单、配置要点）
 * @param capabilities  模块提供的能力列表
 * @param dependencies  模块间依赖声明
 * @param maven         Maven 坐标
 * @param gradleRef     libs.versions.toml 别名
 * @param configPrefix  Spring 配置前缀，如 {@code "ihub.module.user"}
 * @param starterClass  自动配置类全名
 * @param tags          标签列表
 * @param status        模块状态
 * @param mcpTools      AI 可调用的 MCP 工具列表
 * @author IHub
 * @since 0.1.0
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ModuleDescriptor(
        String id,
        String name,
        @JsonProperty("name_en") String nameEn,
        String version,
        String domain,
        String description,
        @JsonProperty("ai_context") String aiContext,
        List<ModuleCapability> capabilities,
        List<ModuleDependency> dependencies,
        Map<String, String> maven,
        @JsonProperty("gradle_ref") String gradleRef,
        @JsonProperty("config_prefix") String configPrefix,
        @JsonProperty("starter_class") String starterClass,
        List<String> tags,
        ModuleStatus status,
        @JsonProperty("mcp_tools") List<McpToolDefinition> mcpTools
) {

    /**
     * 模块状态。
     */
    public enum ModuleStatus {
        /** 规划中，尚未实现 */
        planned,
        /** 实验性，API 可能变化 */
        experimental,
        /** 生产就绪 */
        stable,
        /** 已弃用，避免新项目使用 */
        deprecated
    }
}
