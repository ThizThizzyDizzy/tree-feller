package com.thizthizzydizzy.treefeller.core.config.structure.special;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Map;
import java.util.LinkedHashMap;
import com.thizthizzydizzy.treefeller.core.config.structure.general.DualList;
/** Platform-independent item metadata requirements. */
public abstract class ItemDefinition extends com.thizthizzydizzy.treefeller.core.nbt.NbtDefinition implements IItemDefinition{
    @ConfigComment("Allowed and refused integer custom model data values; absent data is distinct from zero")
    public DualList<Integer> custom_model_data;

    /** Platform identity field, such as a material or item identifier. */
    protected abstract String identityField();
    protected abstract ItemDefinition copyIdentity();
    @Override
    public Map<String,IItemDefinition> requirements(){
        Map<String,IItemDefinition> result=new LinkedHashMap<>();
        try{
            Field identity=getClass().getField(identityField());
            for(Field field:getClass().getFields()){
                if(Modifier.isStatic(field.getModifiers())||field.getName().equals("nbt"))continue;
                Object value=field.get(this);
                if(value==null&&!field.equals(identity))continue;
                for(Map.Entry<String,Object> projection:projections(value).entrySet()){
                    IItemDefinition requirement=getClass().getConstructor().newInstance();
                    identity.set(requirement,identity.get(this));
                    field.set(requirement,projection.getValue());
                    result.put(field.getName()+projection.getKey(),requirement);
                }
            }
            // Check item identity before metadata, regardless of reflection's field order.
            Map<String,IItemDefinition> ordered=new LinkedHashMap<>();
            ordered.put(identity.getName(),result.remove(identity.getName()));
            ordered.putAll(result);
            if(nbt!=null){ItemDefinition requirement=copyIdentity();requirement.nbt=nbt;ordered.put("nbt",requirement);}
            return ordered;
        }catch(ReflectiveOperationException ex){throw new IllegalStateException("Could not inspect item requirements",ex);}
    }
    /** Isolate nested range/list requirements using the same native matching hook. */
    private static Map<String,Object> projections(Object value) throws ReflectiveOperationException{
        Map<String,Object> result=new LinkedHashMap<>();
        result.put("",value);
        if(value==null)return result;
        Class<?> type=value.getClass();
        Package pkg=type.getPackage();
        if(pkg==null||!pkg.getName().equals("com.thizthizzydizzy.treefeller.core.config.structure.general"))return result;
        for(Field field:type.getFields()){
            Object child=field.get(value);
            if(child==null)continue;
            for(Map.Entry<String,Object> projection:projections(child).entrySet()){
                Object isolated=type.getConstructor().newInstance();
                field.set(isolated,projection.getValue());
                result.put("."+field.getName()+projection.getKey(),isolated);
            }
        }
        return result;
    }
    public boolean matchesCustomModelData(Integer value){
        return custom_model_data==null||custom_model_data.applySingle(value);
    }
}
