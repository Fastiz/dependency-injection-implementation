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

class ContainerTest {

    @Test
    fun resolveDependencies() {
        val container = Container.builder {
            single { D(get()) }
            single { C(get()) }
            single { A() }
            single { B(get()) }
            single { E(get(), get()) }
        }

        assertDoesNotThrow { container.get<A>() }
        assertDoesNotThrow { container.get<B>() }
        assertDoesNotThrow { container.get<C>() }
        assertDoesNotThrow { container.get<D>() }
        assertDoesNotThrow { container.get<E>() }
    }

    @Test
    fun instancesMatchTheClasses() {
        val container = Container.builder {
            single { A() }
            single { B(get()) }
            single { C(get()) }
        }

        assertEquals("A", container.get<A>().print())
        assertEquals("B", container.get<B>().print())
        assertEquals("C", container.get<C>().print())
    }

    @Test
    fun throwWhenADependencyIsNotMet() {
        val container = Container.builder {
            single { D(get()) }
            single { A() }
            single { E(get(), get()) }
        }

        assertThrows<UnresolvedDependency> {
            container.get<E>()
        }
    }
}