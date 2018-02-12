package test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Set;
import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ConstructorTest {
	public interface Foo {
		@TypeMap
		MyTarget f(MySource source);
	}

	static class MySource {
		Set getX() { return Collections.singleton("123"); }
	}

	static class MyTarget {
		ArrayList x;
		void setX(ArrayList x) { this.x = x; }
	}

	@Test public void test()  {
		Foo mapper = new ConstructorTest_FooTypeMapper();
		MyTarget target = mapper.f(new MySource());
		assertEquals("123", target.x.get(0));
	}
}
