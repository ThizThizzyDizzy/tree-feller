package com.thizthizzydizzy.treefeller.core.config.structure.special;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
/** Platform-independent item metadata requirements. */
public abstract class ItemDefinition implements IItemDefinition{
    @ConfigComment("Allowed and refused integer custom model data values; absent data is distinct from zero")
    public DualList<Integer> custom_model_data;

    public boolean matchesCustomModelData(Integer value){
        return custom_model_data==null||custom_model_data.applySingle(value);
    }
}
