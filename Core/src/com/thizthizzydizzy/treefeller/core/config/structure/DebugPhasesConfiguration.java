package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.debug.DebugPhase;
public class DebugPhasesConfiguration {
    public DebugPhaseConfiguration trigger = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration detection = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration criteria = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration cutting = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration breaking = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration result = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration replant = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration effects = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration cascade = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration events = new DebugPhaseConfiguration();
    public DebugPhaseConfiguration get(DebugPhase phase){
        try{return (DebugPhaseConfiguration)getClass().getField(phase.name().toLowerCase(java.util.Locale.ROOT)).get(this);}
        catch(ReflectiveOperationException ex){throw new IllegalStateException(ex);}
    }
}
