package com.bavelsoft.typemapper.impl;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.exception.ExceptionUtils;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.List;
import java.util.Iterator;
import java.util.Map;
import java.util.HashSet;
import java.util.HashMap;
import java.util.Collection;
import java.util.Collections;
import java.util.ArrayList;
import java.util.function.BiFunction;
import java.util.function.Supplier;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;
import javax.annotation.processing.Filer;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.tools.Diagnostic;
import javax.lang.model.SourceVersion;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import com.google.auto.service.AutoService;
import com.bavelsoft.typemapper.TypeMap;
import com.bavelsoft.typemapper.ExpectedException;
import com.bavelsoft.typemapper.Field;
import com.bavelsoft.typemapper.Fields;
import com.bavelsoft.typemapper.FieldMatcher;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;

import static java.util.Arrays.asList;

@AutoService(Processor.class)
public class TypeMapProcessor extends AbstractProcessor {
	private Messager messager;
	private Elements elementUtils;
	private Types typeUtils;
	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);
		messager = env.getMessager();
		elementUtils = env.getElementUtils();
		typeUtils = env.getTypeUtils();
		filer = env.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotationsParam, RoundEnvironment env) {
		Set<Element> elements = new HashSet<>();
		for (Element element : env.getElementsAnnotatedWith(FieldMatchSupport.typeMapClass))
			elements.add(element.getEnclosingElement());
		for (Element element : elements) {
			try {
				write(element, generateMapperClass(element).build());
			} catch (ExpectedException e) {
				messager.printMessage(Diagnostic.Kind.ERROR,
						      "couldn't generate field mapper for "+element+" : "+ e.getMessage());
			} catch (Exception e) {
				messager.printMessage(Diagnostic.Kind.ERROR,
						      "couldn't generate field mapper for "+element+" : "+ ExceptionUtils.getStackTrace(e));
                	} 
		}
		return true;
	}

	private TypeSpec.Builder generateMapperClass(Element element) {
		TypeSpec.Builder type = TypeSpec.classBuilder(getClassName(element));
		if (element.getKind() == ElementKind.INTERFACE)
			type.addSuperinterface(TypeName.get(element.asType()));
		else
			type.superclass(TypeName.get(element.asType()));
		
		boolean hasUnimplemented = false;
		for (Element e : elementUtils.getAllMembers((TypeElement)element)) {
			if (e.getKind() == ElementKind.METHOD && e.getAnnotation(FieldMatchSupport.typeMapClass) != null) {
				type.addMethod(generateMapperMethod((ExecutableElement)e).build());
			} else if (e.getKind() == ElementKind.METHOD && Util.isAbstract(e)) {
				hasUnimplemented = true;
			}
		}
		if (hasUnimplemented)
			type.addModifiers(Modifier.ABSTRACT);
		return type;
	}

	private MethodSpec.Builder generateMapperMethod(ExecutableElement methodElement) {
		TypeMap annotation = methodElement.getAnnotation(FieldMatchSupport.typeMapClass);
		MethodTemplate template = new MethodTemplate(methodElement, elementUtils, typeUtils);

		MethodSpec.Builder method = MethodSpec.overriding(methodElement)
			.addStatement(template.replace(annotation.first()));

		Map<String, StringPair> map = FieldMatchSupport.getMatchedFields(methodElement, template);
		for (Map.Entry<String, StringPair> entry : map.entrySet()) {
			template.setPerFieldValues(entry);
			method.addStatement(template.replace(annotation.perField()));
		}
		return method.addStatement(template.replace(annotation.last()));
	}

	private void write(Element element, TypeSpec typeSpec) throws IOException {
		String packageName = elementUtils.getPackageOf(element).toString();
		JavaFileObject javaFileObject = filer.createSourceFile(packageName+"."+getClassName(element));
		Writer writer = javaFileObject.openWriter();
		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
		javaFile.writeTo(writer);
		writer.close();
	}

	private String getClassName(Element element) {
		String name = element.getSimpleName().toString() + "TypeMapper";
		if (element.getEnclosingElement().getKind() == ElementKind.CLASS)
			name = element.getEnclosingElement().getSimpleName().toString() + "_" + name;
		return name;
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return new HashSet<>(asList(
			FieldMatchSupport.typeMapClass.getCanonicalName().toString(),
			FieldMatchSupport.fieldClass.getCanonicalName().toString(),
			FieldMatchSupport.fieldsClass.getCanonicalName().toString()
		));
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}
