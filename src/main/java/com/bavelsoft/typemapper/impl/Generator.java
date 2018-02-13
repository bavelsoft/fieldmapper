package com.bavelsoft.typemapper.impl;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Map;
import java.util.Collection;
import java.util.HashSet;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import com.bavelsoft.typemapper.FieldMatchStrategy;
import com.bavelsoft.typemapper.FieldMatchStrategy.StringPair;
import com.bavelsoft.typemapper.TypeMap;

/*
 * Central class, calls MethodTemplate, FieldMatcher, FieldLister, TypeMapperMethod
 */
class Generator {
	private final FieldLister fieldLister;
	private final Elements elementUtils;
	private final Types typeUtils;

	Generator(Elements elementUtils, Types typeUtils) {
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
		this.fieldLister = new FieldLister(elementUtils, typeUtils);
	}

	TypeSpec.Builder generateMapperClass(Element element) {
		TypeSpec.Builder type = TypeSpec.classBuilder(getClassName(element));
		if (element.getKind() == ElementKind.INTERFACE)
			type.addSuperinterface(TypeName.get(element.asType()));
		else
			type.superclass(TypeName.get(element.asType()));
		
		boolean hasUnimplemented = false;
		for (Element e : elementUtils.getAllMembers((TypeElement)element)) {
			if (e.getKind() == ElementKind.METHOD && e.getAnnotation(FieldMatcher.typeMapClass) != null) {
				addMapperMethod(type, (ExecutableElement)e);
			} else if (e.getKind() == ElementKind.METHOD && Util.isAbstract(e)) {
				hasUnimplemented = true;
			}
		}
		if (hasUnimplemented)
			type.addModifiers(Modifier.ABSTRACT);
		return type;
	}

	private void addMapperMethod(TypeSpec.Builder type, ExecutableElement methodElement) {
		TypeMap annotation = methodElement.getAnnotation(FieldMatcher.typeMapClass);
		Map<String, Element> targetFields = fieldLister.getTargetFields(Util.returnType(methodElement));
		Map<StringPair, Element> sourceFields = fieldLister.getSourceFields(methodElement);
		MethodTemplate template = new MethodTemplate(methodElement, targetFields, sourceFields, elementUtils, typeUtils);

		MethodSpec.Builder method = MethodSpec.overriding(methodElement)
			.addStatement(template.replace(annotation.first()));

		Map<String, StringPair> map = FieldMatcher.getMatchedFields(methodElement, copyOfKeys(targetFields), copyOfKeys(sourceFields));
		for (Map.Entry<String, StringPair> entry : map.entrySet()) {
			template.setPerFieldValues(entry);
			method.addStatement(template.replace(annotation.perField()));
		}
		method.addStatement(template.replace(annotation.last()));

		type.addMethod(method.build());

		TypeMapperMethod.addEnumMappers(type, template);
	}

	<T> Collection<T> copyOfKeys(Map<T,?> map) {
		return new HashSet<>(map.keySet());
	}

	String getClassName(Element element) {
		String name = element.getSimpleName().toString() + "TypeMapper";
		if (element.getEnclosingElement().getKind() == ElementKind.CLASS)
			name = element.getEnclosingElement().getSimpleName().toString() + "_" + name;
		return name;
	}
}
