package net.globulus.easyflavor.processor.codegen;

import net.globulus.easyflavor.processor.FlavorableInterface;
import net.globulus.easyflavor.processor.util.FrameworkUtil;

import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.tools.FileObject;

import javawriter.EzfWriter;

import static javax.tools.StandardLocation.SOURCE_OUTPUT;

public class KotlinExtCodeGen {

    private static final String RETURN_TYPE = "T?";
    private static final Set<Modifier> PUBLIC_MODIFIER = EnumSet.of(Modifier.PUBLIC);
    private static final List<String> GENERIC_TYPES = Collections.singletonList("T");
    private static final String BLOCK_NAME = "block";
    private static final String BLOCK_TYPE = "() -> T";
    private static final String RESOLVE = "EasyFlavor.getResolver().resolve()";
    private static final String RETURN_BLOCK = "return block()";
    private static final String RETURN_NULL = "return null";

    private final String module;

    public KotlinExtCodeGen(String module) {
        this.module = module;
    }

    public void generate(Filer filer, Input input) {
        try {
            String packageName = FrameworkUtil.PACKAGE_NAME;
            String className = "EasyFlavorKotlinExt_" + module + ".kt";

            Set<String> allFlavors = new HashSet<>();
            for (FlavorableInterface fi : input.fis) {
                allFlavors.addAll(fi.getAllFlavors());
            }

            FileObject fo = filer.createResource(SOURCE_OUTPUT, packageName, className);
            Writer writer = fo.openWriter();
            try (EzfWriter jw = new EzfWriter(writer)) {
                jw.emitPackageKt(packageName);

                emitIfUnless(false, jw);
                emitIfUnless(true, jw);

                for (String flavor : allFlavors) {
                    emitFlavorIfUnless(flavor, false, jw);
                    emitFlavorIfUnless(flavor, true, jw);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void emitIfUnless(boolean unless, EzfWriter jw) throws IOException {
        String name = "run" + ((unless) ? "Unless" : "If");
        jw.beginFunctionKt(RETURN_TYPE, name, PUBLIC_MODIFIER, GENERIC_TYPES,
                "flavors", "Array<String>", BLOCK_NAME, BLOCK_TYPE)
                .beginControlFlow("if (%sflavors.contains(%s))", (unless) ? "!" : "", RESOLVE);
        emitBodyEnd(jw);
    }

    private void emitFlavorIfUnless(String flavor,
                                    boolean unless,
                                    EzfWriter jw) throws IOException {
        jw.beginFunctionKt(RETURN_TYPE,
                "run" + ((unless) ? "Unless" : "If") + toPascalCase(flavor),
                PUBLIC_MODIFIER,
                GENERIC_TYPES,
                BLOCK_NAME, BLOCK_TYPE)
                .beginControlFlow("if (%s %s= \"%s\")", RESOLVE, (unless) ? "!" : "=", flavor);
        emitBodyEnd(jw);
    }

    private void emitBodyEnd(EzfWriter jw) throws IOException {
        jw.emitStatementKt(RETURN_BLOCK)
                .endControlFlow()
                .emitStatementKt(RETURN_NULL)
                .endMethod()
                .emitEmptyLine();
    }

    private String toPascalCase(String s) {
       return toProperCase(s);
    }

    private String toProperCase(String s) {
        return s.substring(0, 1).toUpperCase() +
                s.substring(1).toLowerCase();
    }
}
