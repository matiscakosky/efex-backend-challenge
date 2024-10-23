package com.efex.apps

import io.micronaut.runtime.Micronaut.build

fun main(args: Array<String>) {
    build(*args)
        .args(*args)
        .packages("com.efex")
        .start()
}
