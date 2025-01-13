package io.github.yudonggeun.processor;

import com.squareup.javapoet.*;
import io.github.yudonggeun.form.HttpRequestFormData;
import io.github.yudonggeun.form.HttpResponseFormData;
import io.github.yudonggeun.schema.ArraySchema;
import io.github.yudonggeun.schema.Schema;
import io.github.yudonggeun.spec.BodySpec;
import io.github.yudonggeun.spec.RequestSpec;
import io.github.yudonggeun.spec.ResponseSpec;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeKind;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@SupportedAnnotationTypes({
        "io.github.yudonggeun.spec.RequestSpec",
        "io.github.yudonggeun.spec.ResponseSpec",
})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class HttpSpecProcessor extends AbstractProcessor {

    private Set<String> processedClasses = new HashSet<>();

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        for (Element element : roundEnv.getElementsAnnotatedWith(RequestSpec.class)) {
            createFormClass(element, HttpRequestFormData.class);
        }
        for (Element element : roundEnv.getElementsAnnotatedWith(ResponseSpec.class)) {
            createFormClass(element, HttpResponseFormData.class);
        }
        return true;
    }

    private void createFormClass(Element element, Type superType) {

        if (processedClasses.contains(element.getSimpleName().toString())) return;

        String pacakageName = getPackageName(element);
        String className = element.getSimpleName().toString();
        String requestFormClassName = getNewClassName(className);
        List<? extends Element> enclosedElements = element.getEnclosedElements();

        TypeSpec.Builder classBuilder = TypeSpec.classBuilder(requestFormClassName)
                .addSuperinterface(superType)
                .addModifiers(Modifier.PUBLIC);

        // static factory method
        MethodSpec factoryMethod = createFactoryMethod(requestFormClassName);
        classBuilder.addMethod(factoryMethod);

        for (Element enclosedElement : enclosedElements) {

            // field and method chain
            if (enclosedElement.getKind() == ElementKind.FIELD) {
                FieldSpec fieldSpec = createField(enclosedElement);
                classBuilder.addField(fieldSpec);

                MethodSpec methodSpec = createSetter(enclosedElement, requestFormClassName);
                classBuilder.addMethod(methodSpec);
            }

            // body
            if (enclosedElement.getAnnotation(Schema.class) != null || enclosedElement.getAnnotation(ArraySchema.class) != null) {
                // object
                if (enclosedElement.getKind() == ElementKind.CLASS) {
                    classBuilder.addType(createInnerClassSpec(enclosedElement));

                    boolean isArray = enclosedElement.getAnnotation(ArraySchema.class) != null;
                    TypeName innerClassName = isArray ? ArrayTypeName.of(ClassName.bestGuess(getNewClassName(enclosedElement.getSimpleName().toString())))
                            : ClassName.bestGuess(getNewClassName(enclosedElement.getSimpleName().toString()));

                    FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(innerClassName, "body", Modifier.PRIVATE);

                    if (enclosedElement.getAnnotation(BodySpec.class) != null) {
                        fieldSpecBuilder.addAnnotation(AnnotationSpec.get(enclosedElement.getAnnotation(BodySpec.class)));
                    } else {
                        AnnotationSpec bodySpecAnnotation = AnnotationSpec.builder(BodySpec.class).build();
                        fieldSpecBuilder.addAnnotation(bodySpecAnnotation);
                    }
                    classBuilder.addField(fieldSpecBuilder.build());

                    MethodSpec.Builder methodSpec = MethodSpec.methodBuilder("body")
                            .addModifiers(Modifier.PUBLIC)
                            .addParameter(innerClassName, "body")
                            .returns(ClassName.bestGuess(requestFormClassName))
                            .addStatement("this.body = body")
                            .addStatement("return this");

                    if (isArray || enclosedElement.asType().getKind() == TypeKind.ARRAY) {
                        methodSpec.varargs(true);
                    }
                    classBuilder.addMethod(methodSpec.build());
                }
            }
        }

        for (AnnotationMirror annotationMirror : element.getAnnotationMirrors()) {
            classBuilder.addAnnotation(AnnotationSpec.get(annotationMirror));
        }

        processedClasses.add(requestFormClassName);

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
            } else if (
                    enclosedElement.getKind() == ElementKind.CLASS &&
                            (enclosedElement.getAnnotation(Schema.class) != null || enclosedElement.getAnnotation(ArraySchema.class) != null)
            ) {
                TypeSpec innerClassSpec = createInnerClassSpec(enclosedElement);
                innerClassBuilder.addType(innerClassSpec);

                boolean isArray = enclosedElement.getAnnotation(ArraySchema.class) != null;
                TypeName innerClassName = isArray ? ArrayTypeName.of(ClassName.bestGuess(getNewClassName(enclosedElement.getSimpleName().toString())))
                        : ClassName.bestGuess(getNewClassName(enclosedElement.getSimpleName().toString()));

                String fieldName = enclosedElement.getSimpleName().toString().toLowerCase();
                FieldSpec.Builder fieldSpecBuilder = FieldSpec.builder(innerClassName, fieldName, Modifier.PRIVATE);
                FieldSpec fieldSpec = fieldSpecBuilder.build();
                innerClassBuilder.addField(fieldSpec);

                MethodSpec.Builder methodSpec = MethodSpec.methodBuilder(fieldName)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(innerClassName, fieldName)
                        .returns(ClassName.bestGuess(className))
                        .addStatement(String.format("this.%s = %s", fieldName, fieldName))
                        .addStatement("return this");

                if (isArray || enclosedElement.asType().getKind() == TypeKind.ARRAY) {
                    methodSpec.varargs(true);
                }
                innerClassBuilder.addMethod(methodSpec.build());
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
