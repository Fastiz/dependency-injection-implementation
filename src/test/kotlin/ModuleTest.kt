import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertContains
import kotlin.test.assertEquals

class A
class B(a: A)
class C(a: A)
class D(b: B)
class E(a: A, b: B)

class ModuleTest {

    @Test
    fun resolveDependencies() {
        val mod = Module()

        mod.configure {
            single { D(get()) }
            single { C(get()) }
            single { A() }
            single { B(get()) }
            single { E(get(), get()) }
        }

        assertDoesNotThrow { mod.get<A>() }
        assertDoesNotThrow { mod.get<B>() }
        assertDoesNotThrow { mod.get<C>() }
        assertDoesNotThrow { mod.get<D>() }
        assertDoesNotThrow { mod.get<E>() }
    }

    @Test
    fun throwWhenADependencyIsNotMet() {
        val mod = Module()

        assertThrows<UnresolvedDependency> {
            mod.configure {
                single { D(get()) }
                single { C(get()) }
                single { A() }
                single { E(get(), get()) }
            }
        }
    }
}