package net.globulus.easyflavor.android

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.Factory
import androidx.lifecycle.ViewModelStoreOwner
import net.globulus.easyflavor.EasyFlavor

inline fun <reified T> ViewModelStoreOwner.flavoredViewModel(vararg args: Any?): Lazy<T> where T : ViewModel {
  return lazy {
    ViewModelProvider(this, object : Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <V : ViewModel?> create(modelClass: Class<V>): V {
        return EasyFlavor.get(T::class.java, *args) as V
      }
    }).get(T::class.java)
  }
}