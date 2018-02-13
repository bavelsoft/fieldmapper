package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class EnumTest {
	public interface Foo {
		@TypeMap
		MyTarget f(MySource source);
	}

	static class MySource {
		Bar getEnum() { return Bar.Quux; }
	}
	
	static class MyTarget {
		Baz x;
		void setEnum(Baz x) { this.x = x; }
	}

	enum Bar { Qux, Quux }
	enum Baz { Qux, Quux, Quuux }

	@Test public void test()  {
		Foo mapper = new EnumTest_FooTypeMapper();
		MyTarget target = mapper.f(new MySource());
		assertEquals(Baz.Quux, target.x);
	}
}
