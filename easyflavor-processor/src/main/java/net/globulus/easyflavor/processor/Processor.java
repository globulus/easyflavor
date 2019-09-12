package net.globulus.easyflavor.processor;

import net.globulus.easyflavor.annotation.Flavorable;
import net.globulus.easyflavor.annotation.Flavored;
import net.globulus.easyflavor.processor.codegen.EasyFlavorCodeGen;
import net.globulus.easyflavor.processor.util.ProcessorLog;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

public class Processor extends AbstractProcessor {

	private static final List<Class<? extends Annotation>> ANNOTATIONS = Arrays.asList(
			Flavorable.class,
			Flavored.class
	);

	private Elements mElementUtils;
	private Types mTypeUtils;
	private Filer mFiler;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		ProcessorLog.init(env);

		mElementUtils = env.getElementUtils();
		mTypeUtils = env.getTypeUtils();
		mFiler = env.getFiler();
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		Set<String> types = new LinkedHashSet<>();
		for (Class<? extends Annotation> annotation : ANNOTATIONS) {
			types.add(annotation.getCanonicalName());
		}
		return types;
	}

	@Override
	@SuppressWarnings("unchecked")
	public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
		List<TypeMirror> flavorables = new ArrayList<>();
		List<FlavorableInterface> fis = new ArrayList<>();

		ProcessorLog.note(null, "AAAAAA");

		for (Element element : roundEnv.getElementsAnnotatedWith(Flavorable.class)) {
			if (!isValidFlavorable(element)) {
				continue;
			}
			TypeMirror type = element.asType();
			flavorables.add(type);
			fis.add(new FlavorableInterface(type.toString()));
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Flavored.class)) {
			TypeMirror flavorableType = isValidFlavored(element, flavorables);
			if (flavorableType == null) {
				continue;
			}
			String flavorableClass = flavorableType.toString();
			FlavorableInterface fi = null;
			for (FlavorableInterface f : fis) {
				if (f.flavorableClass.equals(flavorableClass)) {
					fi = f;
					break;
				}
			}
			String[] flavors = element.getAnnotation(Flavored.class).flavors();
			if (flavors.length == 0) {
				ProcessorLog.error(element, "flavors array cannot be empty!");
				return false;
			}
			fi.addFlavors(element.asType().toString(), flavors);
		}
		new EasyFlavorCodeGen().generate(mFiler, fis);
		return true;
	}

	private boolean isValidFlavorable(Element element) {
		if (element.getKind() == ElementKind.INTERFACE || element.getKind() == ElementKind.CLASS) {
			if (element.getModifiers().contains(Modifier.PRIVATE)) {
				ProcessorLog.error(element, "The private type %s is annotated with @%s. "
								+ "Private types are not supported because of lacking visibility.",
						element.getSimpleName(), Flavorable.class.getSimpleName());
				return false;
			} else {
				return true;
			}
		} else {
			ProcessorLog.error(element,
					"Element %s is annotated with @%s but is not a class or an interface." +
							" Only classes and interfaces are supported",
					element.getSimpleName(), Flavorable.class.getSimpleName());
			return false;
		}
	}

	private TypeMirror isValidFlavored(Element element, List<TypeMirror> flavorables) {
		if (element.getKind() == ElementKind.CLASS) {
			if (element.getModifiers().contains(Modifier.PRIVATE)) {
				ProcessorLog.error(element, "The private class %s is annotated with @%s. "
								+ "Private classes are not supported because of lacking visibility.",
						element.getSimpleName(), Flavored.class.getSimpleName());
				return null;
			} else {
				for (TypeMirror superType : mTypeUtils.directSupertypes(element.asType())) {
					for (TypeMirror flavorable : flavorables) {
						if (flavorable.equals(superType)) {
							return flavorable;
						}
					}
				}
				ProcessorLog.error(element,
						"Element %s does not extend a %s type.",
						element.getSimpleName(), Flavorable.class.getSimpleName(),
						Flavored.class.getSimpleName());
				return null;
			}
		} else {
			ProcessorLog.error(element,
					"Element %s is annotated with @%s but is not a class. Only classes are supported.",
					element.getSimpleName(), Flavored.class.getSimpleName());
			return null;
		}
	}

	private Modifier mapToJavax(Element element, int modifier) {
		switch (modifier) {
			case java.lang.reflect.Modifier.ABSTRACT:
				return Modifier.ABSTRACT;
			case java.lang.reflect.Modifier.PUBLIC:
				return Modifier.PUBLIC;
			case java.lang.reflect.Modifier.PRIVATE:
				return Modifier.PRIVATE;
			case java.lang.reflect.Modifier.PROTECTED:
				return Modifier.PROTECTED;
			case java.lang.reflect.Modifier.FINAL:
				return Modifier.FINAL;
			case java.lang.reflect.Modifier.STATIC:
				return Modifier.STATIC;
			case java.lang.reflect.Modifier.TRANSIENT:
				return Modifier.TRANSIENT;
			case java.lang.reflect.Modifier.VOLATILE:
				return Modifier.VOLATILE;
			case java.lang.reflect.Modifier.SYNCHRONIZED:
				return Modifier.SYNCHRONIZED;
			case java.lang.reflect.Modifier.STRICT:
				return Modifier.STRICTFP;
			case java.lang.reflect.Modifier.NATIVE:
				return Modifier.NATIVE;
			default:
				ProcessorLog.error(element, "Wrong modifier used: " + modifier);
				return null;
		}
	}
}
