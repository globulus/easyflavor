package net.globulus.easyflavor.demo

import androidx.lifecycle.ViewModel
import net.globulus.easyflavor.annotation.FlavorInject
import net.globulus.easyflavor.annotation.Flavorable
import net.globulus.easyflavor.annotation.Flavored
import net.globulus.easyflavor.demomidlib.AppFlavors

@Flavorable
open class MainActivityViewModel : ViewModel() {

  @FlavorInject
  open fun getTitle() = ""

  @Flavored(flavors = [AppFlavors.FREE])
  internal fun getTitleFree() = "FREE"

  @Flavored(flavors = [AppFlavors.FULL])
  internal fun getTitleFull() = "FULL"
}