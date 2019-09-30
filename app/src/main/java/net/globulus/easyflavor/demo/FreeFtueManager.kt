package net.globulus.easyflavor.demo

import android.util.Log
import net.globulus.easyflavor.annotation.EasyFlavorConfig
import net.globulus.easyflavor.annotation.Flavored
import net.globulus.easyflavor.demolib.Callback
import net.globulus.easyflavor.demolib.FtueManager
import net.globulus.easyflavor.demomidlib.AppFlavors
import net.globulus.easyflavor.runFree

@EasyFlavorConfig(sink = true, kotlinExt = true)
@Flavored(flavors = [AppFlavors.FREE])
class FreeFtueManager : FtueManager {
    override fun signup(email: String, password: String, callback: Callback?) {
        val tag = this::class.java.simpleName
        Log.e(tag, "Free called $email $password")
        callback?.handle(tag)

        runFree {
            Log.e(tag, "RUNNING FULL")
        }
    }
}