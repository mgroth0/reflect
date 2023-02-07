@file:JvmName("ClsJvmKt")

package matt.reflect.cls

import kotlin.reflect.KClass


/*13*/
fun KClass<*>.typeType() = when {
  objectInstance != null -> ObjectType
  isFun                  -> FunInterface
  java.isInterface       -> when {
	isSealed -> SealedInterface
	else     -> RegularInterface
  }

  isData                 -> DataClass
  isValue                -> ValueClass
  isInner                -> when {
	isAbstract -> AbstractInnerClass
	isOpen     -> OpenInnerClass
	else       -> FinalInnerClass
  }

  isSealed               -> SealedOuterClass
  isAbstract             -> AbstractOuterClass
  isOpen                 -> OpenOuterClass
  else                   -> FinalOuterClass
}

//fun KClass<*>.requireIs(typeType: TypeType) {
//  require(modifiers().all { it in mods }) {
//	val fail = modifiers().first { it !in mods }
//	"${this@requireIsOnly} is $fail, which is not in [${mods.joinToString()}]"
//  }
//}
//
//fun KClass<*>.requireIsNone(vararg mods: ClassType) {
//  require(modifiers().none { it in mods })
//}
//
//fun KClass<*>.requireIsAll(vararg mods: ClassType) {
//  require(mods.all { it in modifiers() })
//}
//
//fun KClass<*>.requireIsAny(vararg mods: ClassType) {
//  require(mods.any { it in modifiers() })
//}
