package com.bavelsoft.typemapper;

import java.util.Collection;
import com.bavelsoft.typemapper.FieldMatcher.StringPair;

public class FieldMatcherRelaxed extends FieldMatcherParanoid {
	@Override
	protected void throwIfAmbiguous(String dstField) {
		//don't throw
	}

	@Override
	protected void throwIfSrcFieldsUnmatched(Collection<StringPair> unmatchedSrcFields) {
		//don't throw
	}

	@Override
	protected void throwIfDstFieldsUnmatched(Collection<String> unmatchedDstFields) {
		//don't throw
	}
}
