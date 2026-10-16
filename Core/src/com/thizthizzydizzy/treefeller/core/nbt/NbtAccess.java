package com.thizthizzydizzy.treefeller.core.nbt;
/** Native SNBT parsing; matching and merging stay in Core. */
public interface NbtAccess{
    byte[] parse(String snbt);
}
