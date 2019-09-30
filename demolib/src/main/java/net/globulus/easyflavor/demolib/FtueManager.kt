package net.globulus.easyflavor.demolib

import net.globulus.easyflavor.annotation.EasyFlavorConfig
import net.globulus.easyflavor.annotation.Flavorable

@EasyFlavorConfig(source = true)
@Flavorable
interface FtueManager {
    fun signup(email: String, password: String, callback: Callback?)
}
