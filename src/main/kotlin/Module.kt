import kotlin.reflect.KClass

class UnresolvedDependency(dependencyName: String) : Throwable(dependencyName)

class Module {
    private val factories = mutableMapOf<KClass<*>, () -> Any>()
    private val instances = mutableMapOf<KClass<*>, Any>()

    inline fun <reified T : Any> single(noinline factory: () -> T) {
        val clazz = T::class
        single(clazz, factory)
    }

    fun single(clazz: KClass<*>, factory: () -> Any) {
        factories[clazz] = factory
    }

    inline fun <reified T> get(): T {
        val clazz = T::class
        return get(clazz)
    }

    fun <T> get(clazz: KClass<*>): T {
        val classInstance = instances[clazz]

        if (classInstance != null) {
            return classInstance as T
        }

        val factoryInstance = factories[clazz]

        if (factoryInstance != null) {
            val newInstance = factoryInstance()
            instances[clazz] = newInstance
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
