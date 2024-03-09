
package matt.reflect.cls

import matt.reflect.cls.common.AbstractInnerClass
import matt.reflect.cls.common.AbstractOuterClass
import matt.reflect.cls.common.DataClass
import matt.reflect.cls.common.FinalInnerClass
import matt.reflect.cls.common.FinalOuterClass
import matt.reflect.cls.common.FunInterface
import matt.reflect.cls.common.ObjectType
import matt.reflect.cls.common.OpenInnerClass
import matt.reflect.cls.common.OpenOuterClass
import matt.reflect.cls.common.RegularInterface
import matt.reflect.cls.common.SealedInterface
import matt.reflect.cls.common.SealedOuterClass
import matt.reflect.cls.common.ValueClass
import kotlin.reflect.KClass


/*13*/
fun KClass<*>.typeType() =
    when {
        objectInstance != null -> ObjectType
        isFun                  -> FunInterface
        java.isInterface       ->
            when {
                isSealed -> SealedInterface
                else     -> RegularInterface
            }

        isData                 -> DataClass
        isValue                -> ValueClass
        isInner                ->
            when {
                isAbstract -> AbstractInnerClass
                isOpen     -> OpenInnerClass
                else       -> FinalInnerClass
            }

        isSealed               -> SealedOuterClass
        isAbstract             -> AbstractOuterClass
        isOpen                 -> OpenOuterClass
        else                   -> FinalOuterClass
    }

