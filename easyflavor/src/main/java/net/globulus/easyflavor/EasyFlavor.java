package net.globulus.easyflavor;

public final class EasyFlavor {

    private static ProxyResolver proxyResolver;

    static {
        try {
            Class.forName("net.globulus.easyflavor.EasyFlavorProxyResolver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private EasyFlavor() { }

    public static void setResolver(FlavorResolver r) {
        proxyResolver.setResolver(r);
    }

    public static FlavorResolver getResolver() {
        return proxyResolver.getResolver();
    }

    @SuppressWarnings("unchecked")
    public static  <T> T get(Class<? super T> flavorableClass, Object... args) {
        return proxyResolver.get(flavorableClass, args);
    }

    static void setProxyResolver(ProxyResolver r) {
        proxyResolver = r;
    }
}
