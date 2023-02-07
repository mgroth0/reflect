package matt.reflect.cls

/*13*/
sealed interface TypeType

object ObjectType: TypeType

sealed interface ClassType: TypeType
sealed interface InterfaceType: TypeType

object RegularInterface: InterfaceType
object SealedInterface: SealedType, InterfaceType
object FunInterface: InterfaceType

object DataClass: ClassType
object ValueClass: ClassType

sealed interface FinalClass: ClassType
sealed interface NonSealedNonAbstractOpenClass: ClassType
sealed interface NonSealedAbstractClass: ClassType
sealed interface SealedType: TypeType

object FinalOuterClass: FinalClass
object OpenOuterClass: NonSealedNonAbstractOpenClass
object AbstractOuterClass: NonSealedAbstractClass
object SealedOuterClass: SealedType, ClassType


sealed interface InnerClass: ClassType
object FinalInnerClass: InnerClass, FinalClass
object OpenInnerClass: InnerClass, NonSealedNonAbstractOpenClass
object AbstractInnerClass: InnerClass, NonSealedAbstractClass