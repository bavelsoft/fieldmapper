package com.bavelsoft.typemapper.impl;

import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.ElementKind;
import javax.lang.model.type.TypeMirror;
import java.util.Map;
import java.util.HashMap;
import com.bavelsoft.typemapper.FieldMatchStrategy.StringPair;

class FieldLister {
	private final Elements elementUtils;
	private final Types typeUtils;

	FieldLister(Elements elementUtils, Types typeUtils) {
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
	}

	Map<StringPair, Element> getSourceFields(ExecutableElement methodElement) {
		Map<StringPair, Element> sourceFields = new HashMap<>();
		for (Element parameter : methodElement.getParameters()) {
			String paramName = parameter.getSimpleName().toString();
			Map<String, Element> singleSourceFields = getFields(parameter.asType());
			singleSourceFields.entrySet().removeIf(e->Util.returnType((ExecutableElement)e.getValue()) == null);
			for (Map.Entry<String, Element> entry : singleSourceFields.entrySet()) {
				StringPair key = StringPair.create(paramName, entry.getKey()+"()");
				sourceFields.put(key, entry.getValue());
			}
		}
		return sourceFields;
	}
	
	Map<String, Element> getTargetFields(TypeMirror targetType) {
		Map<String, Element> targetFields = getFields(targetType);
		targetFields.entrySet().removeIf(e->Util.paramType((ExecutableElement)e.getValue()) == null);
		targetFields.entrySet().removeIf(e->e.getKey().equals("equals"));
		return targetFields;
	}

	private Map<String, Element> getFields(TypeMirror typeMirror) {
		Map<String,Element> fields = new HashMap<>();
		TypeElement element = (TypeElement)Util.asElement(typeMirror);
		for (Element fieldElement : elementUtils.getAllMembers(element))
			if (fieldElement.getKind() == ElementKind.METHOD)
				fields.put(fieldElement.getSimpleName().toString(), fieldElement); //TODO overloading!
		return fields;
	}
}
