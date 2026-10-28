plugins {
    `java-library`
}

dependencies {
    implementation(libs.guava)
    implementation(libs.netty.handler)
    compileOnly(libs.jspecify)
}
