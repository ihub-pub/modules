description = "客户端模块"

apply {
    plugin("pub.ihub.plugin.ihub-publish")
}

dependencies {
    implementation("org.springframework.cloud:spring-cloud-starter-openfeign")
}
