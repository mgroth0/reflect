

//import matt.gr/oovyland.autoReflectionsJar
import matt.klib.str.upper

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
//	projects.k.kjlib.lang
  )



  api(libs.kt.reflect)
//  api(projects.k.kbuild)
  /*api(projects.klib)*/
}