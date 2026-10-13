package com.thizthizzydizzy.treefeller.core.breaking;
public final class FallVector{
    public final double x, y, z;
    public FallVector(double x, double y, double z){this.x=x; this.y=y; this.z=z;}
    public FallVector add(FallVector v){return new FallVector(x+v.x,y+v.y,z+v.z);}
    public FallVector scale(double n){return new FallVector(x*n,y*n,z*n);}
    public double horizontalLength(){return Math.hypot(x,z);}
    public boolean finite(){return Double.isFinite(x)&&Double.isFinite(y)&&Double.isFinite(z);}
}
