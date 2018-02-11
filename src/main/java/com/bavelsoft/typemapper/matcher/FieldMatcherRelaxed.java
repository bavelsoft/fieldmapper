package com.bavelsoft.typemapper.matcher;

import java.util.Collection;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;
import com.bavelsoft.typemapper.ExpectedException;

public class FieldMatcherRelaxed extends FieldMatcherParanoid {
	@Override
	protected void throwIfAmbiguous(String targetField) {
		//don't throw
	}

	@Override
	protected void throwIfSourceFieldsUnmatched(Collection<StringPair> unmatchedSourceFields) {
		//don't throw
	}

	@Override
	protected void throwIfTargetFieldsUnmatched(Collection<String> unmatchedTargetFields) {
		//don't throw
	}
}
