package net.globulus.easyflavor;

interface ProxyResolver {
    void setResolver(FlavorResolver r);
    FlavorResolver getResolver();
    <T> T get(Class<? super T> flavorableClass, Object... args);
}
