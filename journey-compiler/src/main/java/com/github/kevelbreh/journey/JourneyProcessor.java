package com.github.kevelbreh.journey;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableSet;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import static javax.tools.Diagnostic.Kind.ERROR;

@AutoService(Processor.class) public final class JourneyProcessor extends AbstractProcessor {

  // Upper and lower characters, digits, underscores, and hyphens, starting with a character or digit.
  // Thanks OkHttp
  private static final String PARAM = "[a-zA-Z0-9][a-zA-Z0-9_-]*";
  private static final String PARAM_NON_CAPTURE = String.format("((?:%s))", PARAM);
  private static final Pattern PARAM_URL_REGEX = Pattern.compile("\\{(" + PARAM + ")\\}");

  private final ClassName ANDROID_BUNDLE = ClassName.get("android.os", "Bundle");
  private final ClassName CONDUCTOR_CONTROLLER =
      ClassName.get("com.bluelinelabs.conductor", "Controller");
  private final ClassName JOURNEY_ROUTES_PROVIDER =
      ClassName.get("com.github.kevelbreh.journey", "RoutesProvider");
  private final ClassName JOURNEY_CONTROLLER_ROUTE =
      ClassName.get("com.github.kevelbreh.journey", "ControllerRoute");
  private final ClassName JOURNEY_CONTROLLER_CREATOR =
      ClassName.get("com.github.kevelbreh.journey", "ControllerCreator");

  private String journeyPackage = "com.github.kevelbreh.journey";

  private Types typeUtils;
  private Elements elementUtils;
  private Messager messager;

  @Override public Set<String> getSupportedAnnotationTypes() {
    return ImmutableSet.of(Route.class.getName());
  }

  @Override public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);
    messager = processingEnv.getMessager();
    typeUtils = processingEnv.getTypeUtils();
    elementUtils = processingEnv.getElementUtils();
  }

  @Override
  public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {
    List<Element> elements = new LinkedList<>();
    for (Element e : roundEnvironment.getElementsAnnotatedWith(Route.class)) {
      if (extendsController(e)) {
        elements.add(e);
      }
    }

    if (elements.isEmpty()) {
      return false;
    }

    Map<String, List<Element>> providerNames = new HashMap<>();
    for (Element provider : elements) {
      final String providerName = capitalize(provider.getAnnotation(Route.class).provider());
      final List<Element> providerElements = new ArrayList<>();

      if (providerNames.keySet().contains(providerName)) {
        continue;
      }

      for (Element element : elements) {
        if (element.getAnnotation(Route.class).provider().equalsIgnoreCase(providerName)) {
          providerElements.add(element);
        }
      }

      providerNames.put(providerName, providerElements);
    }

    for (String providerName : providerNames.keySet()) {
      TypeSpec routerProvider = createRouteProvider(providerNames.get(providerName), providerName);
      JavaFile file = JavaFile.builder(journeyPackage, routerProvider).build();
      try {
        file.writeTo(processingEnv.getFiler());
      } catch (IOException e) {
        processingEnv.getMessager()
            .printMessage(ERROR, "Failed to write JourneyProvider: " + e.getLocalizedMessage());
      }
    }

    return false;
  }

  private TypeSpec createRouteProvider(List<Element> elements, String providerName) {
    ClassName list = ClassName.get("java.util", "List");
    ClassName arrayList = ClassName.get("java.util", "ArrayList");
    TypeName listOfRoutes = ParameterizedTypeName.get(list, JOURNEY_CONTROLLER_ROUTE);

    TypeSpec.Builder provider = TypeSpec.classBuilder(capitalize(providerName) + "JourneyProvider");
    provider.addModifiers(Modifier.PUBLIC, Modifier.FINAL);
    provider.addSuperinterface(JOURNEY_ROUTES_PROVIDER);

    MethodSpec.Builder provideRoutesMethod = MethodSpec.methodBuilder("provideRoutes")
        .addAnnotation(Override.class)
        .addModifiers(Modifier.PUBLIC)
        .returns(listOfRoutes)
        .addStatement("$T result = new $T<>()", listOfRoutes, arrayList);
    for (Element e : elements) {
      TypeSpec creator = generateCreator(e);
      String path = e.getAnnotation(Route.class).value();
      Pair<String, Set<String>> pair = pathToPatternAndParams(path);

      CodeBlock params = generateRouteParams(pair.second);

      provideRoutesMethod.addComment("Route defined as $S", path);
      provideRoutesMethod.addStatement("result.add(new $T($S, $L, $L))", JOURNEY_CONTROLLER_ROUTE,
          pair.first, params, creator);
    }
    provideRoutesMethod.addStatement("return result");

    return provider.addMethod(provideRoutesMethod.build()).build();
  }

  private CodeBlock generateRouteParams(Set<String> params) {
    if (params.isEmpty()) {
      return CodeBlock.builder().add("new $T[0]", String.class).build();
    }

    String[] arr = new String[params.size()];
    arr = params.toArray(arr);

    CodeBlock.Builder builder = CodeBlock.builder().add("new $T[]", String.class).add(" {");
    for (int i = 0; i < arr.length; i++) {
      builder.add("$S", arr[i]);
      if (i < params.size() - 1) {
        builder.add(", ");
      }
    }
    return builder.add("}").build();
  }

  private TypeSpec generateCreator(Element type) {
    return TypeSpec.anonymousClassBuilder("")
        .addSuperinterface(JOURNEY_CONTROLLER_CREATOR)
        .addMethod(MethodSpec.methodBuilder("create")
            .addModifiers(Modifier.PUBLIC)
            .addParameter(ANDROID_BUNDLE, "args")
            .returns(CONDUCTOR_CONTROLLER)
            .addStatement("return new $T($N)", type.asType(), "args")
            .build())
        .build();
  }

  private Pair<String, Set<String>> pathToPatternAndParams(String path) {
    Set<String> params = new LinkedHashSet<>();
    Matcher m = PARAM_URL_REGEX.matcher(path);
    while (m.find()) {
      String paramName = m.group().substring(1, m.group().length() - 1);
      if (!params.add(paramName)) {
        throw new RuntimeException(
            String.format("Path parameter %s has already been added.", paramName));
      }
      path = path.replace(m.group(), PARAM_NON_CAPTURE);
    }
    return Pair.of(path, params);
  }

  private String capitalize(final String line) {
    return Character.toUpperCase(line.charAt(0)) + line.substring(1);
  }

  private boolean extendsController(Element element) {
    TypeMirror target = elementUtils.getTypeElement(CONDUCTOR_CONTROLLER.reflectionName()).asType();
    TypeMirror mirror = element.asType();
    while (mirror.getKind() != TypeKind.NONE) {
      if (target == mirror) {
        return true;
      }
      mirror = ((TypeElement) typeUtils.asElement(mirror)).getSuperclass();
    }
    return false;
  }

  /**
   * Returns the name of the package that the given type is in. If the type is in the default
   * (unnamed) package then the name is the empty string.
   */
  private static String packageNameOf(TypeElement type) {
    while (true) {
      Element enclosing = type.getEnclosingElement();
      if (enclosing instanceof PackageElement) {
        return ((PackageElement) enclosing).getQualifiedName().toString();
      }
      type = (TypeElement) enclosing;
    }
  }
}
