package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.version.v1_19;
import com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.core.IForgeSPIBootstrapper;
import cpw.mods.jarhandling.SecureJar;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.forgespi.locating.IModFile;
public class ForgeSPIBootstrapperV1_19 extends AbstractModProvider implements IForgeSPIBootstrapper{
    @Override
    public List scanModFile(Path path){
        return Arrays.asList(createMod(path));
    }
    @Override
    public String name(){
        return null;
    }
    @Override
    public void scanFile(IModFile imf, Consumer<Path> pathConsumer){
        final Function<Path, SecureJar.Status> status = p -> imf.getSecureJar().verifyPath(p);
        try(Stream<Path> files = Files.find(imf.getSecureJar().getRootPath(), Integer.MAX_VALUE, (p, a) -> p.getNameCount()>0&&p.getFileName().toString().endsWith(".class"))){
            imf.setSecurityStatus(files.peek(pathConsumer).map(status).reduce((s1, s2) -> SecureJar.Status.values()[Math.min(s1.ordinal(), s2.ordinal())]).orElse(SecureJar.Status.INVALID));
        }catch(IOException e){
            throw new RuntimeException(e);
        }
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
