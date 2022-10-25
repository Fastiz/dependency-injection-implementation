import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows

class A
class B(a: A)
class C(a: A)
class D(b: B)
class E(a: A, d: D)

class ModuleTest {

    @Test
    fun resolveDependencies() {
        val module = Module.builder {
            single { D(get()) }
            single { C(get()) }
            single { A() }
            single { B(get()) }
            single { E(get(), get()) }
        }

        assertDoesNotThrow { module.get<A>() }
        assertDoesNotThrow { module.get<B>() }
        assertDoesNotThrow { module.get<C>() }
        assertDoesNotThrow { module.get<D>() }
        assertDoesNotThrow { module.get<E>() }
    }

    @Test
    fun throwWhenADependencyIsNotMet() {
        val module = Module.builder {
            single { D(get()) }
            single { A() }
            single { E(get(), get()) }
        }

        assertThrows<UnresolvedDependency> {
            module.get<E>()
        }
    }
}