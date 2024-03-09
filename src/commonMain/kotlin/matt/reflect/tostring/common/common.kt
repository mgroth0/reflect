package matt.reflect.tostring.common

import matt.lang.tostring.BaseStringableClass
import matt.reflect.firstSimpleName

abstract class ReflectingStringableClass : BaseStringableClass() {
    final override fun className() = this::class.firstSimpleName()
}
