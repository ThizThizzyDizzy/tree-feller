package com.thizthizzydizzy.treefeller.core.config.structure.general;
import java.util.Objects;
import java.util.function.Predicate;
public class DualList<T>{
    public T[] whitelist;
    public T[] blacklist;

    public boolean applyMulti(Predicate<T> predicate){
        if(whitelist!=null){
            for(T t : whitelist){
                if(!predicate.test(t))return false;
            }
        }
        if(blacklist!=null){
            for(T t : blacklist){
                if(predicate.test(t))return false;
            }
        }
        return true;
    }
    public boolean applySingle(T value){
        if(whitelist!=null){
            boolean match = false;
            for(T t : whitelist)match |= Objects.equals(t, value);
            if(!match)return false;
        }
        if(blacklist!=null){
            for(T t : blacklist)if(Objects.equals(t, value))return false;
        }
        return true;
    }
}
