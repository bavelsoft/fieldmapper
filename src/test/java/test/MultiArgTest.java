package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MultiArgTest {
	public interface Foo {
		@TypeMap
		MyTarget f(MySource2 source2, MySource3 source3);
	}

	static class MySource2 {
		char z;
		MySource2(char z) { this.z = z; }
		char getZ() { return z; }
	}

	static class MySource3 {
		int x, y;
		MySource3(int x) { this.x = x; }
		MySource3(int x, int y) { this.x = x; this.y = y; }
		int getX() { return x; }
		int getY() { return y; }
	}

	@Test public void test()  {
		Foo mapper = new MultiArgTest_FooTypeMapper();
		MyTarget target = mapper.f(new MySource2('q'), new MySource3(123));
		assertEquals(123, target.x);
		assertEquals('q', target.z);
	}
}
