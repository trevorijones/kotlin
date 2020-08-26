plugins {
    kotlin("jvm")
    id("jps-compatible")
}

dependencies {
    api(project(":core:compiler.common"))
    compileOnly(intellijDep()) { includeJars("trove4j") }
}

sourceSets {
    "main" { projectDefault() }
    "test" {}
}
