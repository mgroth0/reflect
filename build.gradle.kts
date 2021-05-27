//import matt.gr/oovyland.autoReflectionsJar

dependencies {
  //  implementation(autoReflectionsJar.get())
  if (isMac) {
	implementation(autoReflectionsJar)
  } else {
	implementation(libs.reflections)
  }

  api(libs.kt.reflect)
  api(projects.klib)
}