package org.koin.test.core

import org.junit.Assert
import org.junit.Test
import org.koin.dsl.path.ModulePath
import org.koin.dsl.module.module
import org.koin.standalone.StandAloneContext.startKoin
import org.koin.standalone.get
import org.koin.test.AutoCloseKoinTest
import org.koin.test.ext.junit.assertContexts
import org.koin.test.ext.junit.assertIsInModulePath
import org.koin.test.ext.junit.assertDefinitions
import org.koin.test.ext.junit.assertRemainingInstances

class FactoryInstanceCreationTest : AutoCloseKoinTest() {

    val FlatModule = module {
        factory { ComponentA() }
        single { ComponentB(get()) }
        single { ComponentC(get(), get()) }
    }

    val HierarchicModule = module {
        factory { ComponentA() }

        module("B") {
            single { ComponentB(get()) }

            module("C") {
                single { ComponentC(get(), get()) }
            }
        }
    }

    class ComponentA
    class ComponentB(val componentA: ComponentA)
    class ComponentC(val componentB: ComponentB, val componentA: ComponentA)


    @Test
    fun `load and create instances for flat module`() {
        startKoin(listOf(FlatModule))

        val a = get<ComponentA>()
        val b = get<ComponentB>()
        val c = get<ComponentC>()

        Assert.assertNotNull(a)
        Assert.assertNotNull(b)
        Assert.assertNotNull(c)
        Assert.assertNotEquals(a, b.componentA)
        Assert.assertNotEquals(a, c.componentA)
        Assert.assertEquals(b, c.componentB)

        assertRemainingInstances(2)
        assertDefinitions(3)
        assertContexts(1)
        assertIsInModulePath(ComponentA::class, ModulePath.ROOT)
        assertIsInModulePath(ComponentB::class, ModulePath.ROOT)
        assertIsInModulePath(ComponentC::class, ModulePath.ROOT)
    }

    @Test
    fun `load and create instances for hierarchic context`() {
        startKoin(listOf(HierarchicModule))

        val a = get<ComponentA>()
        val b = get<ComponentB>()
        val c = get<ComponentC>()

        Assert.assertNotNull(a)
        Assert.assertNotNull(b)
        Assert.assertNotNull(c)
        Assert.assertNotEquals(a, b.componentA)
        Assert.assertNotEquals(a, c.componentA)
        Assert.assertEquals(b, c.componentB)

        assertRemainingInstances(2)
        assertDefinitions(3)
        assertContexts(3)
        assertIsInModulePath(ComponentA::class, ModulePath.ROOT)
        assertIsInModulePath(ComponentB::class, "B")
        assertIsInModulePath(ComponentC::class, "C")
    }

}