package net.globulus.easyflavor.demolib

import net.globulus.easyflavor.annotation.Flavorable
import net.globulus.mmap.Source

@Source
@Flavorable
interface FtueManager {
    fun signup(email: String, password: String, callback: Callback?)
}
