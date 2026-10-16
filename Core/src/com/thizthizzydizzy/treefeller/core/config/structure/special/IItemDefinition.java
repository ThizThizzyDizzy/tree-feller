package com.thizthizzydizzy.treefeller.core.config.structure.special;
import com.thizthizzydizzy.treefeller.core.config.ISpecialConfigObject;
import java.util.Map;
import java.util.Collections;
public interface IItemDefinition extends ISpecialConfigObject{
    /** Native definitions can expose independent requirements for Core feedback. */
    default Map<String, IItemDefinition> requirements(){
        return Collections.singletonMap("",this);
    }
}
