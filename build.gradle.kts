//import matt.gr/oovyland.autoReflectionsJar
import matt.kbuild.implementations


dependencies {
  //  implementation(autoReflectionsJar.get())
  /* if (isMac) {
	 implementation(autoReflectionsJar)
   } else {
	 implementation(libs.reflections)
   }*/
  implementations(
	libs.reflections8,
	projects.kj.kjlib.lang
  )


  api(libs.kt.reflect)
//  api(projects.kj.kbuild)
  /*api(projects.klib)*/
  api(
	project(
	  mapOf(
		"path" to ":k:klib",
		"configuration" to "jvmRuntimeElements"
	  )
	)
  )
}