package matt.reflect.test.jcommon

import matt.reflect.j.access
import matt.reflect.j.hasNoArgsConstructor
import matt.reflect.j.recurseSealedClasses
import matt.test.assertions.JupiterTestAssertions
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.assertThrows
import kotlin.reflect.full.IllegalCallableAccessException
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.full.declaredMemberProperties
import kotlin.test.Test
import kotlin.test.assertTrue


class JCommonReflectTests {
    @Test
    fun noArgsConstructorChecks() =
        JupiterTestAssertions.assertRunsInOneMinute {
            assertTrue(ClassWithNoArgsConstructor::class.hasNoArgsConstructor)
            Assertions.assertFalse(ClassWithoutNoArgsConstructor::class.hasNoArgsConstructor)
        }


    @Test
    fun recurseSealedClasses() =
        JupiterTestAssertions.assertRunsInOneMinute {
            Assertions.assertEquals(
                ASealedInterface::class.recurseSealedClasses().toList().size,
                4
            )
        }




    @Test
    fun access() =
        JupiterTestAssertions.assertRunsInOneMinute {

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
                Assertions.assertEquals(call(obj), 5)
            }
            secretVariable.access {
                Assertions.assertEquals(getter.call(obj), "xyz")
            }
        }
}


class ClassWithNoArgsConstructor
class ClassWithoutNoArgsConstructor(val data: Int)



sealed interface ASealedInterface
sealed interface SubA : ASealedInterface
sealed interface SubB : ASealedInterface
data object Impl : SubA



class ClassWithAPrivateMethod() {
    private fun secretNumber() = 5
    private val secretVariable = "xyz"
}

