package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.processor.FlavorableInterface;
import net.globulus.easyflavor.processor.util.FrameworkUtil;

import java.io.Writer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.FileObject;

import javawriter.EzfWriter;

import static javax.tools.StandardLocation.SOURCE_OUTPUT;

public class KotlinExtCodeGen {

    public void generate(Filer filer, Input input) {
        try {
            String packageName = FrameworkUtil.PACKAGE_NAME;
            String className = "EasyFlavorKotlinExt.kt";

            Set<String> allFlavors = new HashSet<>();
            for (FlavorableInterface fi : input.fis) {
                allFlavors.addAll(fi.getAllFlavors());
            }

            FileObject fo = filer.createResource(SOURCE_OUTPUT, packageName, className);
            Writer writer = fo.openWriter();
            try (EzfWriter jw = new EzfWriter(writer)) {
                jw.emitPackageKt(packageName);

                for (String flavor : allFlavors) {
                    jw.beginFunctionKt("T?",
                            "run" + toPascalCase(flavor),
                            EnumSet.of(Modifier.PUBLIC),
                            Collections.singletonList("T"),
                            "block", "() -> T")
                            .beginControlFlow("if (EasyFlavor.getResolver().resolve() == \"%s\")", flavor)
                            .emitStatementKt("return block()")
                            .endControlFlow()
                            .emitStatementKt("return null")
                            .endMethod()
                            .emitEmptyLine();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String toPascalCase(String s) {
       return toProperCase(s);
    }

    private String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }
}
