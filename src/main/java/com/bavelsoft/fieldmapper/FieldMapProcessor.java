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
import org.apache.commons.text.StrSubstitutor;

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
		for (Element element : env.getElementsAnnotatedWith(FieldMap.class)) {
			try {
				process(element);
			} catch (IOException e) {
                        	throw new RuntimeException(e);
                	}
		}
		return true;
	}

	private void process(Element methodElement) throws IOException {
		write(methodElement.getEnclosingElement(), getType(methodElement).build());
	}

	private TypeSpec.Builder getType(Element methodElement) {
		Element element = methodElement.getEnclosingElement();
		TypeSpec.Builder type = TypeSpec.classBuilder(getClassName(element))
			.addSuperinterface(TypeName.get(element.asType()));
		
		for (Element e : elementUtils.getAllMembers((TypeElement)element))
			if (e.getKind() == ElementKind.METHOD && e.getAnnotation(FieldMap.class) != null)
				type.addMethod(getMethod((ExecutableElement)e).build());
		return type;
	}

	private MethodSpec.Builder getMethod(ExecutableElement methodElement) {
		//TODO split up this method?
		FieldMap annotation = methodElement.getAnnotation(FieldMap.class);
		BiFunction<Collection<String>,Collection<String>,Map<String,String>> match = classValue(annotation::match);
		TypeMirror dstType = returnType(methodElement);
		Map<String, Element> dstFields = getFields(dstType);
		TypeMirror srcType = paramType(methodElement);
		Map<String, Element> srcFields = getFields(srcType);
		Map<String, String> matchedFields;
		try {
			matchedFields = match.apply(dstFields.keySet(), srcFields.keySet());
		} catch (Exception e) {
			fatal("couldn't match");
			throw e;
		}
		//TODO nice reporting of unmapped fields

		MethodSpec.Builder method = MethodSpec.overriding(methodElement);
		Map<String,String> map = new HashMap<>();
		map.put("src", "src"); //TODO sync this with the method signature
		map.put("dst", "dst");
		map.put("dstType", dstType.toString());
		StrSubstitutor sub = new StrSubstitutor(map);
		method.addStatement(replace(sub, annotation.first()));
		for (String dstField : matchedFields.keySet()) {
			String srcField = matchedFields.get(dstField);
			TypeElement element = (TypeElement)methodElement.getEnclosingElement();
			String func = mapMethod(element, dstFields.get(dstField), srcFields.get(srcField));
			map.put("srcField", srcField);
			map.put("dstField", dstField);
			map.put("func", func);
			method.addStatement(replace(sub, annotation.perField()));
		}
		method.addStatement(replace(sub, annotation.last()));
		return method;
	}

	private String mapMethod(TypeElement element, Element dstField, Element srcField) {
		//TODO use less exact map method
		//TODO complain of ambiguous map method
		TypeMirror srcType = returnType(srcField);
		TypeMirror dstType = paramType(dstField);
		if (srcType == null || dstType == null)
			return "";
		for (Element e : elementUtils.getAllMembers(element))
			if (e.getKind() == ElementKind.METHOD)
				if (srcType.equals(paramType(e)) && dstType.equals(returnType(e))) //TODO make this extensible, e.g. SBE mapping
					return e.getSimpleName().toString();
		return "";
	}

	private TypeMirror paramType(Element e) {
		ExecutableElement ee = (ExecutableElement)e;
		return ee.getParameters().size() == 1 ? ee.getParameters().get(0).asType() : null;
	}

	private TypeMirror returnType(Element e) {
		ExecutableElement ee = (ExecutableElement)e;
		return ee.getReturnType();
	}

	private Element asElement(TypeMirror t) {
		if (t instanceof DeclaredType)
			return ((DeclaredType)t).asElement();
		else
			return null;
	}

	private String replace(StrSubstitutor sub, String text) {
		try {
			return sub.replace(text);
		} catch (Exception e) {
			fatal("couldn't replace: "+text);
			throw e;
		}
	}

	private Map<String,Element> getFields(TypeMirror typeMirror) {
		Map<String,Element> fields = new HashMap<>();
		TypeElement element = (TypeElement)asElement(typeMirror);
		for (Element fieldElement : elementUtils.getAllMembers(element))
			fields.put(fieldElement.getSimpleName().toString(), fieldElement); //TODO overloading!
		return fields;
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
/*
unit tests!
TODOs
optimize
map a single field
map a single field from nested path
nullmapper
warn of unmapped fields taking singe field overrides into account
unambiguously name inner class interface
inheritance
map fields instead of methods
generated class as abstract if we don't implement all methods
annotations for primitives separately
annotations for primitive wrappers separately
annotations for enums separately
*/

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
