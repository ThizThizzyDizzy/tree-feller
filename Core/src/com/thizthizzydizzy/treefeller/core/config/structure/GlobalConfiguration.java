package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultFallDirection;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultFloat;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultNewInstance;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultValue;
import com.thizthizzydizzy.treefeller.core.config.structure.section.BreakingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CriteriaConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.CuttingConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.DetectionConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.ResultConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.TriggerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.section.breaking.FallDirection;
import java.lang.reflect.Field;
public class GlobalConfiguration{
    @ConfigComment("This section defines when TreeFeller can start searching for a tree")
    public TriggerConfiguration trigger = new TriggerConfiguration();

    @ConfigComment("This section defines how TreeFeller searches for all the blocks in a tree.")
    public DetectionConfiguration detection = new DetectionConfiguration();

    @ConfigComment("This section defines how TreeFeller decides if a given detection is a tree or not.")
    public CriteriaConfiguration criteria = new CriteriaConfiguration();

    @ConfigComment("This section defines how a tree is cut")
    public CuttingConfiguration cutting = new CuttingConfiguration();

    @ConfigComment("This section defines how blocks are broken")
    public BreakingConfiguration breaking = new BreakingConfiguration();

    @ConfigComment("This section defines the results after felling a tree")
    public ResultConfiguration result = new ResultConfiguration();
    
    public GlobalConfiguration(){
        for(Field field : getClass().getFields()){
            try{
                applyGlobalConfiguration(field.getType(), field.get(this));
            }catch(Exception ex){
                throw new RuntimeException("Could not apply global config defaults!");
            }
        }
    }
    private void applyGlobalConfiguration(Class<?> type, Object object) throws Exception{
        for(Field field : type.getFields()){
            Class<?> fieldType = field.getType();
            if(fieldType==Boolean.class){
                ConfigGlobalDefaultBoolean annotation = field.getAnnotation(ConfigGlobalDefaultBoolean.class);
                if(annotation!=null)field.set(object, annotation.value());
            }
            if(fieldType==FallDirection.class){
                ConfigGlobalDefaultFallDirection annotation = field.getAnnotation(ConfigGlobalDefaultFallDirection.class);
                if(annotation!=null)field.set(object, annotation.value());
            }
            if(fieldType==Float.class){
                ConfigGlobalDefaultFloat annotation = field.getAnnotation(ConfigGlobalDefaultFloat.class);
                if(annotation!=null)field.set(object, annotation.value());
            }
            if(fieldType==Integer.class){
                ConfigGlobalDefaultInteger annotation = field.getAnnotation(ConfigGlobalDefaultInteger.class);
                if(annotation!=null)field.set(object, annotation.value());
            }
            ConfigGlobalDefaultValue annotation = field.getAnnotation(ConfigGlobalDefaultValue.class);
            if(annotation!=null){
                if(field.getType()==String.class)field.set(object, annotation.value());
                else{
                    field.set(object, field.getType().getConstructor(String.class).newInstance(annotation.value()));
                }
            }
            if(field.getAnnotation(ConfigGlobalDefaultNewInstance.class)!=null){
                Object obj = field.getType().getConstructor().newInstance();
                applyGlobalConfiguration(field.getType(), obj);
                field.set(object, obj);
            }
        }
    }
}
