package com.bavelsoft.typemapper;
  
public interface MapperDefault {
	default Double map(double x) { return new Double(x); }
	default Float map(float x) { return new Float(x); }
	default Long map(long x) { return new Long(x); }
	default Integer map(int x) { return new Integer(x); }
	default Short map(short x) { return new Short(x); }
	default Byte map(byte x) { return new Byte(x); }
	default Character map(char x) { return new Character(x); }
	default Boolean map(boolean x) { return new Boolean(x); }

	default double map(Double x) { return x == null ? 0 : x; }
	default float map(Float x) { return x == null ? 0 : x; }
	default long map(Long x) { return x == null ? 0 : x; }
	default int map(Integer x) { return x == null ? 0 : x; }
	default short map(Short x) { return x == null ? 0 : x; }
	default byte map(Byte x) { return x == null ? 0 : x; }
	default char map(Character x) { return x == null ? 0 : x; }
	default boolean map(Boolean x) { return x == null ? false : x; }

	default String map(Object x) { return x == null ? null : x.toString(); }
}
