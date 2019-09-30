package net.globulus.easyflavor.processor;

import net.globulus.easyflavor.annotation.EasyFlavorConfig;
import net.globulus.easyflavor.annotation.FlavorInject;
import net.globulus.easyflavor.annotation.Flavorable;
import net.globulus.easyflavor.annotation.Flavored;
import net.globulus.easyflavor.processor.codegen.EasyFlavorCodeGen;
import net.globulus.easyflavor.processor.codegen.FlavoredSubclassCodeGen;
import net.globulus.easyflavor.processor.codegen.Input;
import net.globulus.easyflavor.processor.codegen.KotlinExtCodeGen;
import net.globulus.easyflavor.processor.util.FrameworkUtil;
import net.globulus.easyflavor.processor.util.Pair;
import net.globulus.easyflavor.processor.util.ProcessorLog;
import net.globulus.mmap.MergeManager;
import net.globulus.mmap.MergeSession;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Types;

public class Processor extends AbstractProcessor {

	private static final String NAME = "EasyFlavor";
	private static final List<Class<? extends Annotation>> ANNOTATIONS = Arrays.asList(
			Flavorable.class,
			Flavored.class
	);

	private Types mTypeUtils;
	private Filer mFiler;

	private long mTimestamp;

	private List<String> mFlavorables = new ArrayList<>();
	private List<Element> mFlavoreds = new ArrayList<>();
	private List<FlavorableInterface> mFis = new ArrayList<>();
	private boolean mWroteOutput = false;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);

		ProcessorLog.init(env);

		mTypeUtils = env.getTypeUtils();
		mFiler = env.getFiler();

		mTimestamp = System.currentTimeMillis();
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
		if (mWroteOutput) {
			return true;
		}

		Boolean shouldMerge = null;
		Boolean foundSink = null;
		Boolean kotlinExt = null;
		for (Element element : roundEnv.getElementsAnnotatedWith(EasyFlavorConfig.class)) {
			EasyFlavorConfig annotation = element.getAnnotation(EasyFlavorConfig.class);
			if (annotation.source() && shouldMerge == null) {
				shouldMerge = false;
			}
			if (annotation.sink() && foundSink == null) {
				foundSink = true;
			}
			if (annotation.kotlinExt() && kotlinExt == null) {
				kotlinExt = true;
			}
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Flavorable.class)) {
			if (!isValidFlavorable(element)) {
				continue;
			}
			List<ExposedMethod> constructors;
 			List<FlavorInjectMethod> flavorInjectMethods;
			if (element.getKind() == ElementKind.CLASS) {
				TypeElement typeElement = (TypeElement) element;
				flavorInjectMethods = analyzeFlavorInjects(typeElement);
				constructors = analyzeConstructors(typeElement);
			} else {
				flavorInjectMethods = Collections.emptyList();
				constructors = Collections.emptyList();
			}
			String type = element.asType().toString();
			mFlavorables.add(type);
			mFis.add(new FlavorableInterface(
					new ExposedClass(type, constructors),
					element.getKind() == ElementKind.INTERFACE,
					flavorInjectMethods
			));// annotation.proxied()));
		}

		for (Element element : roundEnv.getElementsAnnotatedWith(Flavored.class)) {
			if (!isValidFlavoredClass(element)) {
				continue;
			}
			mFlavoreds.add(element);
		}

		final boolean shouldMergeResolution = (shouldMerge == null);
		Input input = new Input(mFlavorables, mFis);
//		ProcessorLog.warn(null, "should merge " + shouldMergeResolution);
		MergeManager<Input> mergeManager = new MergeManager<Input>(mFiler, mTimestamp,
				FrameworkUtil.PACKAGE_NAME, NAME,
				() -> shouldMergeResolution)
				.setProcessorLog(new net.globulus.mmap.ProcessorLog() {
					@Override
					public void note(Element element, String s, Object... objects) {
//						ProcessorLog.note(element, s, objects);
					}

					@Override
					public void warn(Element element, String s, Object... objects) {
//						ProcessorLog.warn(element, s, objects);
					}

					@Override
					public void error(Element element, String s, Object... objects) {
//						ProcessorLog.error(element, s, objects);
					}
				});
		MergeSession<Input> mergeSession = mergeManager.newSession();
