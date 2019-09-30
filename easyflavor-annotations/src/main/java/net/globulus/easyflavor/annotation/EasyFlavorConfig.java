package net.globulus.easyflavor.annotation;

public @interface EasyFlavorConfig {
    boolean source() default false;
    boolean sink() default false;
    String kotlinExtModule() default "";
}
