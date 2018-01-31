package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SimplestTest {
	public interface Foo {
		@TypeMap
		MyDst f(MySrc src);
	}

	@Test public void simple()  {
		Foo mapper = new SimplestTest_FooTypeMapper();
		MyDst dst = mapper.f(new MySrc(123));
		assertEquals(123, dst.x);
	}
}
