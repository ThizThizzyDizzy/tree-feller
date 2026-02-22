package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.version.v1_15_2;
import com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.core.IForgeSPIBootstrapper;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
import net.minecraftforge.forgespi.locating.IModFile;
public class ForgeSPIBootstrapperV1_15_2 extends AbstractJarFileLocator implements IForgeSPIBootstrapper{
    @Override
    public List scanModFile(Path path){
        ModFile modFile = ModFile.newFMLInstance(path, this);
        modJars.put(modFile, createFileSystem(modFile));
        return Arrays.asList(modFile);
    }
    @Override
    public List<IModFile> scanMods(){
        return null;
    }
    @Override
    public String name(){
        return null;
    }
    @Override
    public void initArguments(Map<String, ?> map){
    }
}
