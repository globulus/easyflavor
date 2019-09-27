package net.globulus.easyflavor.demolib

import net.globulus.easyflavor.annotation.Flavorable
import net.globulus.mmap.Source

@Source
@Flavorable(proxied = true)
interface FtueManager {
    fun signup(email: String, password: String, callback: Callback?)
}
