package net.globulus.easyflavor.demo;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface FlavorInject {

    Mode mode() default Mode.AFTER;

    enum Mode {
        BEFORE, AFTER
    }
}
