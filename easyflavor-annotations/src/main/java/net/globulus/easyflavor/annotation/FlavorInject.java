package net.globulus.easyflavor.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface FlavorInject {

    Mode mode() default Mode.REPLACE;

    enum Mode {
        REPLACE, BEFORE, AFTER//, BEFORE_SUPER, AFTER_SUPER;

//        public boolean isSuper() {
//            return this == BEFORE_SUPER || this == AFTER_SUPER;
//        }
    }
}
