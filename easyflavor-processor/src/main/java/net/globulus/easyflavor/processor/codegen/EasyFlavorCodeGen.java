package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.processor.FlavorableInterface;
import net.globulus.easyflavor.processor.util.FrameworkUtil;

import java.io.Writer;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import javawriter.EzfJavaWriter;

public class EasyFlavorCodeGen {

    public void generate(Filer filer, List<FlavorableInterface> input) {
        try {
            String packageName = FrameworkUtil.PACKAGE_NAME;
            String className = "EasyFlavor";

            JavaFileObject jfo = filer.createSourceFile(packageName + "." + className);
            Writer writer = jfo.openWriter();
            try (EzfJavaWriter jw = new EzfJavaWriter(writer)) {
                jw.emitPackage(packageName);

                jw.emitImports("android.content.Context")
                        .emitImports("java.lang.reflect.Proxy")
                        .emitEmptyLine();

                jw.emitJavadoc("Generated class by @%s. Do not modify this code!", className);
                jw.beginType(className, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL), null)
                        .emitEmptyLine();

                jw.emitField("FlavorResolver", "resolver", EnumSet.of(Modifier.PRIVATE, Modifier.STATIC))
                        .emitEmptyLine();

                jw.beginConstructor(EnumSet.of(Modifier.PRIVATE))
                        .endConstructor()
                        .emitEmptyLine();

                EnumSet<Modifier> methodModifiers = EnumSet.of(Modifier.PUBLIC, Modifier.STATIC);
                jw.beginMethod("void", "setResolver", methodModifiers, "FlavorResolver", "r")
                        .emitStatement("resolver = r")
                        .endMethod()
                        .emitEmptyLine();

                jw.emitAnnotation(SuppressWarnings.class, "\"unchecked\"")
                        .beginMethod("T", "get", methodModifiers,
                                Arrays.asList("Context", "context", "Class<? super T>", "flavorableClass"), null,
                                Collections.singletonList("T"));

                jw.beginControlFlow("try");

                boolean first = true;
                String classEqualsFormat = "%s.class.equals(flavorableClass)";
                FlavorableInterfaceCodeGen fiCodeGen = new FlavorableInterfaceCodeGen();
                for (FlavorableInterface fi : input) {
                    String classEquals = String.format(classEqualsFormat, fi.flavorableClass);
                    if (first) {
                        first = false;
                        jw.beginControlFlow("if (%s)", classEquals);
                    } else {
                        jw.nextControlFlow("else if (%s)", classEquals);
                    }
                    fiCodeGen.generateCode(fi, jw);
                    jw.endControlFlow();
                }

                jw.nextControlFlow("catch (ClassNotFoundException | IllegalAccessException | InstantiationException e)")
                        .emitStatement("e.printStackTrace()")
                        .endControlFlow();

                jw.emitStatement("throw new IllegalArgumentException(\"Unable to find flavors for class: \" + flavorableClass)")
                        .endMethod();

                jw.endType();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
