package com.github.maklumi.catur

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform