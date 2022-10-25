import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

interface Printable {
    fun print(): String
}

class A : Printable {
    override fun print() = "A"
}

class B(a: A) : Printable {
    override fun print() = "B"
}

class C(a: A) : Printable {
    override fun print() = "C"
}

class D(b: B) : Printable {
    override fun print() = "D"
}

class E(a: A, d: D) : Printable {
    override fun print() = "E"
}

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
    fun instancesMatchTheClasses() {
        val module = Module.builder {
            single { A() }
            single { B(get()) }
            single { C(get()) }
        }

        assertEquals("A", module.get<A>().print())
        assertEquals("B", module.get<B>().print())
        assertEquals("C", module.get<C>().print())
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