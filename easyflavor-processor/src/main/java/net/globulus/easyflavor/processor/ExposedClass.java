package net.globulus.easyflavor.processor;

import java.io.Serializable;
import java.util.List;

public class ExposedClass implements Serializable {

    public final String name;
    public final List<List<String>> constructors; // public constructors

    public ExposedClass(String name, List<List<String>> constructors) {
        this.name = name;
        this.constructors = constructors;
    }
}
