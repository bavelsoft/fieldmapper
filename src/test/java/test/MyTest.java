package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MyTest {
	public interface Foo {
		@TypeMap
		MyDst f(MySrc src);

		default int doo(int i) { return i*2; }
	}

	public interface Moo {
		@TypeMap
		MyDst f(MySrc src);

		void g();

		default int doo(int i) { return i*2; }
	}

	static class MySrc {
		int getX() { return 123; }
	}

	static class MyDst {
		int x;
		void setX(int x) { this.x = x; }
	}

	@Test public void simple()  {
	}
}
