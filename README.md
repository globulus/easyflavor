# EasyFlavor

**EasyFlavor** is a lightweight dependency injection lib for apps with several configurations/flavors that use a common codebase which slightly differs based on the flavor type.

Take, for example, an app that has a trial and a full version. Their codebase is exactly the same, but the trial version has some features taken out, and some parts of the code behaving differently than the full version. With **EasyFlavor**, the differences can neatly be put in a clean architecture using a few annotations, and the boilerplate code generated automatically. Naturally, the library can scale as much as possible, handling large projects with many different flavors.

The architecture enforced by the library fits neatly into popular options, such as MVP or MVVM. It's also not meant to replace or be a dumbed-down version of other DI tools, such as [Dagger](https://github.com/google/dagger), but to [complement them](#notes) by providing boilerplate code.

**EasyFlavor** uses no Android dependencies so it's shipped as a Java library, meaning that it can be used in pure Java and Android projects alike.

The library works with **Kotlin** as well as Java, as illustrated by the [demo app](app/). It also has the ability to generate additional [Kotlin extensions code](#kotlin-extensions).

**AndroidX VieModels** are fully supported via [*flavoredViewModel* property delegate](#viewmodel-support).

EasyFlavor uses [MMAP](https://github.com/globulus/mmap) to allow for multi-module annotation processing. It relies purely on generated code, and not reflection, meaning that there's no performance overhead is introduced.

### Installation

EasyFlavor is hosted on JCenter - just add the EasyFlavor dependency and annotation processor:

```gradle
dependencies {
   implementation 'net.globulus.easyflavor:easyflavor:1.0.6'
   implementation 'net.globulus.easyflavor:easyflavor-annotations:1.0.6'
   annotationProcessor 'net.globulus.easyflavor:easyflavor-processor:1.0.6'
   // and/or
   kapt 'net.globulus.easyflavor:easyflavor-processor:1.0.6'
}
```

### How to use

#### Define app flavors

Define your app flavors and tell *EasyFlavor* how to resolve which flavor the app is running:

```java
class MyApp extends Application {
    public static final String FULL = "full";
    public static final String FREE = "free";
    
    @Override
    public void onCreate() {
        super.onCreate();
        
        // FlavorResolver is a simple functional interface that returns
        // a String describing the app flavor. It's up to you to define
        // exactly how is the app flavor determined.
        EasyConfig.setResolver(() -> (BuildConfig.FULL_VERSION) ? FULL : FREE);
    }
}
```

#### Designate a *Flavorable* type

For the part of code that depends on flavors, create a class or an interface and *annotate it with* **@Flavorable**. E.g, here's an interface describing a View Model of a MainActivity:

```java
@Flavorable
class MainActivityViewModel {
    void fetchData(String collection, Callback callback);
}
```

#### Option #1 - Add a *Flavored* subclass/implementation

Provide flavor-specific implementations of the interface. Annotate them with **@Flavored**, providing an array of flavors as Strings for which this implementation is valid:

```java
@Flavored(flavors = {MyApp.FREE})
class FreeMainActivityViewModel implements MainActivityViewModel {
    void fetchData(String collection, Callback callback) {
        String tag = this.getClass().getSimpleName();
        Log.e(tag, "FREE called for " + collection);
        callback.handle(tag);
    }
}
```

```java
@Flavored(flavors = {MyApp.FULL})
class FullMainActivityViewModel implements MainActivityViewModel {
    void fetchData(String collection, Callback callback) {
        String tag = this.getClass().getSimpleName();
        Log.e(tag, "FULL called for " + collection);
        callback.handle(tag);
    }
}
```

#### Option #2 - *FlavorInject* methods directly

You may also specify methods in your *Flavorable* class that are replaced, prefixed or suffixed by other methods, depening on the app Flavor.

1. Annotate those methods with **@FlavorInject**, optionally specifying the execution mode (*before*, *after*, or by default *replace*).

2. Annotate flavor-specific methods with **@Flavored**, also providing an array of flavors. **The names of flavored methods must contain the name of its FlavorInject counterpart at either beginning or end**.

```java
@Flavorable
class FtueManager {
    
    @FlavorInject(mode = FlavorInject.Mode.BEFORE)
    void fetchData(String collection, Callback callback) {
         Log.e(this.getClass().getSimpleName(), "Common code called");
    }
    
    @Flavored(flavors = {MyApp.FULL})
    void fullFetchData(String collection, Callback callback) {
         Log.e(this.getClass().getSimpleName(), "Full version fetch data");
    }
    
    @Flavored(flavors = {MyApp.FREE})
    void freeFetchData(String collection, Callback callback) {
         Log.e(this.getClass().getSimpleName(), "Free version fetch data");
    }
}
```

*EasyFlavor* will generate a subclasses of your *Flavorable* that have the flavors-specific code injected into *FlavorInject* methods based on rules described above.

#### Obtain flavored instances

When instantiating the Flavorable, use **EasyFlavor.get(CLASS, ARGS...)** instead of instantiating manually:
```java
class MainActivity extends Activity {
    
    private MainActivityViewModel viewModel;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        viewModel = EasyFlavor.get(MainActivityViewModel.class);
    }
}
```

For classes and generated subclasses, *EasyFlavor* **automatically resolves constructors based on types and length of the passed argument list**:

```java
@Flavorable
class FtueManager {
    
    public FtueManager(String mode) {
        ...
    }
    
    public FtueManager(String mode, String tag) {
        ...
    }
}

...

class MainActivity extends Activity {
    
    private FtueManager ftueManager;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        ...
        ftueManager = EasyFlavor.get(MainActivityViewModel.class, "mode", "tag");
    }
}
```

Note that primitive types in constructors aren't allowed given that the args list is an array of Object. For Kotlin, this isn't an issue as the processor will automatically wrap primitive types

### Multi-module support

EasyFlavor supports hierarchical modules, meaning that it can [generate the final code based on multiple layers of modules](https://github.com/globulus/mmap), from top-level libraries all the way down to the app itself.

Because of this, it's **necessary to do the following**:

1. Annotate one class (just one, any one) in your *topmost* module with *@EasyFlavorConfig(source = true)*.
2. Annotate one class (again, just one) in your *bottommost* module(s) with *@EasyFlavorConfig(sink = true)*.

That's it! These annotations will tell the processor how's your architecture oriented and allow it to generate all of its files so that no conflicts arise.


### Notes

* *EasyFlavor.get()* fits nicely with existing DI solutions, making it dead easy to link the EasyFlavor injection with, e.g, Dagger2:
```kotlin
class MainActivity extends Activity {
   @Inject
   lateinit var viewModel: MainActivityViewModel
}
```

```kotlin
@Module
class ViewModelModule {
   @Provides
   fun provideMainActivityViewModel() = EasyFlavor.get(MainActivityViewModel::class.java)
}
```

* If you're using EasyFlavor with multiple modules, it may be necessary to clean your project and recompile if the processor begins to complain that a certain class isn't a subtype of a *Flavorable* class.

### Kotlin extensions

EasyFlavor's processor can generate a Kotlin file containing functions that make it easy to run a certain block of code based on a Flavor (or multiple of them). E.g, here's what this file looks like if you have two flavors in your app, "Free" and "Full":

```kotlin
public fun <T> runIf(flavors: Array<String>, block: () -> T): T? {
  if (flavors.contains(EasyFlavor.getResolver().resolve())) {
    return block()
  }
  return null
}

public fun <T> runUnless(flavors: Array<String>, block: () -> T): T? {
  if (!flavors.contains(EasyFlavor.getResolver().resolve())) {
    return block()
  }
  return null
}

public fun <T> runIfFree(block: () -> T): T? {
  if (EasyFlavor.getResolver().resolve() == "free") {
    return block()
  }
  return null
}

public fun <T> runUnlessFree(block: () -> T): T? {
  if (EasyFlavor.getResolver().resolve() != "free") {
    return block()
  }
  return null
}

public fun <T> runIfFull(block: () -> T): T? {
  if (EasyFlavor.getResolver().resolve() == "full") {
    return block()
  }
  return null
}

public fun <T> runUnlessFull(block: () -> T): T? {
  if (EasyFlavor.getResolver().resolve() != "full") {
    return block()
  }
  return null
}
```

Then, you can do something like this:

```kotlin
class SomeClass {
    fun someMethod() {
        // Do something
        runIfFree {
            // Do something only if current flavor is free
        }
        // Do something else
    }
}
```

To enable this feature, annotate one class per block with **@EasyFlavorConfig** and provide a module name to the *kotlinExtModule* param:

```kotlin
@EasyFlavorConfig(kotlinExtModule = "MyModule")
class SomeClassInModule
```

Specifying unique names for your modules allows you to use these functions from all your modules, regardless of their hierarchy. Omitting the module will make it so that Kotlin files aren't generated (if, e.g, you only use Java in your project).

### ViewModel Support

If your Flavorable class extends [ViewModel](https://developer.android.com/topic/libraries/architecture/viewmodel), you can inject it via the **flavoredViewModel** property delegate:

```kotlin
@Flavorable
open class MainActivityViewModel : ViewModel() {

  @FlavorInject
  open fun getTitle() = ""

  @Flavored(flavors = [AppFlavors.FREE])
  internal fun getTitleFree() = "FREE"

  @Flavored(flavors = [AppFlavors.FULL])
  internal fun getTitleFull() = "FULL"
}
```

```kotlin

class MainActivity : AppCompatActivity() {

  private val viewModel: MainActivityViewModel by flavoredViewModel()
```

*flavoredViewModel* will internally use the *ViewModelProvider* to instantiate the appropriate flavored instance for your ViewModel. Note that you can pass any arguments to *flavoredViewModel* as you would for *EasyFlavor.get*, except the class, as it's inferred automatically.