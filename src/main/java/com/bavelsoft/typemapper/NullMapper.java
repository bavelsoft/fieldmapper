package com.bavelsoft.typemapper;
  
//TODO rename
public class NullMapper {  
	default double map(Double x) { return x == null ? 0 : x; }
	default float map(Float x) { return x == null ? 0 : x; }
	default long map(Long x) { return x == null ? 0 : x; }
	default int map(Integer x) { return x == null ? 0 : x; }
	default short map(Short x) { return x == null ? 0 : x; }
	default byte map(Byte x) { return x == null ? 0 : x; }
	default char map(Character x) { return x == null ? 0 : x; }
	default boolean map(Boolean x) { return x == null ? false : x; }
}
