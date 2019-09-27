package net.globulus.easyflavor.demo

import android.util.Log
import net.globulus.easyflavor.annotation.FlavorInject
import net.globulus.easyflavor.annotation.Flavorable
import net.globulus.easyflavor.annotation.Flavored
import net.globulus.easyflavor.demomidlib.AppFlavors

@Flavorable(proxied = true)
interface Test {
    fun i(): Test
    fun a()
}

open class TestImpl : Test {
    override fun i(): Test {
        Log.e("AAAA", "INIT TEST")
        return this
    }

    @FlavorInject(mode = FlavorInject.Mode.BEFORE_SUPER)
    override fun a() {
        Log.e("AAAA", "ORIG TEST")
        Log.e("AAAA", "TEST COLOR ${color()}")

    }

    open fun color() = 0
}

@Flavored(flavors = [AppFlavors.FREE])
class FreeTest : TestImpl() {
    override fun a() {
        Log.e("AAAA", "FREE TEST")
    }

    override fun color() = 1
}

@Flavored(flavors = [AppFlavors.FULL])
class FullTest : TestImpl() {
    override fun a() {
        Log.e("AAAA", "FULL TEST")
    }

    override fun color() = 2
}