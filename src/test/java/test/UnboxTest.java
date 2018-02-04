package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;
import com.bavelsoft.typemapper.MapperDefault;

public class UnboxTest {
	public interface Foo extends MapperDefault {
		@TypeMap
		MyDst f(MyBoxedSrc src);
	}

	@Test public void test()  {
		Foo mapper = new UnboxTest_FooTypeMapper();
		MyDst dst = mapper.f(new MyBoxedSrc(123));
		assertEquals(123, dst.x);
	}

	@Test public void testNull()  {
		Foo mapper = new UnboxTest_FooTypeMapper();
		MyDst dst = mapper.f(new MyBoxedSrc(null));
		assertEquals(0, dst.x);
	}
}
