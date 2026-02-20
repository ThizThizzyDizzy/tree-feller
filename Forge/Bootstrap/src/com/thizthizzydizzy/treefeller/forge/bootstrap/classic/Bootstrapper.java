package com.thizthizzydizzy.treefeller.forge.bootstrap.classic;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.FMLInjectionData;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
@IFMLLoadingPlugin.Name("Tree Feller Loader")
@IFMLLoadingPlugin.MCVersion("1.12.2")
@IFMLLoadingPlugin.TransformerExclusions("com.thizthizzydizzy.treefeller.forge.bootstrap")
public class Bootstrapper implements IFMLLoadingPlugin{
    public Bootstrapper(){
        try {
            String mcVersion = (String) FMLInjectionData.data()[1];

            InputStream is = getClass().getResourceAsStream("/META-INF/jars/TreeFellerForgeClassicPlatform.jar");
            File tempJar = File.createTempFile("treefeller_extract", ".jar");
            Files.copy(is, tempJar.toPath(), StandardCopyOption.REPLACE_EXISTING);

            LaunchClassLoader classLoader = (LaunchClassLoader) getClass().getClassLoader();
            classLoader.addURL(tempJar.toURI().toURL());
            
            System.out.println("Successfully injected Forge Classic Platform.");
        } catch (IOException e) {
            throw new RuntimeException("Failed to inject Forge Classic Platform.");
        }
    }
    
    @Override
    public String[] getASMTransformerClass(){
        return null;
    }
    @Override
    public String getModContainerClass(){
        return null;
    }
    @Override
    public String getSetupClass(){
        return null;
    }
    @Override
    public void injectData(Map<String, Object> data){
    }
    @Override
    public String getAccessTransformerClass(){
        return null;
    }
}
