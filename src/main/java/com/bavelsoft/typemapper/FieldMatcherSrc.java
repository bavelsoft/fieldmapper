package com.bavelsoft.typemapper;

import java.util.Collection;

public class FieldMatcherSrc extends FieldMatcherParanoid {
	@Override
	protected void throwIfDstFieldsUnmatched(Collection<String> unmatchedDstFields) {
		//don't throw
	}
}
