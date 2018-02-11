package com.bavelsoft.typemapper.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.text.StrSubstitutor;
import com.bavelsoft.typemapper.ExpectedException;
import com.bavelsoft.typemapper.TypeMap;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;

/*
 * This is the only real stateful class in the project.
 */
class MethodTemplate {
	private final Map<String, Element> targetFields;
	private final Map<StringPair, Element> sourceFields;
	private final Map<String, String> templateData;
	private final StrSubstitutor sub;
	private final ExecutableElement methodElement;
	private final Elements elementUtils;
	private final Types typeUtils;

	MethodTemplate(ExecutableElement methodElement, Elements elementUtils, Types typeUtils) {
		TypeMirror targetType = Util.returnType(methodElement);
		this.targetFields = initTargetFields(targetType, elementUtils);
		this.sourceFields = initSourceFields(methodElement, elementUtils);
		this.templateData = initTemplateData(targetType);
		this.sub = new StrSubstitutor(templateData);
		this.methodElement = methodElement;
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
	}

	private static Map<String, String> initTemplateData(TypeMirror targetType) {
	 	Map<String,String> templateData = new HashMap<>();
		templateData.put(TypeMap.TARGET_NAME, "target");
		templateData.put(TypeMap.TARGET_TYPE, targetType.toString());
		return templateData;
	}

	private static Map<StringPair, Element> initSourceFields(ExecutableElement methodElement, Elements elementUtils) {
		Map<StringPair, Element> sourceFields = new HashMap<>();
		for (Element parameter : methodElement.getParameters()) {
			String paramName = parameter.getSimpleName().toString();
			Map<String, Element> singleSourceFields = getFields(parameter.asType(), elementUtils);
			singleSourceFields.entrySet().removeIf(e->Util.returnType((ExecutableElement)e.getValue()) == null);
			for (Map.Entry<String, Element> entry : singleSourceFields.entrySet()) {
				StringPair key = StringPair.create(paramName, entry.getKey()+"()");
				sourceFields.put(key, entry.getValue());
			}
		}
		return sourceFields;
	}
	
	private static Map<String, Element> initTargetFields(TypeMirror targetType, Elements elementUtils) {
		Map<String, Element> targetFields = getFields(targetType, elementUtils);
		targetFields.entrySet().removeIf(e->Util.paramType((ExecutableElement)e.getValue()) == null);
		targetFields.entrySet().removeIf(e->e.getKey().equals("equals"));
		return targetFields;
	}

	void setPerFieldValues(Map.Entry<String, StringPair> entry) {
		setPerFieldValues(entry, null);
	}

	void setPerFieldValues(Map.Entry<String, StringPair> entry, TypeElement classWithMapMethod) {
		templateData.put(TypeMap.TARGET_FIELD_NAME, entry.getKey());
		templateData.put(TypeMap.SOURCE_FIELD_GETTER, entry.getValue().toString());
			TypeMirror targetType, sourceType;
		try {
			targetType = Util.paramType(targetFields.get(entry.getKey()));
		} catch (NullPointerException e) {
			throw new ExpectedException("no setter for "+entry.getKey());
		}
		try {
			sourceType = Util.returnType(sourceFields.get(entry.getValue()));
		} catch (NullPointerException e) {
			throw new ExpectedException("no getter for "+entry.getValue());
		}
		templateData.put(TypeMap.FUNC, getMapMethodName(targetType, sourceType, classWithMapMethod));
	}

	String replace(String text) {
		try {
			return sub.replace(text);
		} catch (Exception e) {
			throw new ExpectedException("couldn't replace: "+text);
		}
	}

	Collection<String> getTargetFields() {
		return targetFields.keySet();
	}
	
	Collection<StringPair> getSourceFields() {
		return sourceFields.keySet();
	}
	
	private static Map<String,Element> getFields(TypeMirror typeMirror, Elements elementUtils) {
		Map<String,Element> fields = new HashMap<>();
		TypeElement element = (TypeElement)Util.asElement(typeMirror);
		for (Element fieldElement : elementUtils.getAllMembers(element))
			if (fieldElement.getKind() == ElementKind.METHOD)
				fields.put(fieldElement.getSimpleName().toString(), fieldElement); //TODO overloading!
		return fields;
	}
	
	private String getMapMethodName(TypeMirror targetType, TypeMirror sourceType, TypeElement classWithMapMethod) {
		if (classWithMapMethod == null)
			classWithMapMethod = (TypeElement)methodElement.getEnclosingElement();
		//TODO use less exact map method
		//TODO complain of ambiguous map method
		if (sourceType == null || targetType == null)
			return "";
		for (Element e : elementUtils.getAllMembers(classWithMapMethod))
			if (e.getKind() == ElementKind.METHOD)
				if (Util.isSame(sourceType, Util.paramType(e), typeUtils)
				&& Util.isSame(targetType, Util.returnType(e), typeUtils))
					return e.getSimpleName().toString();
		return "";
	}
}
