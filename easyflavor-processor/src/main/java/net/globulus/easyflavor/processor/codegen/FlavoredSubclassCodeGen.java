package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.annotation.FlavorInject;
import net.globulus.easyflavor.processor.ExposedClass;
import net.globulus.easyflavor.processor.ExposedMethod;
import net.globulus.easyflavor.processor.FlavorInjectMethod;
import net.globulus.easyflavor.processor.FlavorableInterface;

import java.io.Writer;
import java.util.EnumSet;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

import javawriter.EzfJavaWriter;

public class FlavoredSubclassCodeGen {

    public ExposedClass generate(Filer filer, FlavorableInterface fi, String flavor) {
        try {
            String superClass = fi.flavorableClass.name;
            int lastIndexOfDot = superClass.lastIndexOf('.');
            String packageName = superClass.substring(0, lastIndexOfDot);
            String className = superClass.substring(lastIndexOfDot + 1) + "_" + flavor;

            JavaFileObject jfo = filer.createSourceFile(packageName + "." + className);
            Writer writer = jfo.openWriter();
            try (EzfJavaWriter jw = new EzfJavaWriter(writer)) {
                jw.emitPackage(packageName);

//                jw.emitImports("java.lang.reflect.Proxy")
//                        .emitEmptyLine();

                String extendsType;
                String[] implementsType;
                if (fi.isInterface) {
                    extendsType = null;
                    implementsType = new String[] { superClass };
                } else {
                    extendsType = superClass;
                    implementsType = new String[0];
                }
                jw.emitJavadoc("Generated class by EasyFlavor. Do not modify this code!");
                jw.beginType(className, "class", EnumSet.of(Modifier.PUBLIC, Modifier.FINAL),
                        extendsType, implementsType)
                        .emitEmptyLine();

               for (FlavorInjectMethod method : fi.flavorInjectMethods) {
                   ExposedMethod flavorMethod = method.flavoredMethods.get(flavor);
                   if (flavorMethod == null) {
                       continue;
                   }
                   String originalName = method.originalMethod.name;
                   jw.emitAnnotation(Override.class)
                           .beginMethod(method.originalMethod.returnType,
                                   originalName,
                                   method.originalMethod.modifiers,
                                   method.originalMethod.params,
                                   method.originalMethod.thrown,
                                   null);
                   String invocationParams = getInvocationParams(method.originalMethod);
                   String statementStart = getStatementStart(method.originalMethod);
                   if (method.mode == FlavorInject.Mode.REPLACE) {
                       jw.emitStatement("%s%s(%s)", statementStart, flavorMethod.name, invocationParams);
                   } else if (method.mode == FlavorInject.Mode.BEFORE) {
                       jw.emitStatement("%s(%s)", flavorMethod.name, invocationParams)
                               .emitStatement("%ssuper.%s(%s)", statementStart, originalName, invocationParams);
                   } else if (method.mode == FlavorInject.Mode.AFTER) {
                       jw.emitStatement("super.%s(%s)", originalName, invocationParams)
                               .emitStatement("%s%s(%s)", statementStart, flavorMethod.name, invocationParams);
                   }
                   jw.endMethod();
               }

                jw.endType();
            }
            return new ExposedClass(packageName + '.' + className, fi.flavorableClass.constructors);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getInvocationParams(ExposedMethod method) {
        StringBuilder params = new StringBuilder();
        for (int i = 1; i < method.params.size(); i += 2) {
            if (i > 1) {
                params.append(", ");
            }
            params.append(method.params.get(i));
        }
        return params.toString();
    }

    private String getStatementStart(ExposedMethod method) {
        String start;
        if (method.returnType.equals("void")) {
            start = "";
        } else {
            start = "return ";
        }
        return start;
    }

}
