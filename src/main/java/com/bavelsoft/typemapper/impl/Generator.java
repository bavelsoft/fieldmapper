package com.bavelsoft.typemapper.impl;

import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;
import java.util.Map;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import com.bavelsoft.typemapper.FieldMatcher;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;
import com.bavelsoft.typemapper.TypeMap;

/*
 * Central class, calls MethodTemplate and FieldMatchSupport
 */
class Generator {
	static TypeSpec.Builder generateMapperClass(Element element, Elements elementUtils, Types typeUtils) {
		TypeSpec.Builder type = TypeSpec.classBuilder(getClassName(element));
		if (element.getKind() == ElementKind.INTERFACE)
			type.addSuperinterface(TypeName.get(element.asType()));
		else
			type.superclass(TypeName.get(element.asType()));
		
		boolean hasUnimplemented = false;
		for (Element e : elementUtils.getAllMembers((TypeElement)element)) {
			if (e.getKind() == ElementKind.METHOD && e.getAnnotation(FieldMatchSupport.typeMapClass) != null) {
				type.addMethod(generateMapperMethod((ExecutableElement)e, elementUtils, typeUtils).build());
			} else if (e.getKind() == ElementKind.METHOD && Util.isAbstract(e)) {
				hasUnimplemented = true;
			}
		}
		if (hasUnimplemented)
			type.addModifiers(Modifier.ABSTRACT);
		return type;
	}

	static MethodSpec.Builder generateMapperMethod(ExecutableElement methodElement, Elements elementUtils, Types typeUtils) {
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

	static String getClassName(Element element) {
		String name = element.getSimpleName().toString() + "TypeMapper";
		if (element.getEnclosingElement().getKind() == ElementKind.CLASS)
			name = element.getEnclosingElement().getSimpleName().toString() + "_" + name;
		return name;
	}
}
