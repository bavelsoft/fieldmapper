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

class TypeMapperMethod {
//TODO use MapperDefault by default, even if the user doesn't extend/implement it

	static void addEnumMappers(TypeSpec.Builder type, MethodTemplate template) {
		for (MethodTemplate.EnumMapper enumMapper : template.getEnumMappers()) {
			MethodSpec.Builder m = MethodSpec.methodBuilder(enumMapper.name)
				.addParameter(TypeName.get(enumMapper.sourceType), "x")
				.returns(TypeName.get(enumMapper.targetType));
			for (Element e : ((DeclaredType)enumMapper.sourceType).asElement().getEnclosedElements()) {
				if (e.getKind() == ElementKind.ENUM_CONSTANT) {
					String enumValue = ((VariableElement)e).getSimpleName().toString();
					m.addStatement("if (x == $T.$L) return $T.$L", enumMapper.sourceType, enumValue, enumMapper.targetType, enumValue);
				}
			}
			m.addStatement("return null");
			//TODO customize handling of no match
			type.addMethod(m.build());
		}
	}
}
