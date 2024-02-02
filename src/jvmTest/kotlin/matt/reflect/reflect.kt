package matt.reflect

import matt.test.assertions.JupiterTestAssertions.assertRunsInOneMinute
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.test.Test
import kotlin.test.assertTrue


class JvmReflectTests {


    @Test
    fun noArgsConstructorChecks() = assertRunsInOneMinute {
        assertTrue(ClassWithNoArgsConstructor::class.hasNoArgsConstructor)
        assertFalse(ClassWithoutNoArgsConstructor::class.hasNoArgsConstructor)
    }

    @Test
    fun recurseSealedClasses() = assertRunsInOneMinute {
        assertEquals(
            ASealedInterface::class.recurseSealedClasses().toList().size,
            4
        )
    }


    @Test
    fun access() = assertRunsInOneMinute {

        val obj = ClassWithAPrivateMethod()
        val secretNumberFunction =
            ClassWithAPrivateMethod::class.declaredMemberFunctions.first { it.name == "secretNumber" }
        val secretVariable =
            ClassWithAPrivateMethod::class.declaredMemberProperties.first { it.name == "secretVariable" }
        assertThrows<IllegalCallableAccessException> {
            secretNumberFunction.call(obj)
        }
        assertThrows<IllegalCallableAccessException> {
            secretVariable.get(obj)
        }
        secretNumberFunction.access {
            assertEquals(call(obj), 5)
        }
        secretVariable.access {
            assertEquals(getter.call(obj), "xyz")
        }
    }

}

class ClassWithAPrivateMethod() {
    private fun secretNumber() = 5
    private val secretVariable = "xyz"
}


sealed interface ASealedInterface
sealed interface SubA : ASealedInterface
sealed interface SubB : ASealedInterface
object Impl : SubA


class ClassWithNoArgsConstructor
class ClassWithoutNoArgsConstructor(val data: Int)