//		ProcessorLog.warn(null, "BEFORE MERGE 1");
		input = mergeSession.mergeInput(input);
//		ProcessorLog.warn(null, "AFTER MERGE 1");

		for (Element element : mFlavoreds) {
			String flavorableClass = getFlavorableSupertype(element, input.flavorables);
			if (flavorableClass == null) {
				continue;
			}
//			ProcessorLog.warn(element, "Found flavorable class " + flavorableClass);
			FlavorableInterface fi = null;
			for (FlavorableInterface f : input.fis) {
//				ProcessorLog.warn(null, "Found mFis class " + f.flavorSubclassesMap);
				if (f.flavorableClass.name.equals(flavorableClass)) {
					fi = f;
					break;
				}
			}
			Flavored annotation = element.getAnnotation(Flavored.class);
			String[] flavors = annotation.flavors();
			if (flavors.length == 0) {
				ProcessorLog.error(element, "flavors array cannot be empty!");
				return false;
			}
			fi.addFlavorSubclass(
					new ExposedClass(element.asType().toString(), analyzeConstructors((TypeElement) element)),
					flavors);
		}

		if (foundSink != null) {
//			ProcessorLog.warn(null, "WRITING OUTPUT");
			FlavoredSubclassCodeGen flavoredSubclassCodeGen = new FlavoredSubclassCodeGen();
			for (FlavorableInterface fi : input.fis) {
				for (String flavor : fi.getSubclassFlavors()) {
					ExposedClass flavorSubclass = flavoredSubclassCodeGen.generate(mFiler, fi, flavor);
					fi.addFlavorSubclass(flavorSubclass, flavor);
				}
			}
			new EasyFlavorCodeGen().generate(mFiler, input);

			if (kotlinExt != null) {
				new KotlinExtCodeGen().generate(mFiler, input);
			}
			mWroteOutput = true;
		} else {
//			ProcessorLog.warn(null, "BEFORE MERGE 2");
			mergeSession.writeMergeFiles(input);
//			ProcessorLog.warn(null, "AFTER MERGE 2");
		}

		return true;
	}

	private boolean isValidFlavorable(Element element) {
		if (element.getKind() == ElementKind.INTERFACE || element.getKind() == ElementKind.CLASS) {
			if (element.getModifiers().contains(Modifier.PRIVATE)) {
				ProcessorLog.error(element, "The private type %s is annotated with @%s. "
								+ "Private types are not supported because of lacking visibility.",
						element.getSimpleName(), Flavorable.class.getSimpleName());
				return false;
			} else if (element.getModifiers().contains(Modifier.FINAL)) {
				ProcessorLog.error(element, "The final type %s is annotated with @%s. "
								+ "Final types are not supported because of lacking extensibility.",
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

	private List<FlavorInjectMethod> analyzeFlavorInjects(TypeElement element) {
		List<Pair<ExposedMethod, FlavorInject>> flavorInjects = new ArrayList<>();
		List<Pair<ExposedMethod, Flavored>> flavoreds = new ArrayList<>();
		for (Element enclosed : element.getEnclosedElements()) {
			FlavorInject flavorInject = enclosed.getAnnotation(FlavorInject.class);
			if (flavorInject != null && isValidFlavorInject(enclosed)) {
				flavorInjects.add(new Pair<>(new ExposedMethod(enclosed), flavorInject));
				continue;
			}
			Flavored flavored = enclosed.getAnnotation(Flavored.class);
			if (flavored != null && isValidFlavoredMethod(enclosed)) {
				flavoreds.add(new Pair<>(new ExposedMethod(enclosed), flavored));
			}
		}
		List<FlavorInjectMethod> flavorInjectMethods = new ArrayList<>();
		for (Pair<ExposedMethod, FlavorInject> flavorInject : flavorInjects) {
			Map<String, ExposedMethod> flavoredMethods = new HashMap<>();
			ExposedMethod original = flavorInject.first;
			for (Pair<ExposedMethod, Flavored> flavored : flavoreds) {
				ExposedMethod other = flavored.first;
				if (other.isEquivalentTo(original) && other.isNamedLike(original)) {
					for (String flavor : flavored.second.flavors()) {
						flavoredMethods.put(flavor, other);
					}
				}
			}
			flavorInjectMethods.add(new FlavorInjectMethod(flavorInject.second.mode(), original, flavoredMethods));
		}
		return flavorInjectMethods;
	}

	private List<ExposedMethod> analyzeConstructors(TypeElement element) {
		List<ExposedMethod> constructors = new ArrayList<>();
		for (Element enclosed : element.getEnclosedElements()) {
			if (enclosed.getKind() != ElementKind.CONSTRUCTOR
					|| !enclosed.getModifiers().contains(Modifier.PUBLIC)) {
				continue;
			}
			constructors.add(new ExposedMethod(enclosed, true));
		}
		return constructors;
	}

	private boolean isValidFlavorInject(Element element) {
		if (element.getKind() != ElementKind.CONSTRUCTOR && element.getKind() != ElementKind.METHOD) {
			ProcessorLog.error(element,
					"Element %s is annotated with @%s but is not a constructor or a method." +
							" Only constructors and methods are supported",
					element.getSimpleName(), FlavorInject.class.getSimpleName());
			return false;
		}
		if (element.getModifiers().contains(Modifier.STATIC) || element.getModifiers().contains(Modifier.FINAL)) {
			ProcessorLog.error(element,
					"Element %s is annotated with @%s but is final or static." +
							" All methods must be non-static and open.",
					element.getSimpleName(), FlavorInject.class.getSimpleName());
			return false;
		}
		return true;
	}

	private boolean isValidFlavoredMethod(Element element) {
		if (element.getKind() != ElementKind.METHOD) {
			ProcessorLog.error(element,
					"Element %s is annotated with @%s but is not a method." +
							" Only methods are supported",
					element.getSimpleName(), FlavorInject.class.getSimpleName());
			return false;
		}
		if (element.getModifiers().contains(Modifier.STATIC) || element.getModifiers().contains(Modifier.PRIVATE)) {
			ProcessorLog.error(element,
					"Element %s is annotated with @%s but is private or static." +
							" All methods must be non-static and non-private.",
					element.getSimpleName(), FlavorInject.class.getSimpleName());
			return false;
		}
		return true;
	}

	private boolean isValidFlavoredClass(Element element) {
		if (element.getKind() == ElementKind.CLASS) {
			if (element.getModifiers().contains(Modifier.PRIVATE)) {
				ProcessorLog.error(element, "The private class %s is annotated with @%s. "
								+ "Private classes are not supported because of lacking visibility.",
						element.getSimpleName(), Flavored.class.getSimpleName());
				return false;
			} else {
				return true;
			}
		}
		return false;
	}

	private String getFlavorableSupertype(Element element, List<String> flavorables) {
//		ProcessorLog.warn(element, "AAAA "  + flavorables.size());
		for (TypeMirror directSuperType : mTypeUtils.directSupertypes(element.asType())) {
			List<TypeMirror> allSuperTypes = new ArrayList<>(mTypeUtils.directSupertypes(directSuperType));
			allSuperTypes.add(directSuperType);
			for (TypeMirror superType : allSuperTypes) {
				for (String flavorable : flavorables) {
//				ProcessorLog.warn(element, "AAAA "  + flavorable);
					if (flavorable.equals(superType.toString())) {
						return flavorable;
					}
				}
			}
		}
		ProcessorLog.error(element,
				"Element %s does not extend the %s type.",
				element.getSimpleName(), Flavorable.class.getSimpleName(),
				Flavored.class.getSimpleName());
		return null;
	}
}
