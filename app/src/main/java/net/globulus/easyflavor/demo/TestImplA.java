package net.globulus.easyflavor.demo;

import android.util.Log;
import net.globulus.easyflavor.annotation.FlavorInject;
import net.globulus.easyflavor.annotation.Flavorable;
import net.globulus.easyflavor.annotation.Flavored;
import net.globulus.easyflavor.demomidlib.AppFlavors;

@Flavorable
public class TestImplA  {

    public TestImplA(int mode) {

    }

    public TestImplA(int mode, String type) {
        Log.e("AAAA", "${this::class.java.simpleName}, $a, $b");
    }

    @FlavorInject(mode = FlavorInject.Mode.REPLACE)
    void a() {
        Log.e("AAAA", "ORIG TEST");
    }

    @Flavored(flavors = {AppFlavors.FREE})
    void freeA() {
        Log.e("AAAA", "FREE TEST");
    }

    @Flavored(flavors = {AppFlavors.FULL})
    void fullA() {
        Log.e("AAAA", "FULL TEST");
    }
}
