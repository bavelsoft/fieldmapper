package com.bavelsoft.typemapper.impl;

import java.util.Map;
import java.util.HashMap;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;
import javax.lang.model.type.TypeMirror;
import org.apache.commons.text.StrSubstitutor;
import com.bavelsoft.typemapper.TypeMap;

class MethodTemplate {
	final Map<String, Element> dstFields;
	final Map<String, Element> srcFields;
	private final Map<String, String> map = new HashMap<>();
	private final StrSubstitutor sub = new StrSubstitutor(map);
	private final ExecutableElement methodElement;
	private final Elements elementUtils;

	MethodTemplate(ExecutableElement methodElement, Elements elementUtils) {
		TypeMirror dstType = Util.returnType(methodElement);
		TypeMirror srcType = Util.paramType(methodElement);
		this.methodElement = methodElement;
		this.elementUtils = elementUtils;
		this.dstFields = getFields(dstType);
		this.srcFields = getFields(srcType);
		map.put(TypeMap.SRC, "src"); //TODO sync this with the method signature
		map.put(TypeMap.DST, "dst");
		map.put(TypeMap.DST_TYPE, dstType.toString());
	}

	void setPerFieldValues(Map.Entry<String, String> entry) {
		map.put(TypeMap.DST_FIELD, entry.getKey());
		map.put(TypeMap.SRC_FIELD, entry.getValue());
		TypeMirror dstType = Util.paramType(dstFields.get(entry.getKey()));
		TypeMirror srcType = Util.returnType(srcFields.get(entry.getValue()));
		map.put(TypeMap.FUNC, mapMethod(dstType, srcType, methodElement));
	}

	String replace(String text) {
		try {
			return sub.replace(text);
		} catch (Exception e) {
			throw new RuntimeException("couldn't replace: "+text);
		}
	}

	private Map<String,Element> getFields(TypeMirror typeMirror) {
		Map<String,Element> fields = new HashMap<>();
		TypeElement element = (TypeElement)asElement(typeMirror);
		for (Element fieldElement : elementUtils.getAllMembers(element))
			fields.put(fieldElement.getSimpleName().toString(), fieldElement); //TODO overloading!
		return fields;
	}
	
	private Element asElement(TypeMirror t) {
		if (t instanceof DeclaredType)
			return ((DeclaredType)t).asElement();
		else
			return null;
	}

	private String mapMethod(TypeMirror dstType, TypeMirror srcType, ExecutableElement methodElement) {
		TypeElement element = (TypeElement)methodElement.getEnclosingElement();
		//TODO use less exact map method
		//TODO complain of ambiguous map method
		if (srcType == null || dstType == null)
			return "";
		for (Element e : elementUtils.getAllMembers(element))
			if (e.getKind() == ElementKind.METHOD)
				if (srcType.equals(Util.paramType(e)) && dstType.equals(Util.returnType(e))) //TODO make this extensible, e.g. SBE mapping
					return e.getSimpleName().toString();
		return "";
	}
}

