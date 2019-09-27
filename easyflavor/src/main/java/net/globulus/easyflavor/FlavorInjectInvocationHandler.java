package net.globulus.easyflavor;

import net.globulus.easyflavor.annotation.FlavorInject;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

final class FlavorInjectInvocationHandler<T> implements InvocationHandler {

    private final T realInstance;
    private final T flavorInstance;

    FlavorInjectInvocationHandler(T realInstance, T flavorInstance) {
        this.realInstance = realInstance;
        this.flavorInstance = flavorInstance;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        Class<?> realClass = realInstance.getClass();
        Method realMethod = realClass.getMethod(method.getName(), method.getParameterTypes());
        FlavorInject annotation = realMethod.getAnnotation(FlavorInject.class);
        Object result = null;
        if (annotation != null && flavorInstance != null) {
            FlavorInject.Mode mode = annotation.mode();

            if (mode == FlavorInject.Mode.BEFORE || mode == FlavorInject.Mode.BEFORE_SUPER) {
                result = method.invoke(flavorInstance, args);
            }

            Object realResult;
            if (mode.isSuper()) {
                Constructor<MethodHandles.Lookup> constructor =
                        MethodHandles.Lookup.class.getDeclaredConstructor(Class.class, int.class);
                constructor.setAccessible(true);
                Object[] methodArgs = (args != null) ? args : new Object[0];
                realResult = constructor.newInstance(realClass, MethodHandles.Lookup.PRIVATE)
                        .unreflectSpecial(method, realClass)
                        .bindTo(flavorInstance)
                        .invokeWithArguments(methodArgs);
            } else {
                realResult = realMethod.invoke(realInstance, args);
            }

            if (mode == FlavorInject.Mode.AFTER || mode == FlavorInject.Mode.AFTER_SUPER) {
                result = method.invoke(flavorInstance, args);
            }

            if (result == null) {
                result = realResult;
            }
        } else {
            result = method.invoke(realInstance, args);
        }

        // If the invoked method returned "this", we need to reroute it back to the Proxy
        // so that subsequent method calls are invoked on the Proxy again.
        if (result == realInstance || result == flavorInstance) {
            result = o;
        }

        return result;
    }
}
