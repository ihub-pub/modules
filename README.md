<p align="center">
    <a target="_blank" href="https://ihub.pub/">
        <img src="https://doc.ihub.pub/ihub.svg" height="150" alt="IHub">
        <img src="https://doc.ihub.pub/ihub_libs.svg" height="150" alt="IHub">
    </a>
</p>

---

<p align="center">
    <a target="_blank" href="https://github.com/ihub-pub/modules/actions/workflows/gradle-build.yml">
        <img src="https://badge.ihub.pub/github/actions/workflow/status/ihub-pub/modules/gradle-build.yml?branch=main&label=Build&logo=GitHub+Actions&logoColor=white" alt="Gradle Build"/>
    </a>
    <a title="Test Cases" href="https://ihub-pub.testspace.com/spaces/267303?utm_campaign=metric&utm_medium=referral&utm_source=badge">
        <img alt="Space Metric" src="https://badge.ihub.pub/testspace/tests/ihub-pub/ihub-pub:modules/main?compact_message&label=Tests&logo=GitHub+Actions&logoColor=white" />
    </a>
    <a target="_blank" href="https://www.codefactor.io/repository/github/ihub-pub/modules">
        <img src="https://badge.ihub.pub/codefactor/grade/github/ihub-pub/modules/main?color=white&label=Codefactor&labelColor=F44A6A&logo=CodeFactor&logoColor=white" alt="CodeFactor"/>
    </a>
    <a target="_blank" href="https://codecov.io/gh/ihub-pub/modules">
        <img src="https://badge.ihub.pub/codecov/c/github/ihub-pub/modules?token=ZQ0WR3ZSWG&color=white&label=Codecov&labelColor=F01F7A&logo=Codecov&logoColor=white" alt="Codecov"/>
    </a>
    <a target="_blank" href="https://github.com/ihub-pub/modules">
        <img src="https://badge.ihub.pub/github/stars/ihub-pub/modules?color=white&style=flat&logo=GitHub&labelColor=181717&label=Stars" alt="IHubPub"/>
    </a>
    <a target="_blank" href="https://gitee.com/ihub-pub/modules">
        <img src="https://badge.ihub.pub/badge/dynamic/json?url=https%3A%2F%2Fgitee.com%2Fapi%2Fv5%2Frepos%2Fihub-pub%2Fmodules&query=%24.stargazers_count&style=flat&logo=gitee&label=stars&labelColor=c71d23&color=white&cacheSeconds=5000" alt="IHubPub"/>
    </a>
    <a target="_blank" href="https://javadoc.io/doc/pub.ihub.module">
        <img alt="Java Doc" src="https://javadoc.io/badge2/pub.ihub.module/ihub-core/javadoc.svg?color=white&labelColor=8CA1AF&label=Docs&logo=readthedocs&logoColor=white">
    </a>
    <a target="_blank" href="https://mvnrepository.com/artifact/pub.ihub.module">
        <img src="https://badge.ihub.pub/maven-central/v/pub.ihub.module/ihub-bom?color=white&labelColor=C71A36&label=Maven&logo=Apache+Maven&logoColor=white" alt="Maven Central"/>
    </a>
</p>

## IHub 业务蓝图层（L2 Capability Board）

IHub 三层架构中的 **业务蓝图层**，提供即插即用的通用业务模块，每个模块携带 AI 可编排的结构化描述符。

### 🧩 模块描述符规范

每个业务模块在 `META-INF/ihub/module-descriptor.json` 提供结构化元数据，AI 工具可通过 MCP Server 发现并编排这些模块：

```json
{
  "id": "iam-user",
  "domain": "iam",
  "capabilities": [...],    // API、事件、定时任务
  "mcp_tools": [...]        // AI 可直接调用的操作
}
```

JSON Schema: [`schema/module-descriptor-v1.json`](schema/module-descriptor-v1.json)

### 📦 核心模块

| 模块 | 状态 | 描述 |
|------|------|------|
| `ihub-module-core` | 实验性 | 模块基础设施：描述符模型、注册表、Jackson 序列化 |
| `ihub-module-iam` | 规划中 | IAM 模块：用户/角色/权限管理 |

### 🤖 AI 编排能力

通过 `agents/mcp-server` 的 `ModuleTools`，AI 工具可：
- `listModules()` — 列出所有可用业务模块
- `getModule(moduleId)` — 获取模块详细描述符
- `getModuleTools(moduleId)` — 获取模块的 MCP 工具列表

> 详细设计：[P2 MCP 编排规范](https://github.com/ihub-pub/ihub/blob/main/docs/strategy/2026-05-04-ihub-modules-p2-design.md)

## 🧭 开源贡献指南

请阅读 [贡献指南](https://github.com/ihub-pub/.github/blob/main/CONTRIBUTING.md) 为该项目做出贡献

## 👨‍💻 Contributors

![Alt](https://repobeats.axiom.co/api/embed/10b52c85a6a8d23a2601bd26bd16716deddbc073.svg "Repobeats analytics image")

[![Contributors](https://contrib.rocks/image?repo=ihub-pub/modules)](https://github.com/ihub-pub/modules/graphs/contributors "Contributors")
