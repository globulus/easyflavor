package net.globulus.easyflavor.processor;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FlavorableInterface implements Serializable {

    public final String flavorableClass;
    final Map<String, List<String>> flavorMap = new HashMap<>();

    FlavorableInterface(String flavorableClass) {
        this.flavorableClass = flavorableClass;
    }

    void addFlavors(String flavorClass, String[] flavors) {
        flavorMap.put(flavorClass, Arrays.asList(flavors));
    }

    public Set<Map.Entry<String, List<String>>> getFlavorEntries() {
        return flavorMap.entrySet();
    }
}
