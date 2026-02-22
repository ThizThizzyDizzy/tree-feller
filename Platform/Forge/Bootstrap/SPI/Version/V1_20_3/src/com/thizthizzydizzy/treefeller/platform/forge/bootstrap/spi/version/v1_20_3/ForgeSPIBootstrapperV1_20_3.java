package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.version.v1_20_3;
import com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.core.IForgeSPIBootstrapper;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.jar.Manifest;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator.ModFileOrException;
public class ForgeSPIBootstrapperV1_20_3 extends AbstractModProvider implements IForgeSPIBootstrapper{
    @Override
    public List<ModFileOrException> scanModFile(Path path){
        return Arrays.asList(createMod(path));
    }
    @Override
    public String name(){
        return null;
    }
    @Override
    public Path findPath(IModFile imf, String... strings){
        return null;
    }
    @Override
    public Optional<Manifest> findManifest(Path path){
        return null;
    }
}
