package net.globulus.easyflavor;

import net.globulus.easyflavor.annotation.FlavorInject;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

class FlavorInjectInvocationHandler<T> implements InvocationHandler {

    private final T realInstance;
    private final T flavorInstance;

    FlavorInjectInvocationHandler(T realInstance, T flavorInstance) {
        this.realInstance = realInstance;
        this.flavorInstance = flavorInstance;
    }

    @Override
    public Object invoke(Object o, Method method, Object[] args) throws Throwable {
        Method realMethod = realInstance.getClass()
                .getMethod(method.getName(), method.getParameterTypes());
        FlavorInject flavorInject = realMethod.getAnnotation(FlavorInject.class);
        Object result = null;
        if (flavorInject != null && flavorInstance != null) {
            if (flavorInject.mode() == FlavorInject.Mode.BEFORE) {
                result = method.invoke(flavorInstance, args);
            }
            Object realResult = method.invoke(realInstance, args);
            if (flavorInject.mode() == FlavorInject.Mode.AFTER) {
                result = method.invoke(flavorInstance, args);
            }
            if (result == null) {
                result = realResult;
            }
        } else {
            result = method.invoke(realInstance, args);
        }
        return result;
    }
}
