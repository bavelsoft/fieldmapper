package com.bavelsoft.fieldmapper;

import static java.util.Arrays.asList;
import static java.util.Collections.disjoint;
import static java.util.stream.Collectors.toSet;
import static javax.lang.model.type.TypeKind.VOID;
import static javax.tools.StandardLocation.CLASS_OUTPUT;
import static javax.lang.model.element.Modifier.PRIVATE;
import static javax.lang.model.element.Modifier.PUBLIC;
import static javax.lang.model.element.Modifier.FINAL;
import static javax.lang.model.element.Modifier.NATIVE;

import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.ArrayTypeName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.io.IOException;
import java.io.Writer;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collection;
import java.util.Arrays;
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
import javax.lang.model.element.Element;
import javax.lang.model.element.VariableElement;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.MirroredTypeException;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import com.google.auto.service.AutoService;

@AutoService(Processor.class)
public class FieldMapProcessor extends AbstractProcessor {
	private Messager messager;
	private Elements elementUtils;
	private Filer filer;

	@Override
	public synchronized void init(ProcessingEnvironment env) {
		super.init(env);
		messager = env.getMessager();
		elementUtils = env.getElementUtils();
		filer = env.getFiler();
	}

	@Override
	public boolean process(Set<? extends TypeElement> annotationsParam, RoundEnvironment env) {
		Set<Element> elements = new HashSet<>();
		for (Element element : env.getElementsAnnotatedWith(FieldMap.class))
			elements.add(element.getEnclosingElement());
		for (Element element : elements) {
			try {
				write(element, getType(element).build());
			} catch (IOException e) {
                        	throw new RuntimeException(e);
                	}
		}
		return true;
	}

	private TypeSpec.Builder getType(Element element) {
		TypeSpec.Builder type = TypeSpec.classBuilder(getClassName(element))
			.addSuperinterface(TypeName.get(element.asType()));
		
		for (Element e : elementUtils.getAllMembers((TypeElement)element))
			if (e.getKind() == ElementKind.METHOD && e.getAnnotation(FieldMap.class) != null)
				type.addMethod(getMethod((ExecutableElement)e).build());
		return type;
	}

	private MethodSpec.Builder getMethod(ExecutableElement methodElement) {
		FieldMap annotation = methodElement.getAnnotation(FieldMap.class);
		MethodTemplate template = new MethodTemplate(methodElement, elementUtils);

		MethodSpec.Builder method = MethodSpec.overriding(methodElement)
			.addStatement(template.replace(annotation.first()));

		for (Map.Entry<String, String> entry : getMatchedFields(annotation, template.dstFields, template.srcFields)) {
			template.setPerFieldValues(entry);
			method.addStatement(template.replace(annotation.perField()));
		}
		return method.addStatement(template.replace(annotation.last()));
	}

	private Set<Map.Entry<String, String>> getMatchedFields(FieldMap annotation, Map<String, Element> dstFields, Map<String, Element> srcFields) {
		Map<String, String> matchedFields;
		BiFunction<Collection<String>,Collection<String>,Map<String,String>> match = classValue(annotation::match);
		try {
			matchedFields = match.apply(dstFields.keySet(), srcFields.keySet());
		} catch (Exception e) {
			fatal("couldn't match");
			throw e;
		}
		//TODO nice reporting of unmapped fields
		return matchedFields.entrySet();
	}

	private <T> T classValue(Supplier<Class<T>> f) {
		String className;
		try {
			Class<T> clazz = f.get();
			className = clazz.getCanonicalName();
		} catch (MirroredTypeException mte) {
			DeclaredType classTypeMirror = (DeclaredType) mte.getTypeMirror();
			TypeElement classTypeElement = (TypeElement) classTypeMirror.asElement();
			className = classTypeElement.getQualifiedName().toString();
		}
		try {
			return (T)Class.forName(className).newInstance();
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException e) {
			throw new RuntimeException(e);
		}
	}

	private void fatal(String s) { //TODO throwing the exception could be breaking this
		messager.printMessage(Diagnostic.Kind.ERROR, s);
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
		return element.getSimpleName().toString() + "FieldMapper";
	}

	@Override
	public Set<String> getSupportedAnnotationTypes() {
		return annotationTypesForClasses(FieldMap.class);
	}

	private Set<String> annotationTypesForClasses(Class<?>... classes) {
		return Arrays.stream(classes).map(c->c.getCanonicalName()).collect(toSet());
	}

	@Override
	public SourceVersion getSupportedSourceVersion() {
		return SourceVersion.latestSupported();
	}
}
