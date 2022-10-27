package matt.reflect

expect class SomeTests {



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
