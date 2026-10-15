package com.thizthizzydizzy.treefeller.core.debug;
/** Increasing diagnostic detail; USER is intended for players troubleshooting a cut. */
public enum DebugLevel { OFF, USER, SUMMARY, DETAIL, TRACE;
    public boolean includes(DebugLevel level){return this!=OFF&&ordinal()>=level.ordinal();}
}
