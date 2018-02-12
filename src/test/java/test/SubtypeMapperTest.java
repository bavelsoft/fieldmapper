package test;

import com.bavelsoft.typemapper.TypeMap;
import com.bavelsoft.typemapper.MapperDefault;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class SubtypeMapperTest {
	public interface Foo extends MapperDefault {
		@TypeMap
		MySubTarget f(MySubSource source);

		default String map(MyType x) { return "MyType"; }
		default MyType map(String x) { return new MyType(); }
		default MySubtype map2(String x) { return new MySubtype(); }
	}

	static class MySubSource {
		Object getBar() { return new Integer(123); }
		MyType getBaz() { return null; }
		String getQux() { return null; }
	}

	static class MySubTarget {
		String bar, baz;
		MyType qux;
		void setBar(String x) { this.bar = x; }
		void setBaz(String x) { this.baz = x; }
		void setQux(MyType x) { this.qux = x; }
	}

	static class MyType {
	}

	static class MySubtype extends MyType {
	}

	@Test public void testObjectToSring()  {
		Foo mapper = new SubtypeMapperTest_FooTypeMapper();
		MySubTarget target = mapper.f(new MySubSource());
		assertEquals("123", target.bar);
	}

	@Test public void testMyTypeToSring()  {
		Foo mapper = new SubtypeMapperTest_FooTypeMapper();
		MySubTarget target = mapper.f(new MySubSource());
		assertEquals("MyType", target.baz);
	}

	@Test public void testStringToMyType()  {
		Foo mapper = new SubtypeMapperTest_FooTypeMapper();
		MySubTarget target = mapper.f(new MySubSource());
		assertEquals(MyType.class, target.qux.getClass());
	}
}
