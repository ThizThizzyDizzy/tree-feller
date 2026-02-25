package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.transformer;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipFile;
public class ForgeSPIBootstrapperBootstrapper implements ITransformationService{
    static{
        unblockFromModsFolderLocator();
    }
    private static boolean unblockSuccessed = false;
    private static void unblockFromModsFolderLocator(){
    }
    public String name(){
        return "treefeller-bootstrap-bootstrap";
    }
    public void initialize(IEnvironment ie){
    }
    public void beginScanning(IEnvironment ie){
    }
    public void onLoad(IEnvironment ie, Set<String> set) throws IncompatibleEnvironmentException{

        boolean is1_13 = false;
        try{
            // Find the mod dir discoverer
            Class<?> discovererClass = Class.forName("net.minecraftforge.fml.loading.ModDirTransformerDiscoverer");
            
            // the 1.13 one has no fields and 3 methods
            is1_13 = discovererClass.getDeclaredFields().length==0 && discovererClass.getDeclaredMethods().length == 3;

            // Find the transformers and locators lists
            java.lang.reflect.Field transformersField = discovererClass.getDeclaredField("transformers");
            transformersField.setAccessible(true);

            java.lang.reflect.Field locatorsField = discovererClass.getDeclaredField("locators");
            locatorsField.setAccessible(true);

            java.util.List<java.nio.file.Path> transformers = (java.util.List<java.nio.file.Path>)transformersField.get(null);
            java.util.List<java.nio.file.Path> locators = (java.util.List<java.nio.file.Path>)locatorsField.get(null);

            // Find this mod in the transformers list
            for(Iterator<Path> it = transformers.iterator(); it.hasNext();){
                Path path = it.next();

                try(ZipFile zipFile = new ZipFile(path.toFile())){
                    if(zipFile.stream().anyMatch(entry -> entry.getName().startsWith("com/thizthizzydizzy/treefeller/core"))){
                        // Kindly let forge know that this is actually a locator, and not a transformer
                        locators.add(path);
                        it.remove();
                    }
                }catch(IOException e){
                }
            }
            return;
        }catch(Exception e){
            if(!is1_13)return; // 1.17+, don't need to do anything special
        }

        //1.13 bootstrapper bootstrapper
        Thread.currentThread().setContextClassLoader(getClass().getClassLoader());
        getClass().getClassLoader();
    }
    public List transformers(){
        return Collections.emptyList();
    }
}
