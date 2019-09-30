package net.globulus.easyflavor.processor;

import net.globulus.easyflavor.annotation.FlavorInject;

import java.util.Map;
import java.util.Set;

public class FlavorInjectMethod {

    public final FlavorInject.Mode mode;
    public final ExposedMethod originalMethod;
    public final Map<String, ExposedMethod> flavoredMethods;

    FlavorInjectMethod(FlavorInject.Mode mode,
                       ExposedMethod originalMethod,
                       Map<String, ExposedMethod> flavoredMethods) {
        this.mode = mode;
        this.originalMethod = originalMethod;
        this.flavoredMethods = flavoredMethods;
    }

    public Set<String> getFlavors() {
        return flavoredMethods.keySet();
    }
}
