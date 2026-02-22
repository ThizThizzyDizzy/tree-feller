package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.version.v1_17_1;
import com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.core.IForgeSPIBootstrapper;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.forgespi.locating.IModFile;
public class ForgeSPIBootstrapperV1_17_1 extends AbstractJarFileLocator implements IForgeSPIBootstrapper{
    private Path modPath;
    @Override
    public List scanModFile(Path path){
        modPath = path;
        return scanMods();
    }
    @Override
    public Stream<Path> scanCandidates(){
        return Stream.of(modPath);
    }
    @Override
    public String name(){
        return null;
    }
    @Override
    public void initArguments(Map<String, ?> map){
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
