package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.core;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;
import net.minecraftforge.forgespi.locating.IModFile;
public interface IForgeSPIBootstrapper{
    public List scanModFile(Path path);
    public Path findPath(IModFile imf, String... strings);
    public void scanFile(IModFile imf, Consumer<Path> pathConsumer);
    public Optional<Manifest> findManifest(Path path);
    public void initArguments(Map<String, ?> map);
    public boolean isValid(IModFile imf);
}
