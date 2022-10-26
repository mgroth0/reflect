package matt.reflect

import kotlin.contracts.ExperimentalContracts

class SomeTests {

  @Test
  @ExperimentalContracts
  fun testClassForName() = reportAndReThrowErrors {
	yesIUseTestLibs()
	assertEquals(String::class, classForName("kotlin.String"))
	assertEquals(Int::class, classForName("kotlin.Int"))
	assertEquals(Runtime::class, classForName("java.lang.Runtime"))
  }


  /*https://stackoverflow.com/questions/30445974/kotlin-java-lang-nosuchmethoderror-in-tests*/
  fun reportAndReThrowErrors(op: ()->Unit) {
	try {
	  op()
	} catch (throwable: Throwable) {
	  var e: Throwable? = throwable
	  do {
		e!!
		println(e)
		e.printStackTrace()
		e = e.cause
	  } while (e != null)
	  throw throwable
	}

  }

}
