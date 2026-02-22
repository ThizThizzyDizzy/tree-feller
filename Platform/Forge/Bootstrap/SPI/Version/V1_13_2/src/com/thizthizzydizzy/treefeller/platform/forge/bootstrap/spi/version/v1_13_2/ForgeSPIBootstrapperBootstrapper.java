package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.version.v1_13_2;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
public class ForgeSPIBootstrapperBootstrapper implements ITransformationService{
    public String name(){
        return "treefeller-bootstrap-bootstrap";
    }
    public void initialize(IEnvironment ie){
    }
    public void beginScanning(IEnvironment ie){
    }
    public void onLoad(IEnvironment ie, Set<String> set) throws IncompatibleEnvironmentException{
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        getClass().getClassLoader();
    }
    public List transformers(){
        return Collections.emptyList();
    }
}
