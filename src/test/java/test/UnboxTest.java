package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import com.bavelsoft.typemapper.MapperDefault;

public class UnboxTest {
	public interface Foo extends MapperDefault {
		@TypeMap
		MyTarget f(MyBoxedSource source);
	}

	static class MyBoxedSource {
		Integer x;
		MyBoxedSource(Integer x) { this.x = x; }
		Integer getX() { return x; }
	
		char getZ() { return 'z'; }
	}

	@Test public void test()  {
		Foo mapper = new UnboxTest_FooTypeMapper();
		MyTarget target = mapper.f(new MyBoxedSource(123));
		assertEquals(123, target.x);
	}

	@Test public void testNull()  {
		Foo mapper = new UnboxTest_FooTypeMapper();
		MyTarget target = mapper.f(new MyBoxedSource(null));
		assertEquals(0, target.x);
	}
}
