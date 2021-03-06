package net.globulus.easyflavor.processor;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;

public class ExposedMethod implements Serializable {

    public final String name;
    public final String originalMethod;
    public final Set<Modifier> modifiers;
    public final String returnType;
    public final List<String> params;
    public final List<String> thrown;

    public ExposedMethod(Element element) {
        this(element, false);
    }

    public ExposedMethod(Element element, boolean boxParamTypes) {
        ExecutableType method = (ExecutableType) element.asType();
        TypeElement declaringClass = (TypeElement) element.getEnclosingElement();
        this.name = element.getSimpleName().toString();
        this.originalMethod = declaringClass.getQualifiedName().toString() + "." + element.getSimpleName();
        this.modifiers = element.getModifiers();
        this.returnType = method.getReturnType().toString();
        this.params = new ArrayList<>();
        int count = 0;
        for (TypeMirror param : method.getParameterTypes()) {
            String paramType = param.toString();
            if (boxParamTypes) {
                paramType = mapPrimitiveToBoxed(paramType);
            }
            this.params.add(paramType);
            String[] components = paramType.toLowerCase().split("\\.");
            String paramName = components[components.length - 1];
            if (paramName.endsWith(">")) {
                paramName = paramName.substring(0, paramName.length() - 1);
            }
            this.params.add(paramName + count);
            count++;
        }
        this.thrown = method.getThrownTypes().stream()
                .map((Function<TypeMirror, String>) TypeMirror::toString)
                .collect(Collectors.toList()
                );
    }

    public List<String> getParamTypes() {
        List<String> paramTypes = new ArrayList<>();
        for (int i = 0; i < params.size(); i += 2) {
            paramTypes.add(params.get(i));
        }
        return paramTypes;
    }

    public List<String> getParamNames() {
        List<String> paramTypes = new ArrayList<>();
        for (int i = 1; i < params.size(); i += 2) {
            paramTypes.add(params.get(i));
        }
        return paramTypes;
    }

    boolean isEquivalentTo(ExposedMethod other) {
        return returnType.equals(other.returnType)
                && params.equals(other.params)
                && thrown.equals(other.thrown);
    }

    boolean isNamedLike(ExposedMethod other) {
        String thisName = name.toLowerCase();
        String otherName = other.name.toLowerCase();
        return thisName.startsWith(otherName) || thisName.endsWith(otherName);
    }

    private String mapPrimitiveToBoxed(String type) {
        switch (type) {
            case "int": return "Integer";
            case "byte": return "Byte";
            case "short": return "Short";
            case "long": return "Long";
            case "float": return "Float";
            case "double": return "Double";
            case "boolean": return "Boolean";
            default: return type;
        }
    }
}