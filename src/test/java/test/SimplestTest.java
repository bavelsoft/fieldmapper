package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SimplestTest {
	public interface Foo {
		@TypeMap
		MyTarget f(MySource source);
	}

	@Test public void test()  {
		Foo mapper = new SimplestTest_FooTypeMapper();
		MyTarget target = mapper.f(new MySource(123));
		assertEquals(123, target.x);
	}
}
