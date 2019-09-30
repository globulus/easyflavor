package net.globulus.easyflavor.demomidlib

import android.util.Log
import net.globulus.easyflavor.annotation.EasyFlavorConfig
import net.globulus.easyflavor.annotation.Flavored
import net.globulus.easyflavor.demolib.Callback
import net.globulus.easyflavor.demolib.FtueManager
import net.globulus.easyflavor.runIfFull

@EasyFlavorConfig(kotlinExtModule = "Demomidlib")
@Flavored(flavors = [AppFlavors.FULL])
class FullFtueManager : FtueManager {
    override fun signup(email: String, password: String, callback: Callback?) {
        val tag = this::class.java.simpleName
        Log.e(tag, "Full called $email $password")
        callback?.handle(tag)

        runIfFull {
            Log.e(tag, "RUN FULL")
        }
    }
}