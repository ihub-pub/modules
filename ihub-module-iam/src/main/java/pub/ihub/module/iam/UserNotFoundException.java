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

/**
 * 用户不存在异常。
 *
 * <p>继承 {@link IllegalArgumentException} 以兼容既有调用方的异常契约，
 * REST 层捕获后映射为 404。
 *
 * @author IHub
 * @since 0.1.0
 */
public class UserNotFoundException extends IllegalArgumentException {

    public UserNotFoundException(String userId) {
        super("用户不存在: " + userId);
    }
}
