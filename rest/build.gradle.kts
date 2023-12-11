description = "应用模块"

apply {
    plugin("pub.ihub.plugin.ihub-boot")
}

dependencies {
    implementation(project(":client"))
    implementation(project(":service"))
    implementation("org.springframework.boot:spring-boot-starter-web")
}

iHubVerification {
    jacocoInstructionCoveredRatio.set("0.5")
    // 忽略启动类所在包路径测试覆盖率检查
    jacocoPackageExclusion.set("pub.ihub.demo.rest")
}
