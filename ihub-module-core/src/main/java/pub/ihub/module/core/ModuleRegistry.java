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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 业务模块注册表。
 *
 * <p>扫描 classpath 中所有 {@code module-descriptor.json} 文件，
 * 加载并缓存 {@link ModuleDescriptor}。供 MCP Server 的 {@code ModuleTools} 使用。
 *
 * <p>模块描述符文件约定放置在各模块 jar 包的根路径，命名格式：
 * {@code META-INF/ihub/module-descriptor.json}
 *
 * @author IHub
 * @since 0.1.0
 */
public class ModuleRegistry {

    static final String DESCRIPTOR_PATH = "META-INF/ihub/module-descriptor.json";

    private final Map<String, ModuleDescriptor> modules = new ConcurrentHashMap<>();
    private final ObjectMapper objectMapper;

    public ModuleRegistry(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    /**
     * 手动注册一个模块描述符（用于测试或动态注册）。
     */
    public void register(ModuleDescriptor descriptor) {
        modules.put(descriptor.id(), descriptor);
    }

    /**
     * 从 classpath 中扫描并加载所有模块描述符。
     *
     * <p>每个 IHub 模块 jar 包在 {@code META-INF/ihub/module-descriptor.json} 放置描述符。
     */
    public void scanClasspath() {
        try {
            var resources = getClassLoader().getResources(DESCRIPTOR_PATH);
            while (resources.hasMoreElements()) {
                var url = resources.nextElement();
                try (InputStream is = url.openStream()) {
                    var descriptor = objectMapper.readValue(is, ModuleDescriptor.class);
                    modules.put(descriptor.id(), descriptor);
                } catch (IOException e) {
                    // 单个描述符加载失败不影响其他模块
                    System.err.println("Failed to load module descriptor from: " + url + " - " + e.getMessage());
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to scan module descriptors", e);
        }
    }

    /**
     * 获取所有已注册模块。
     */
    public List<ModuleDescriptor> listAll() {
        return Collections.unmodifiableList(new ArrayList<>(modules.values()));
    }

    /**
     * 按 ID 获取模块。
     */
    public Optional<ModuleDescriptor> findById(String moduleId) {
        return Optional.ofNullable(modules.get(moduleId));
    }

    /**
     * 按领域过滤模块。
     */
    public List<ModuleDescriptor> findByDomain(String domain) {
        return modules.values().stream()
                .filter(m -> domain.equals(m.domain()))
                .toList();
    }

    /**
     * 返回用于扫描 classpath 的 ClassLoader，子类可覆写以便测试。
     */
    protected ClassLoader getClassLoader() {
        return getClass().getClassLoader();
    }
}
