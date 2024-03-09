package matt.reflect.reflections.test.reflections

import matt.reflect.reflections.usingReflections
import matt.reflect.scan.jcommon.systemScope
import kotlin.test.Test


class ReflectionsTests() {
    @Test
    fun reflections() {
        systemScope().usingReflections()
    }
}
