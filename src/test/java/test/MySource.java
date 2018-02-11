package test;

public class MySource {
	int x, y;
	MySource(int x) { this.x = x; }
	MySource(int x, int y) { this.x = x; this.y = y; }
	int getX() { return x; }
	int getY() { return y; }

	char getZ() { return 'z'; }
}
