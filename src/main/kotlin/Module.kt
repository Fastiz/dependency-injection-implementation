import kotlin.reflect.KClass

class ClassFactory(
    val clazz: KClass<*>,
    val factory: () -> Any,
)

class ClassInstance(
    val clazz: KClass<*>,
    val instance: Any,
)

class UnresolvedDependency(val dependencyName: String) : Throwable()

class Module {
    val factories = mutableListOf<ClassFactory>()
    val instances = mutableListOf<ClassInstance>()

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
        val classFactory = ClassFactory(T::class, factory)
        factories.add(classFactory)
    }

    inline fun <reified T> get(): T {
        val classInstance = instances.find { it.clazz == T::class }

        if (classInstance != null) {
            return classInstance.instance as T
        }

        throw UnresolvedDependency(T::class.toString())
    }
}
