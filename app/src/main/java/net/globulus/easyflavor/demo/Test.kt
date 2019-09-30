package net.globulus.easyflavor.demo

import android.util.Log
import net.globulus.easyflavor.annotation.FlavorInject
import net.globulus.easyflavor.annotation.Flavorable
import net.globulus.easyflavor.annotation.Flavored
import net.globulus.easyflavor.demomidlib.AppFlavors

@Flavorable
open class TestImpl  {
    @FlavorInject(mode = FlavorInject.Mode.REPLACE)
    open fun a() {
        Log.e("AAAA", "ORIG TEST")
    }

    @Flavored(flavors = [AppFlavors.FREE])
    fun freeA() {
        Log.e("AAAA", "FREE TEST")
    }

    @Flavored(flavors = [AppFlavors.FULL])
    fun fullA() {
        Log.e("AAAA", "FULL TEST")
    }
}
