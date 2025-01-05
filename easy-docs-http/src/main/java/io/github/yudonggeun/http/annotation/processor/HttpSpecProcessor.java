package io.github.yudonggeun.http.annotation.processor;

import com.squareup.javapoet.*;
import io.github.yudonggeun.http.annotation.*;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({
        "io.github.yudonggeun.http.annotation.RequestSpec",
        "io.github.yudonggeun.http.annotation.ResponseSpec",
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HttpSpecProcessor extends AbstractProcessor {

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RequestSpec.class)) {
            createFormClass(element, HttpRequestInput.class);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(ResponseSpec.class)) {
            createFormClass(element, HttpResponseInput.class);
        }
        return true;
    }

    private void createFormClass(Element element, Type superType) {
        String pacakageName = getPackageName(element);
        String className = element.getSimpleName().toString();
        List<? extends Element> enclosedElements = element.getEnclosedElements();

        TypeSpec innerClassSpec = null;
        List<FieldSpec> fieldSpecs = new ArrayList<>();
        List<MethodSpec> methodSpecs = new ArrayList<>();

        // static factory method
        String requestFormClassName = getNewClassName(className);
        MethodSpec factoryMethod = createFactoryMethod(requestFormClassName);
        methodSpecs.add(factoryMethod);

        for (Element enclosedElement : enclosedElements) {

            // field and method chain
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                FieldSpec fieldSpec = createField(enclosedElement);
                fieldSpecs.add(fieldSpec);

                MethodSpec methodSpec = createSetter(enclosedElement, requestFormClassName);
                methodSpecs.add(methodSpec);
            }

            // body
            if (enclosedElement.getAnnotation(BodySpec.class) != null) {
                // object
                if (enclosedElement.getKind() == ElementKind.CLASS) {
                    innerClassSpec = createInnerClassSpec(enclosedElement);

                    ClassName innerClassName = ClassName.bestGuess(getNewClassName(enclosedElement.getSimpleName().toString()));
                    FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(innerClassName, "body", Modifier.PRIVATE);
                    FieldSpec fieldSpec = fieldSpecBuilder.build();
                    fieldSpecs.add(fieldSpec);

                    MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("body")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(innerClassName, "body")
                            .returns(ClassName.bestGuess(requestFormClassName))
                            .addStatement("this.body = body")
                            .addStatement("return this");

                    if (enclosedElement.asType().getKind() == TypeKind.ARRAY) {
                        methodSpec.varargs(true);
                    }
                    methodSpecs.add(methodSpec.build());
                }
            }
        }

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(requestFormClassName)
                .addSuperinterface(superType)
                .addModifiers(Modifier.PUBLIC)
                .addFields(fieldSpecs)
                .addMethods(methodSpecs);

        if (innerClassSpec != null) {
            classBuilder.addType(innerClassSpec);
        }

        JavaFile javaFile = JavaFile.builder(pacakageName, classBuilder.build())
                .build();

        try {
            javaFile.writeTo(processingEnv.getFiler());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private static MethodSpec createFactoryMethod(String requestFormClassName) {
        return MethodSpec.methodBuilder("input")
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC)
                .returns(ClassName.bestGuess(requestFormClassName))
                .addStatement(String.format("return new %s()", requestFormClassName))
                .build();
    }

    private static FieldSpec createField(Element enclosedElement) {
        TypeName typeName = TypeName.get(enclosedElement.asType());
        String fieldName = enclosedElement.getSimpleName().toString();

        FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(typeName, fieldName, Modifier.PRIVATE);
        List<? extends AnnotationMirror> annotationMirrors = enclosedElement.getAnnotationMirrors();
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            AnnotationSpec annotationSpec = AnnotationSpec.get(annotationMirror);
            fieldSpecBuilder.addAnnotation(annotationSpec);
        }
        return fieldSpecBuilder.build();
    }

    private MethodSpec createSetter(Element fieldElement, String returnType) {
        TypeName typeName = TypeName.get(fieldElement.asType());
        String fieldName = fieldElement.getSimpleName().toString();
        MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(fieldName)
                .addModifiers(Modifier.PUBLIC)
                .addParameter(typeName, fieldName)
                .returns(ClassName.bestGuess(returnType))
                .addStatement(String.format("this.%s = %s", fieldName, fieldName))
                .addStatement("return this");

        if (fieldElement.asType().getKind() == TypeKind.ARRAY) {
            methodSpec.varargs(true);
        }
        return methodSpec.build();
    }

    private TypeSpec createInnerClassSpec(Element element) {
        String className = getNewClassName(element.getSimpleName().toString());

        TypeSpec.Builder innerClassBuilder = TypeSpec.classBuilder(className)
                .addModifiers(Modifier.STATIC, Modifier.PUBLIC);

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            AnnotationSpec annotationSpec = AnnotationSpec.get(annotationMirror);
            innerClassBuilder.addAnnotation(annotationSpec);
        }

        MethodSpec factoryMethod = createFactoryMethod(className);
        innerClassBuilder.addMethod(factoryMethod);

        List<? extends Element> enclosedElements = element.getEnclosedElements();
        for (Element enclosedElement : enclosedElements) {
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                FieldSpec fieldSpec = createField(enclosedElement);
                MethodSpec methodSpec = createSetter(enclosedElement, className);
                innerClassBuilder.addField(fieldSpec);
                innerClassBuilder.addMethod(methodSpec);
            } else if (enclosedElement.getKind() == ElementKind.CLASS) {
                TypeSpec innerClassSpec = createInnerClassSpec(enclosedElement);
                innerClassBuilder.addType(innerClassSpec);
            }
        }
        return innerClassBuilder.build();
    }

    private String getNewClassName(String className) {
        return className + "Form";
    }

    private String getPackageName(Element element) {
        while (!element.getKind().equals(ElementKind.PACKAGE)) {
            element = element.getEnclosingElement();
        }
        return ((PackageElement) element).getQualifiedName().toString();
    }
}
