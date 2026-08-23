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

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 用户管理 REST API。
 *
 * <p>由 {@link IHubIamAutoConfiguration} 在 Servlet Web 应用中自动注册。
 *
 * @author IHub
 * @since 0.1.0
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private static final int MAX_PAGE_SIZE = 100;

    private final UserService userService;
    private final IHubUserProperties properties;

    public UserController(UserService userService, IHubUserProperties properties) {
        this.userService = userService;
        this.properties = properties;
    }

    /**
     * 创建用户。
     */
    @PostMapping
    public ResponseEntity<User> create(@Valid @RequestBody CreateUserRequest request) {
        User user = userService.createUser(
            request.username(), request.password(), request.email(),
            mergeDefaultRoles(request.roles()));
        return ResponseEntity
            .created(URI.create("/api/users/" + user.userId()))
            .body(user);
    }

    /**
     * 查询用户详情。
     */
    @GetMapping("/{id}")
    public User get(@PathVariable("id") String userId) {
        return userService.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    /**
     * 更新用户档案（邮箱）。
     */
    @PutMapping("/{id}")
    public User update(@PathVariable("id") String userId, @Valid @RequestBody UpdateUserRequest request) {
        return userService.updateProfile(userId, request.email());
    }

    /**
     * 注销用户（软删除，状态置为 DELETED）。
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String userId) {
        userService.changeStatus(userId, User.UserStatus.DELETED);
        return ResponseEntity.noContent().build();
    }

    /**
     * 为用户追加角色。
     */
    @PostMapping("/{id}/roles")
    public User assignRoles(@PathVariable("id") String userId, @Valid @RequestBody AssignRolesRequest request) {
        return userService.assignRoles(userId, request.roleIds());
    }

    /**
     * 分页查询用户。
     *
     * @param keyword 用户名/邮箱模糊匹配（可选）
     * @param roleId  按角色 ID 过滤（可选）
     * @param page    页码，从 0 开始
     * @param size    页大小，1-100
     */
    @GetMapping
    public PagedResult<User> list(
            @RequestParam(name = "keyword", required = false) String keyword,
            @RequestParam(name = "roleId", required = false) String roleId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {
        int effectiveSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        int effectivePage = Math.max(page, 0);
        List<User> matched = userService.listAll().stream()
            .filter(user -> matchesKeyword(user, keyword))
            .filter(user -> roleId == null || roleId.isBlank() || user.roles().contains(roleId))
            .sorted(Comparator.comparing(User::createdAt).thenComparing(User::userId))
            .toList();
        int from = Math.min(effectivePage * effectiveSize, matched.size());
        int to = Math.min(from + effectiveSize, matched.size());
        return PagedResult.of(matched.subList(from, to), effectivePage, effectiveSize, matched.size());
    }

    private boolean matchesKeyword(User user, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        return user.username().contains(keyword)
            || (user.email() != null && user.email().contains(keyword));
    }

    private List<String> mergeDefaultRoles(List<String> roles) {
        List<String> merged = roles == null ? new ArrayList<>() : new ArrayList<>(roles);
        properties.defaultRoles().forEach(role -> {
            if (!merged.contains(role)) {
                merged.add(role);
            }
        });
        return merged;
    }

    /**
     * 用户不存在 → 404。
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleNotFound(UserNotFoundException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, e.getMessage());
    }

    /**
     * 业务规则冲突（用户名重复等）→ 409。
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleConflict(IllegalArgumentException e) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, e.getMessage());
    }

    /**
     * 请求参数校验失败 → 400，附字段级错误。
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleValidation(MethodArgumentNotValidException e) {
        ProblemDetail detail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "请求参数校验失败");
        Map<String, String> errors = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors()
            .forEach(error -> errors.put(error.getField(), error.getDefaultMessage()));
        detail.setProperty("errors", errors);
        return detail;
    }

    /**
     * 创建用户请求。
     */
    public record CreateUserRequest(
            @NotBlank
            @Size(min = 3, max = 32)
            @Pattern(regexp = "[\\w-]+", message = "用户名仅允许字母/数字/下划线/中划线")
            String username,
            @NotBlank
            @Size(min = 8, max = 72)
            @Pattern(regexp = "(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*", message = "密码须同时包含大写字母、小写字母和数字")
            String password,
            @Email
            String email,
            List<String> roles
    ) {
    }

    /**
     * 更新用户档案请求。
     */
    public record UpdateUserRequest(
            @Email
            String email
    ) {
    }

    /**
     * 分配角色请求。
     */
    public record AssignRolesRequest(
            @NotNull
            List<String> roleIds
    ) {
    }
}
