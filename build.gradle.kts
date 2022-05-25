//import matt.gr/oovyland.autoReflectionsJar

dependencies {
  //  implementation(autoReflectionsJar.get())
 /* if (isMac) {
	implementation(autoReflectionsJar)
  } else {
	implementation(libs.reflections)
  }*/
  implementation(libs.reflections8)

  api(libs.kt.reflect)
  api(projects.kj.kbuild)
  /*api(projects.klib)*/
  api(project(mapOf(
    "path" to ":k:klib",
    "configuration" to "jvmRuntimeElements")))
}