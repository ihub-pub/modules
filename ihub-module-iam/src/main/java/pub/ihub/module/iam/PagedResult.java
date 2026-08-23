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

import java.util.List;

/**
 * 分页结果。
 *
 * @param content 当前页数据
 * @param page    页码（从 0 开始）
 * @param size    页大小
 * @param total   符合条件的总记录数
 * @author IHub
 * @since 0.1.0
 */
public record PagedResult<T>(
        List<T> content,
        int page,
        int size,
        long total
) {

    public PagedResult {
        content = content == null ? List.of() : List.copyOf(content);
    }

    public static <T> PagedResult<T> of(List<T> content, int page, int size, long total) {
        return new PagedResult<>(content, page, size, total);
    }
}
