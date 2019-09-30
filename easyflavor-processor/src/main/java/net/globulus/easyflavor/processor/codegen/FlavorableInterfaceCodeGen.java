package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.processor.ExposedClass;
import net.globulus.easyflavor.processor.ExposedMethod;
import net.globulus.easyflavor.processor.FlavorableInterface;

import java.io.IOException;
import java.util.Map;

import javawriter.EzfWriter;

public class FlavorableInterfaceCodeGen implements CodeGen<FlavorableInterface> {

    @Override
    @SuppressWarnings("unchecked")
    public void generateCode(FlavorableInterface type, EzfWriter jw) throws IOException {
        String interfaceType = type.flavorableClass.name;
        jw.emitStatement("%s flavorInstance = null", interfaceType);
        jw.beginControlFlow("switch (resolver.resolve())");
        for (Object o : type.getFlavorSubclasses()) {
            Map.Entry<String, ExposedClass> entry = (Map.Entry<String, ExposedClass>) o;
            jw.emitCase("\"" + entry.getKey() + "\"");
            writeConstructors(entry.getValue(), jw);
            jw.emitStatement("break");
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

    private void writeConstructors(ExposedClass exposedClass, EzfWriter jw) throws IOException {
        String name = exposedClass.name;
        boolean first = true;
        ExposedMethod emptyConstr = null;
        // Find empty constructor first
        for (ExposedMethod constructor : exposedClass.constructors) {
            if (constructor.params.isEmpty()) {
                jw.beginControlFlow("if (args == null || args.length == 0)");
                jw.emitStatement("flavorInstance = new %s()", name);
                first = false;
                emptyConstr = constructor;
                break;
            }
        }
        for (ExposedMethod constructor : exposedClass.constructors) {
            if (constructor == emptyConstr) {
                continue; // Already wrote this one
            }
            StringBuilder checks = new StringBuilder();
            StringBuilder casts = new StringBuilder();
            int count = 0;
            for (String paramType : constructor.getParamTypes()) {
                if (count > 0) {
                    checks.append(" && ");
                    casts.append(", ");
                }
                checks.append("args[").append(count).append("] instanceof ").append(paramType);
                casts.append("(").append(paramType).append(") args[").append(count).append("]");
                count++;
            }
            if (first) {
                jw.beginControlFlow("if (%s)", checks.toString());
                first = false;
            } else {
                jw.nextControlFlow("else if (%s)", checks.toString());
            }
            jw.emitStatement("flavorInstance = new %s(%s)", name, casts.toString());
        }
        jw.nextControlFlow("else")
                .emitStatement("throw new IllegalArgumentException(\"Cannot find constructor" +
                        " with provided args: \" + args.toString())")
                .endControlFlow();
    }
}
