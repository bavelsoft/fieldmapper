package com.bavelsoft.typemapper.impl;

import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.Map;
import java.util.HashSet;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import com.bavelsoft.typemapper.ExpectedException;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeSpec;
import org.apache.commons.lang3.exception.ExceptionUtils;

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
				write(element, Generator.generateMapperClass(element, elementUtils, typeUtils).build());
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

	private void write(Element element, TypeSpec typeSpec) throws IOException {
		String packageName = elementUtils.getPackageOf(element).toString();
		JavaFileObject javaFileObject = filer.createSourceFile(packageName+"."+Generator.getClassName(element));
		Writer writer = javaFileObject.openWriter();
		JavaFile javaFile = JavaFile.builder(packageName, typeSpec).build();
		javaFile.writeTo(writer);
		writer.close();
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
