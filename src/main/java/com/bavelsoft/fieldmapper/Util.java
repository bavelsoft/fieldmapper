package com.bavelsoft.fieldmapper;

import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Element;
import javax.lang.model.type.TypeMirror;

class Util {
	static TypeMirror paramType(Element e) {
		ExecutableElement ee = (ExecutableElement)e;
		return ee.getParameters().size() == 1 ? ee.getParameters().get(0).asType() : null;
	}

	static TypeMirror returnType(Element e) {
		ExecutableElement ee = (ExecutableElement)e;
		return ee.getReturnType();
	}
}
