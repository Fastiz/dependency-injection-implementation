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

    fun configure(executor: Module.() -> Unit) {
        this.executor()

        var modified = true
        var unresolvedDependencies: MutableList<UnresolvedDependency>
        var remainingFactories = factories
        while (remainingFactories.isNotEmpty() && modified) {
            modified = false
            unresolvedDependencies = mutableListOf()
            val aux = mutableListOf<ClassFactory>()
            remainingFactories.forEach {
                try {
                    val instance = it.factory()
                    instances.add(ClassInstance(it.clazz, instance))
                    modified = true
                } catch (e: UnresolvedDependency) {
                    unresolvedDependencies.add(e)
                    aux.add(it)
                }
            }
            remainingFactories = aux
        }

        if (remainingFactories.isNotEmpty()) {
            throw UnresolvedDependency(remainingFactories.joinToString { it.clazz.toString() })
        }
    }

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

        throw UnresolvedDependency(clazz.toString())
    }
}
