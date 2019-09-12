package net.globulus.easyflavor.demo

object AppFlavors {
    const val FREE = "free"
    const val FULL = "full"

    @JvmStatic
    fun get() = if (BuildConfig.FULL_VERSION) FULL else FREE
}
