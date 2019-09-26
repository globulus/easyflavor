# EasyFlavor

**EasyFlavor** is a lightweight dependency injection lib for apps with several configurations/flavors that use a common codebase which slightly differs based on the flavor type.

Take, for example, an app that has a trial and a full version. Their codebase is exactly the same, but the trial version has some features taken out, and some parts of the code behaving differently than the full version. With **EasyFlavor**, the differences can neatly be put in a clean architecture using a few annotations, and the boilerplate code generated automatically. Naturally, the library can scale as much as possible, handling large projects with many different flavors.

The architecture enforced by the library fits neatly into popular options, such as MVP or MVVM. It's also not meant to replace or be a dumbed-down version of other DI tools, such as [Dagger](https://github.com/google/dagger), but to [complement them](#notes) by providing boilerplate code.

**EasyFlavor** uses no Android dependencies so it's shipped as a Java library, meaning that it can be used in pure Java and Android projects alike.

The library works with **Kotlin** as well as Java, as illustrated by the [demo app](app/).

EasyFlavor uses [MMAP](https://github.com/globulus/mmap) to allow for multi-module annoation processing.

### Installation

EasyFlavor is hosted on JCenter - just add the EasyFlavor dependency and annotation processor:

```gradle
dependencies {
   implementation 'net.globulus.easyflavor:easyflavor:1.0.2'
   implementation 'net.globulus.easyflavor:easyflavor-annotations:1.0.2'
   annotationProcessor 'net.globulus.easyflavor:easyflavor-processor:1.0.2'
   // and/or
   kapt 'net.globulus.easyflavor:easyflavor-processor:1.0.2'
}
```

### How to use

1. Create an interface for the part of code that depends on flavors, and *annotate it with* **@Flavorable**. E.g, here's an interface describing a View Model of a MainActivity:

```java
@Flavorable
interface MainActivityViewModel {
    void fetchData(String collection, Callback callback);
}
```

2. Provide a common-code implementation of the interface. **Its name must be the same as that of the interface + Impl.** Annotate those methods that depend on flavors with **@FlavorInject**. Use **mode** to tell if the flavor injected code is supposed to be executed *before* or *after* the common code (default is after):

```java
class MainActivityViewModelImpl implements MainActivityViewModel {
    @FlavorInject(mode = FlavorInject.Mode.BEFORE)
    void fetchData(String collection, Callback callback) {
         Log.e(this.getClass().getSimpleName(), "Common code called");
    }
}
```

3. Define your app flavors and tell EasyFlavor how to resolve which flavor the app is running:

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

4. Provide flavor-specific implementations of the interface. Annotate them with **@Flavored**, providing an array of flavors as Strings for which this implementation is valid:

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

5. When instantiating the View Model, use **EasyFlavor.get(CLASS)** instead of instantiating manually:
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

6. PROFIT! Whenever any annotated method of the ViewModel is invoked, EasyFlavor will **inject the flavor-specific ViewModel code before or after the common one**.

### Multi-module support

EasyFlavor supports hierarchical modules, meaning that it can [generate the final code based on multiple layers of modules](https://github.com/globulus/mmap), from top-level libraries all the way down to the app itself.

Because of this, it's **necessary to do the following**:

1. Annotate one class (just one, any one) in your *topmost* module with *@Source*.
2. Annoate one class (again, just one) in your *bottommost* module(s) with *@Sink*.

That's it! These annotations will tell the processor how's your architecture oriented and allow it to generate all of its files so that no conflicts arise.


### Notes

* Since the *EasyFlavor* class is generated by the annotation processor, you'd need to build your project for the first time before it becomes available (for, among other things, setting the *FlavorResolver*). Afterwards, any change you make on EasyFlavor annotations will automatically reflect in the generated code.
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
* An obvious drawback to EasyFlavor DI is that it only supports *parameter-less constructors*, which in itself may not be an issue at all.
