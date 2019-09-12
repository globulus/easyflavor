package net.globulus.easyflavor.demo

import android.util.Log

class FtueManagerImpl : FtueManager {
    @FlavorInject
    override fun signup(email: String, password: String, callback: Callback?) {
        Log.e(this::class.java.simpleName, "FtueManagerImpl called with $email $password")
    }
}
