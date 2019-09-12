package net.globulus.easyflavor.demo;

import android.content.Context;

import androidx.annotation.NonNull;

import java.lang.reflect.Proxy;

public final class EasyFlavors {

    private static Resolver resolver;

    private EasyFlavors() { }

    public static void setResolver(@NonNull Resolver r) {
        resolver = r;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Context context, Class<? super T> clazz) {
        try {
            if (FtueManager.class.equals(clazz)) {
                FtueManager realInstance = (FtueManager) Class.forName("net.globulus.easyflavor.demo.FtueManagerImpl").newInstance();
                FtueManager flavorInstance = null;
                switch (resolver.resolve(context)) {
                    case AppFlavors.FREE:
                        flavorInstance = (FtueManager) Class.forName("net.globulus.easyflavor.demo.FreeFtueManager").newInstance();
                        break;
                    case AppFlavors.FULL:
                        flavorInstance = (FtueManager) Class.forName("net.globulus.easyflavor.demo.FullFtueManager").newInstance();
                        break;
                }
                return (T) Proxy.newProxyInstance(
                        EasyFlavors.class.getClassLoader(),
                        new Class[] { FtueManager.class },
                        new FlavorInjectInvocationHandler<>(realInstance, flavorInstance)
                );
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            e.printStackTrace();
        }
        throw new IllegalArgumentException("Unable to find flavors for class: " + clazz);
    }

    @FunctionalInterface
    interface Resolver {
        String resolve(Context context);
    }
}
