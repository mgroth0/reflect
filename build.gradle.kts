//import matt.gr/oovyland.autoReflectionsJar

dependencies {
  //  implementation(autoReflectionsJar.get())
  implementation(autoReflectionsJar)
  api(libs.kt.reflect)
  api(projects.klib)
}