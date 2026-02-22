package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.coremod.version.v1_12_2;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import net.minecraft.launchwrapper.LaunchClassLoader;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
@IFMLLoadingPlugin.Name("Tree Feller Bootstrapper")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class ForgeCoremodBootstrapperV1_12_2 implements IFMLLoadingPlugin{
    private File modsDir;
    public ForgeCoremodBootstrapperV1_12_2(){
        try{
            InputStream is = getClass().getResourceAsStream("/META-INF/jars/TreeFellerPlatformForgeVersionV1_12_2.jar");
            Path tempDir = Files.createDirectories(new File(modsDir, "treefeller").toPath());
            Path extractedJar = tempDir.resolve("TreeFellerPlatformForge.jar");
            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);

            LaunchClassLoader classLoader = (LaunchClassLoader)getClass().getClassLoader();
            classLoader.addURL(extractedJar.toFile().toURI().toURL());

            System.out.println("Successfully injected Forge Platform.");
        }catch(IOException e){
            throw new RuntimeException("Failed to inject Forge Platform.");
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
        modsDir = new File((File)data.get("mcLocation"), "mods");
    }
    @Override
    public String getAccessTransformerClass(){
        return null;
    }
}
