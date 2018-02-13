package com.bavelsoft.typemapper.match;

import java.util.Collection;
import com.bavelsoft.typemapper.FieldMatchStrategy.StringPair;
import com.bavelsoft.typemapper.ExpectedException;

public class FieldMatchRelaxed extends FieldMatchParanoid {
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
