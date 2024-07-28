plugins {
    alias(ihub.plugins.root)
    alias(ihub.plugins.copyright)
    alias(ihub.plugins.git.hooks)
    alias(ihub.plugins.java) apply false
    alias(ihub.plugins.test) apply false
    alias(ihub.plugins.verification) apply false
    alias(ihub.plugins.publish) apply false
}

subprojects {
    !project.pluginManager.hasPlugin("java-platform") || return@subprojects
    apply {
        plugin("pub.ihub.plugin.ihub-java")
        plugin("pub.ihub.plugin.ihub-test")
        plugin("pub.ihub.plugin.ihub-verification")
        plugin("pub.ihub.plugin.ihub-publish")
    }

    dependencies {
        if (project.name != "ihub-core") {
            "api"(project(":ihub-core"))
        }
    }
}

iHubGitHooks {
    hooks.set(
        mapOf(
            "pre-commit" to "./gradlew build",
            "commit-msg" to "./gradlew commitCheck"
        )
    )
}
