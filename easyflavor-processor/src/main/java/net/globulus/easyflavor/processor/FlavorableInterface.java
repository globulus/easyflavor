package net.globulus.easyflavor.processor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlavorableInterface implements Serializable {

    public final ExposedClass flavorableClass;
    public final boolean isInterface;
//    public final boolean proxied;
    private final Map<String, ExposedClass> flavorSubclasses = new HashMap<>();
    public final List<FlavorInjectMethod> flavorInjectMethods;

    FlavorableInterface(ExposedClass flavorableClass,
                        boolean isInterface,
                        List<FlavorInjectMethod> flavorInjectMethods) {
        this.flavorableClass = flavorableClass;
        this.isInterface = isInterface;
        this.flavorInjectMethods = flavorInjectMethods;
    }

    void addFlavorSubclass(ExposedClass flavorClass, String... flavors) {
        for (String flavor : flavors) {
            flavorSubclasses.put(flavor, flavorClass);
        }
    }

    public Set<Map.Entry<String, ExposedClass>> getFlavorSubclasses() {
        return flavorSubclasses.entrySet();
    }

    /**
     * Gets flavors that need subclasses for inject methods, i.e those that aren't already there
     * in flavorSubclasses.
     * @return
     */
    public Set<String> getSubclassFlavors() {
        Set<String> set = new HashSet<>();
        for (FlavorInjectMethod method : flavorInjectMethods) {
            set.addAll(method.getFlavors());
        }
        set.removeAll(flavorSubclasses.keySet());
        return set;
    }

    public Set<String> getAllFlavors() {
        Set<String> set = new HashSet<>();
        for (FlavorInjectMethod method : flavorInjectMethods) {
            set.addAll(method.getFlavors());
        }
        set.addAll(flavorSubclasses.keySet());
        return set;
    }
}
