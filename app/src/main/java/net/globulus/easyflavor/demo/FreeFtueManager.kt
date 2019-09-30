package net.globulus.easyflavor.demo

import android.util.Log
import net.globulus.easyflavor.annotation.EasyFlavorConfig
import net.globulus.easyflavor.annotation.Flavored
import net.globulus.easyflavor.demolib.Callback
import net.globulus.easyflavor.demolib.FtueManager
import net.globulus.easyflavor.demomidlib.AppFlavors
import net.globulus.easyflavor.runIfFree

@EasyFlavorConfig(sink = true, kotlinExtModule = "Demo")
@Flavored(flavors = [AppFlavors.FREE])
class FreeFtueManager : FtueManager {
    override fun signup(email: String, password: String, callback: Callback?) {
        val tag = this::class.java.simpleName
        Log.e(tag, "Free called $email $password")
        callback?.handle(tag)

        runIfFree {
            Log.e(tag, "RUNNING FREE")
        }
    }
}