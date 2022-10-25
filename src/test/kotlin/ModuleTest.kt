import org.junit.jupiter.api.Test
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

        val classes = mod.instances.map { it.clazz }

        assertContains(classes, A::class)
        assertContains(classes, B::class)
        assertContains(classes, C::class)
        assertContains(classes, D::class)
        assertContains(classes, E::class)
        assertEquals(5, mod.instances.size)
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