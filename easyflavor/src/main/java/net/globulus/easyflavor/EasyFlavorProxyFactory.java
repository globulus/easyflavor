package net.globulus.easyflavor;

import java.lang.reflect.Modifier;

import javassist.util.proxy.Proxy;
import javassist.util.proxy.ProxyFactory;

final class EasyFlavorProxyFactory {

    private EasyFlavorProxyFactory() { }

    @SuppressWarnings("unchecked")
    static <T> T createProxy(Class<? super T> clazz, T realInstance, T flavorInstance)
            throws InstantiationException, IllegalAccessException {
        ProxyFactory factory = new ProxyFactory();
        factory.setSuperclass(clazz);
        factory.setFilter(method -> Modifier.isAbstract(method.getModifiers()));
        Class c = factory.createClass();
        FlavorInjectMethodHandler<T> handler = new FlavorInjectMethodHandler<>(realInstance, flavorInstance);
        T instance = (T) c.newInstance();
        ((Proxy) instance).setHandler(handler);
        return instance;
    }
}
