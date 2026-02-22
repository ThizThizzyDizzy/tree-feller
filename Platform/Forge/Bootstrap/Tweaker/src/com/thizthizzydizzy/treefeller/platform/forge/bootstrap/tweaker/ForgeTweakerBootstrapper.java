package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.tweaker;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;
import net.minecraft.launchwrapper.ITweaker;
import net.minecraft.launchwrapper.LaunchClassLoader;
public class ForgeTweakerBootstrapper implements ITweaker{
    private File modsDir;
    @Override
    public void acceptOptions(List<String> args, File gameDir, File assetsDir, String profile){
        modsDir = new File(gameDir, "mods");
    }
    @Override
    public void injectIntoClassLoader(LaunchClassLoader classLoader){
        try{
            String platformName;
            try {
                Class.forName("net.minecraftforge.fml.relauncher.IFMLLoadingPlugin", false, this.getClass().getClassLoader());
                platformName = "TreeFellerPlatformForgeVersionV1_12_2.jar";
            } catch (ClassNotFoundException e) {
                platformName = "TreeFellerPlatformForgeVersionV1_7_10.jar";
            }

            System.out.println("Selected Forge Platform: "+platformName);

            InputStream is = getClass().getResourceAsStream("/META-INF/jars/"+platformName);
            Path tempDir = Files.createDirectories(new File(modsDir, "treefeller").toPath());
            Path extractedJar = tempDir.resolve("TreeFellerPlatformForge.jar");
            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);

            classLoader.addURL(extractedJar.toFile().toURI().toURL());

            System.out.println("Successfully injected Forge Platform.");
        }catch(IOException e){
            throw new RuntimeException("Failed to inject Forge Platform.");
        }
    }
    @Override
    public String getLaunchTarget(){
        return null;
    }
    @Override
    public String[] getLaunchArguments(){
        return new String[0];
    }
}
