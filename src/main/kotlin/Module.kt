import kotlin.reflect.KClass

class ClassFactory(
    val clazz: KClass<*>,
    val factory: () -> Any,
)

class ClassInstance(
    val clazz: KClass<*>,
    val instance: Any,
)

class UnresolvedDependency(dependencyName: String) : Throwable(dependencyName)

class Module {
    private val factories = mutableListOf<ClassFactory>()
    private val instances = mutableListOf<ClassInstance>()

    inline fun <reified T : Any> single(noinline factory: () -> T) {
        val clazz = T::class
        single(clazz, factory)
    }

    fun single(clazz: KClass<*>, factory: () -> Any) {
        val classFactory = ClassFactory(clazz, factory)
        factories.add(classFactory)
    }

    inline fun <reified T> get(): T {
        val clazz = T::class
        return get(clazz)
    }

    fun <T> get(clazz: KClass<*>): T {
        val classInstance = instances.find { it.clazz == clazz }

        if (classInstance != null) {
            return classInstance.instance as T
        }

        val factoryInstance = factories.find { it.clazz == clazz }

        if (factoryInstance != null) {
            val newInstance = factoryInstance.factory()
            instances.add(ClassInstance(clazz, newInstance))
            return newInstance as T
        }

        throw UnresolvedDependency(clazz.toString())
    }

    companion object {
        fun builder(executor: Module.() -> Unit): Module {
            return Module().apply(executor)
        }
    }
}
