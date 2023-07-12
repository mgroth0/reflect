package matt.reflect

import matt.lang.nametoclass.classForName
import matt.test.onlyIfBasic
import matt.test.reportAndReThrowErrors
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.test.Test
import kotlin.test.assertTrue


actual class ReflectTests {


    actual fun testClassForName() = reportAndReThrowErrors {
        onlyIfBasic()
        assertEquals(String::class, classForName("kotlin.String"))
        assertEquals(String::class, classForName("kotlin.String?")) /*for kotlinx.serialization*/
        assertEquals(Int::class, classForName("kotlin.Int"))
        assertEquals(Runtime::class, classForName("java.lang.Runtime"))
    }


}

class JvmReflectTests {
    @Test
    fun noArgsConstructorChecks() {
        assertTrue(ClassWithNoArgsConstructor::class.hasNoArgsConstructor)
        assertFalse(ClassWithoutNoArgsConstructor::class.hasNoArgsConstructor)
    }

    @Test
    fun recurseSealedClasses() {
        assertEquals(
            ASealedInterface::class.recurseSealedClasses().toList().size,
            4
        )
    }


    @Test
    fun access() {
        val obj = ClassWithAPrivateMethod()
        val secretNumberFunction =
            ClassWithAPrivateMethod::class.declaredMemberFunctions.first { it.name == "secretNumber" }
        val secretVariable =
            ClassWithAPrivateMethod::class.declaredMemberProperties.first { it.name == "secretVariable" }
        secretNumberFunction.call()
        secretVariable.get(obj)
        secretNumberFunction.access {
            assertEquals(call(), 5)
        }
        secretVariable.access {
            assertEquals(getter.call(obj), "xyz")
        }
    }

}

class ClassWithAPrivateMethod() {
    private fun secretNumber() = 5
    val secretVariable = "xyz"
}


sealed interface ASealedInterface
sealed interface SubA : ASealedInterface
sealed interface SubB : ASealedInterface
object Impl : SubA


class ClassWithNoArgsConstructor
class ClassWithoutNoArgsConstructor(val data: Int)