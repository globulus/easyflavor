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

    public static <T> T get(Class<? super T> flavorableClass) {
        return proxyResolver.get(flavorableClass);
    }

    static void setProxyResolver(ProxyResolver r) {
        proxyResolver = r;
    }
}
