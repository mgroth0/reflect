

//import matt.gr/oovyland.autoReflectionsJar
import matt.klib.str.upper
modtype = LIB

apis(
  ":k:klib".jvm()
)

dependencies {
  //  implementation(autoReflectionsJar.get())
  /* if (isMac) {
	 implementation(autoReflectionsJar)
   } else {
	 implementation(libs.reflections)
   }*/
  implementations(
	libs.reflections8,
//	projects.kj.kjlib.lang
  )



  api(libs.kt.reflect)
//  api(projects.kj.kbuild)
  /*api(projects.klib)*/
//  if (rootDir.name.upper() == "FLOW") {
//	api(project(":k:klib")) {
//	  targetConfiguration = "jvmRuntimeElements"
//	}
//  } else {
//	api("matt.k:klib:+")
//  }
}