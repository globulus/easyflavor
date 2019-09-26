package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.processor.FlavorableInterface;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javawriter.EzfJavaWriter;

public class FlavorableInterfaceCodeGen implements CodeGen<FlavorableInterface> {

    @Override
    @SuppressWarnings("unchecked")
    public void generateCode(FlavorableInterface type, EzfJavaWriter jw) throws IOException {
        String interfaceType = type.flavorableClass;
        String implType = interfaceType + "Impl";
        jw.emitStatement("%s realInstance = (%s) Class.forName(\"%s\").newInstance()",
                interfaceType, interfaceType, implType);
        jw.emitStatement("%s flavorInstance = null", interfaceType);
        jw.beginControlFlow("switch (resolver.resolve())");
        for (Object o : type.getFlavorEntries()) {
            Map.Entry<String, List<String>> entry = (Map.Entry<String, List<String>>) o;
            for (String flavor : entry.getValue()) {
                jw.emitCase("\"" + flavor + "\"");
            }
            jw.emitStatement("flavorInstance = (%s) Class.forName(\"%s\").newInstance()",
                    interfaceType, entry.getKey());
            jw.emitStatement("break");
        }
        jw.endControlFlow();
        jw.emitStatement("return (T) EasyFlavorProxyFactory.createProxy(%s.class, realInstance, flavorInstance)", interfaceType);
    }
}
