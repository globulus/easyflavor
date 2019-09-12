package net.globulus.easyflavor.demo

import net.globulus.easyflavor.annotation.Flavorable

@Flavorable
interface FtueManager {
    fun signup(email: String, password: String, callback: Callback?)
}
