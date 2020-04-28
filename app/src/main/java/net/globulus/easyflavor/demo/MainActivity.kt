package net.globulus.easyflavor.demo

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import net.globulus.easyflavor.EasyFlavor
import net.globulus.easyflavor.FlavorResolver
import net.globulus.easyflavor.android.flavoredViewModel
import net.globulus.easyflavor.demo.TestImpl
import net.globulus.easyflavor.demolib.Callback
import net.globulus.easyflavor.demolib.FtueManager
import net.globulus.easyflavor.demomidlib.AppFlavors

class MainActivity : AppCompatActivity() {

  private val viewModel: MainActivityViewModel by flavoredViewModel()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    setContentView(R.layout.activity_main)

    EasyFlavor.setResolver { AppFlavors.get() }

    Log.e("TITLE", "TITLE VALUE " + viewModel.getTitle())
    val ftueManager = EasyFlavor.get(FtueManager::class.java)
    ftueManager.signup("email", "password", object : Callback {
      override fun handle(value: Any?) {
        Log.e("CALLBACK",
            "Callback value: " + (value?.toString() ?: "null"))
      }
    })
    EasyFlavor.get(TestImpl::class.java, 3, "aaa").a()
  }
}