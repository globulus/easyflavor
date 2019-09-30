package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.processor.FlavorableInterface;
import net.globulus.easyflavor.processor.util.FrameworkUtil;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import javawriter.EzfJavaWriter;

public class EasyFlavorCodeGen {

    public void generate(Filer filer, Input input) {
        try {
            String packageName = FrameworkUtil.PACKAGE_NAME;
            String className = "EasyFlavorProxyResolver";

            JavaFileObject jfo = filer.createSourceFile(packageName + "." + className);
            Writer writer = jfo.openWriter();
            try (EzfJavaWriter jw = new EzfJavaWriter(writer)) {
                jw.emitPackage(packageName);

//                jw.emitImports("java.lang.reflect.Proxy")
//                        .emitEmptyLine();

                jw.emitJavadoc("Generated class by @%s. Do not modify this code!", className);
                jw.beginType(className, "class", EnumSet.of(Modifier.FINAL),
                        null, "ProxyResolver")
                        .emitEmptyLine();

                jw.emitField("FlavorResolver", "resolver", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC))
                        .emitEmptyLine();

                jw.beginStaticBlock()
                        .emitStatement("EasyFlavor.setProxyResolver(new %s())", className)
                        .endStaticBlock()
                        .emitEmptyLine();

                jw.beginConstructor(EnumSet.of(Modifier.PRIVATE))
                        .endConstructor()
                        .emitEmptyLine();

                jw.emitAnnotation(Override.class)
                        .beginMethod("void", "setResolver", EnumSet.of(Modifier.PUBLIC),
                        "FlavorResolver", "r")
                        .emitStatement("resolver = r")
                        .endMethod()
                        .emitEmptyLine();

                jw.emitAnnotation(SuppressWarnings.class, "\"unchecked\"")
                        .emitAnnotation(Override.class)
                        .beginMethod("T", "get", EnumSet.of(Modifier.PUBLIC),
                                Arrays.asList("Class<? super T>", "flavorableClass", "Object...", "args"),
                                null, Collections.singletonList("T"));

//                jw.beginControlFlow("try");

                String classEqualsFormat = "%s.class.equals(flavorableClass)";
                FlavorableInterfaceCodeGen fiCodeGen = new FlavorableInterfaceCodeGen();
                for (FlavorableInterface fi : input.fis) {
                    String classEquals = String.format(classEqualsFormat, fi.flavorableClass.name);
                    jw.beginControlFlow("if (%s)", classEquals);
                    fiCodeGen.generateCode(fi, jw);
                    jw.endControlFlow();
                }

//                jw.nextControlFlow("catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)")
//                        .emitStatement("e.printStackTrace()")
//                        .endControlFlow();

                jw.emitStatement("throw new IllegalArgumentException(\"Unable to find flavors for class: \" + flavorableClass)")
                        .endMethod();

                jw.endType();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
