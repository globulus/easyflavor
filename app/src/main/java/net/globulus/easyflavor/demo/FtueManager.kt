package net.globulus.easyflavor.demo

@Flavorable
interface FtueManager {
    fun signup(email: String, password: String, callback: Callback?)
}
