package matt.reflect.reflections

import matt.reflect.scan.systemScope
import kotlin.test.Test


class ReflectionsTests() {
    @Test
    fun reflections() {
        systemScope().usingReflections()
    }
}