package test;

import com.bavelsoft.typemapper.TypeMap;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class MySrc {
	int x;
	MySrc(int x) { this.x = x; }
	int getX() { return x; }
}
