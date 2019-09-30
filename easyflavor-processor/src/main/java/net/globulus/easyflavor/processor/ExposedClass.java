package net.globulus.easyflavor.processor;

import java.io.Serializable;
import java.util.List;

public class ExposedClass implements Serializable {

    public final String name;
    public final List<ExposedMethod> constructors; // public constructors

    public ExposedClass(String name, List<ExposedMethod> constructors) {
        this.name = name;
        this.constructors = constructors;
    }
}
