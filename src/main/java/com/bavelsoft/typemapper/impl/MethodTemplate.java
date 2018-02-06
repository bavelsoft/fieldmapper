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

class MethodTemplate {
	private final Map<String, Element> dstFields;
	private final Map<StringPair, Element> srcFields;
	private final Map<String, String> templateData;
	private final StrSubstitutor sub;
	private final ExecutableElement methodElement;
	private final Elements elementUtils;
	private final Types typeUtils;

	MethodTemplate(ExecutableElement methodElement, Elements elementUtils, Types typeUtils) {
		TypeMirror dstType = Util.returnType(methodElement);
		this.dstFields = initDstFields(dstType, elementUtils);
		this.srcFields = initSrcFields(methodElement, elementUtils);
		this.templateData = initMap(dstType);
		this.sub = new StrSubstitutor(templateData);
		this.methodElement = methodElement;
		this.elementUtils = elementUtils;
		this.typeUtils = typeUtils;
	}

	private static Map<String, String> initMap(TypeMirror dstType) {
	 	Map<String,String> templateData = new HashMap<>();
		templateData.put(TypeMap.DST, "dst");
		templateData.put(TypeMap.DST_TYPE, dstType.toString());
		return templateData;
	}

	private static Map<StringPair, Element> initSrcFields(ExecutableElement methodElement, Elements elementUtils) {
		Map<StringPair, Element> srcFields = new HashMap<>();
		for (Element parameter : methodElement.getParameters()) {
			String paramName = parameter.getSimpleName().toString();
			Map<String, Element> singleSrcFields = getFields(parameter.asType(), elementUtils);
			singleSrcFields.entrySet().removeIf(e->Util.returnType((ExecutableElement)e.getValue()) == null);
			for (Map.Entry<String, Element> entry : singleSrcFields.entrySet()) {
				StringPair key = StringPair.create(paramName, entry.getKey());
				srcFields.put(key, entry.getValue());
			}
		}
		return srcFields;
	}
	
	private static Map<String, Element> initDstFields(TypeMirror dstType, Elements elementUtils) {
		Map<String, Element> dstFields = getFields(dstType, elementUtils);
		dstFields.entrySet().removeIf(e->Util.paramType((ExecutableElement)e.getValue()) == null);
		dstFields.entrySet().removeIf(e->e.getKey().equals("equals"));
		return dstFields;
	}

	void setPerFieldValues(Map.Entry<String, StringPair> entry) {
		setPerFieldValues(entry, null);
	}

	void setPerFieldValues(Map.Entry<String, StringPair> entry, TypeElement classWithMapMethod) {
		templateData.put(TypeMap.DST_FIELD, entry.getKey());
		templateData.put(TypeMap.SRC_FIELD, entry.getValue().toString());
			TypeMirror dstType, srcType;
		try {
			dstType = Util.paramType(dstFields.get(entry.getKey()));
		} catch (NullPointerException e) {
			throw new ExpectedException("no setter for "+entry.getKey());
		}
		try {
			srcType = Util.returnType(srcFields.get(entry.getValue()));
		} catch (NullPointerException e) {
			throw new ExpectedException("no getter for "+entry.getValue());
		}
		templateData.put(TypeMap.FUNC, getMapMethodName(dstType, srcType, classWithMapMethod));
	}

	String replace(String text) {
		try {
			return sub.replace(text);
		} catch (Exception e) {
			throw new ExpectedException("couldn't replace: "+text);
		}
	}

	Collection<String> getDstFields() {
		return dstFields.keySet();
	}
	
	Collection<StringPair> getSrcFields() {
		return srcFields.keySet();
	}
	
	private static Map<String,Element> getFields(TypeMirror typeMirror, Elements elementUtils) {
		Map<String,Element> fields = new HashMap<>();
		TypeElement element = (TypeElement)asElement(typeMirror);
		for (Element fieldElement : elementUtils.getAllMembers(element))
			if (fieldElement.getKind() == ElementKind.METHOD)
				fields.put(fieldElement.getSimpleName().toString(), fieldElement); //TODO overloading!
		return fields;
	}
	
	private static Element asElement(TypeMirror t) {
		if (t instanceof DeclaredType)
			return ((DeclaredType)t).asElement();
		else
			return null;
	}

	private String getMapMethodName(TypeMirror dstType, TypeMirror srcType, TypeElement classWithMapMethod) {
		if (classWithMapMethod == null)
			classWithMapMethod = (TypeElement)methodElement.getEnclosingElement();
		//TODO use less exact map method
		//TODO complain of ambiguous map method
		if (srcType == null || dstType == null)
			return "";
		for (Element e : elementUtils.getAllMembers(classWithMapMethod))
			if (e.getKind() == ElementKind.METHOD)
				if (isSame(srcType, Util.paramType(e)) && isSame(dstType, Util.returnType(e)))
					return e.getSimpleName().toString();
		return "";
	}

	private boolean isSame(TypeMirror a, TypeMirror b) {
		if (a == b)
			return true;
		else if (a == null || b == null)
			return false;
		else
			return typeUtils.isSameType(a, b);
	}
}


