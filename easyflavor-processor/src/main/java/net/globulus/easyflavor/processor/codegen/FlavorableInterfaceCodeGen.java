package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.processor.ExposedClass;
import net.globulus.easyflavor.processor.FlavorableInterface;

import java.io.IOException;
import java.util.Map;

import javawriter.EzfJavaWriter;

public class FlavorableInterfaceCodeGen implements CodeGen<FlavorableInterface> {

    @Override
    @SuppressWarnings("unchecked")
    public void generateCode(FlavorableInterface type, EzfJavaWriter jw) throws IOException {
        String interfaceType = type.flavorableClass.name;
        jw.emitStatement("%s flavorInstance = null", interfaceType);
        jw.beginControlFlow("switch (resolver.resolve())");
        for (Object o : type.getFlavorSubclasses()) {
            Map.Entry<String, ExposedClass> entry = (Map.Entry<String, ExposedClass>) o;
            jw.emitCase("\"" + entry.getKey() + "\"")
                    .emitStatement("flavorInstance = new %s()", entry.getValue().name)
                    .emitStatement("break");
        }
        jw.endControlFlow();

//        if (type.proxied) {
//            String implType = interfaceType + "Impl";
//            jw.emitStatement("%s realInstance = (%s) Class.forName(\"%s\").newInstance()",
//                    interfaceType, interfaceType, implType);
//            jw.emitStatement("return (T) Proxy.newProxyInstance(\n" +
//                            "                        EasyFlavor.class.getClassLoader(),\n" +
//                            "                        new Class[] { %s.class },\n" +
//                            "                        new FlavorInjectInvocationHandler<>(realInstance, flavorInstance)\n" +
//                            ")",
//                    interfaceType);
//        } else {
            jw.emitStatement("return (T) flavorInstance");
//        }
    }
}
