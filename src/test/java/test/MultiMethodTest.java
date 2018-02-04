package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MultiMethodTest {
	public interface Foo {
		@TypeMap
		MyDst f(MySrc src);

		@TypeMap
		MyDst g(MySrc src);
	}

	@Test public void test()  {
		Foo mapper = new MultiMethodTest_FooTypeMapper();
		MyDst dst = mapper.f(new MySrc(123));
		assertEquals(123, dst.x);
	}
}
