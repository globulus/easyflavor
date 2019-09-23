package net.globulus.easyflavor.demolib

import net.globulus.easyflavor.annotation.Flavorable

@Flavorable(origin = true)
interface FtueManager {
    fun signup(email: String, password: String, callback: Callback?)
}
