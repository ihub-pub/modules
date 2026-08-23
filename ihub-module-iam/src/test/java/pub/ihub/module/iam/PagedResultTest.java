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

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * {@link PagedResult} 边界测试。
 *
 * @author IHub
 * @since 0.1.0
 */
class PagedResultTest {

    @Test
    void nullContentBecomesEmptyList() {
        PagedResult<String> result = new PagedResult<>(null, 0, 10, 0);
        assertTrue(result.content().isEmpty());
    }

    @Test
    void ofFactoryBuildsCopy() {
        PagedResult<String> result = PagedResult.of(List.of("a", "b"), 1, 20, 100);
        assertEquals(2, result.content().size());
        assertEquals(1, result.page());
        assertEquals(20, result.size());
        assertEquals(100, result.total());
    }
}
