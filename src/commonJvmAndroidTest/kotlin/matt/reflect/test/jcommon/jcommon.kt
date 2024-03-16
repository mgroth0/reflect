package matt.reflect.test.jcommon

import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.shouldBe
import matt.reflect.j.hasNoArgsConstructor
import matt.reflect.j.recurseSealedClasses
import kotlin.test.Test


class JCommonReflectTests {
    @Test
    fun noArgsConstructorChecks() {
        ClassWithNoArgsConstructor::class.hasNoArgsConstructor.shouldBeTrue()
        ClassWithoutNoArgsConstructor::class.hasNoArgsConstructor.shouldBeFalse()
    }


    @Test
    fun recurseSealedClasses() {
        ASealedInterface::class.recurseSealedClasses().toList().size shouldBe 4
    }
}


class ClassWithNoArgsConstructor
class ClassWithoutNoArgsConstructor(val data: Int)



sealed interface ASealedInterface
sealed interface SubA : ASealedInterface
sealed interface SubB : ASealedInterface
data object Impl : SubA




