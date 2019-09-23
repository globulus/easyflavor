package net.globulus.easyflavor;

interface ProxyResolver {
    void setResolver(FlavorResolver r);
    <T> T get(Class<? super T> flavorableClass);
}
