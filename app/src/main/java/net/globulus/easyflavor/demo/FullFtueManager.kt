package net.globulus.easyflavor.demo

import android.util.Log

@Flavored(flavors = [AppFlavors.FULL])
class FullFtueManager : FtueManager {
    override fun signup(email: String, password: String, callback: Callback?) {
        val tag = this::class.java.simpleName
        Log.e(tag, "Full called $email $password")
        callback?.handle(tag)
    }
}