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

/**
 * 模块间依赖声明。
 *
 * @param moduleId 依赖的模块 ID
 * @param required 是否必须（false 表示可选）
 * @param reason   依赖原因（AI 可读）
 * @author IHub
 * @since 0.1.0
 */
public record ModuleDependency(
        String moduleId,
        boolean required,
        String reason
) {}
