package org.project.vrescie.compose.multiplatform

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform