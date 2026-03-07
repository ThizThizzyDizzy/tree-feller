package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.core;
import com.thizthizzydizzy.treefeller.platform.utility.version.VersionMatcher;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.jar.Manifest;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;
public class ForgeSPIBootstrapper implements IModLocator{
    private IForgeSPIBootstrapper bootstrapper;
    private Map<String, ?> initArguments;
    @Override
    public List scanMods(){
        System.out.println("Fetching MC Version...");
        String version = null;
        List<VersionFetcher> versionFetchers = new ArrayList<>();
        versionFetchers.add(() -> {
            System.out.println("Attempting to fetch MC Version via MCPVersion");
            Class<?> mcpVersionClass = Class.forName("net.minecraftforge.versions.mcp.MCPVersion");
            Method getMCVersion = mcpVersionClass.getDeclaredMethod("getMCVersion");
            return (String)getMCVersion.invoke(null);
        });
        versionFetchers.add(() -> {
            System.out.println("Attempting to fetch MC Version via FMLLoader");
            Class<?> fmlLoaderClass = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
            Object versionInfo = fmlLoaderClass.getDeclaredMethod("versionInfo").invoke(null);
            Class<?> versionInfoClass = Class.forName("net.minecraftforge.fml.loading.VersionInfo");
            return (String)versionInfoClass.getDeclaredMethod("mcVersion").invoke(versionInfo);
        });
        versionFetchers.add(() -> {
            System.out.println("Attempting to exfiltrate MC Version from FMLLoader");
            Class<?> fmlLoaderClass = Class.forName("net.minecraftforge.fml.loading.FMLLoader");
            Field versionField = fmlLoaderClass.getDeclaredField("mcVersion");
            versionField.setAccessible(true);
            return (String)versionField.get(null);
        });
        for(VersionFetcher fetcher : versionFetchers){
            try{
                version = fetcher.fetchMCVersion();
                break;
            }catch(Exception ex){
                System.out.println("Failed to fetch version - "+ex.getClass().getName()+": "+ex.getMessage());
            }
        }
        if(version==null)
            throw new RuntimeException("Could not fetch minecraft version!");
        System.out.println("Identified Minecraft version as "+version);

        String bootstrapperName = VersionMatcher.by(VersionMatcher.VersionType.MINECRAFT).ascending((String)null)
            .atVersion("1.14.4", "v1_14_4.ForgeSPIBootstrapperV1_14_4")
            .atVersion("1.15.2", "v1_15_2.ForgeSPIBootstrapperV1_15_2")
            .atVersion("1.17.1", "v1_17_1.ForgeSPIBootstrapperV1_17_1")
            .atVersion("1.18.2", "v1_18_2.ForgeSPIBootstrapperV1_18_2")
            .atVersion("1.19", "v1_19.ForgeSPIBootstrapperV1_19")
            .atVersion("1.20.3", "v1_20_3.ForgeSPIBootstrapperV1_20_3")
            .match(version);
        if(bootstrapperName==null)
            throw new RuntimeException("MC Version not supported: "+version);
        System.out.println("Loading bootstrapper: "+bootstrapperName);
        try{
            bootstrapper = (IForgeSPIBootstrapper)Class.forName("com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.version."+bootstrapperName)
                .getDeclaredConstructor()
                .newInstance();
        }catch(Exception ex){
            throw new RuntimeException("Could not initialize bootstrapper!");
        }

        bootstrapper.initArguments(initArguments);

        String platformName = VersionMatcher.by(VersionMatcher.VersionType.MINECRAFT).ascending((String)null)
            .atVersion("1.14.4", "TreeFellerPlatformForgeVersionV1_21_5.jar")
            .atVersion("1.21.6", "TreeFellerPlatformForgeVersionV1_21_11.jar")
            .match(version);

        System.out.println("Selected Forge Platform: "+platformName);

        boolean shouldCopyCore = VersionMatcher.by(VersionMatcher.VersionType.MINECRAFT).ascending(false)
            .atVersion("1.14.4", true)
            .atVersion("1.17", false)
            .match(version);

        Path treeFellerPath;
        try{
            InputStream is = getClass().getResourceAsStream("/META-INF/jars/"+platformName);
            if(is==null)
                throw new RuntimeException("Could not find platform jarfile in /META-INF/jars!");

            Path modsDir = FMLPaths.GAMEDIR.get().resolve("mods");
            Path tempDir = Files.createDirectories(modsDir.resolve("treefeller"));
            Path extractedJar = tempDir.resolve("TreeFellerPlatformForge.jar");

            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);

            if(shouldCopyCore){
                Path sourceJar;
                System.out.println("Finding TreeFeller core...");
                try(Stream<Path> s = Files.walk(modsDir, 1)){
                    sourceJar = s.filter(p -> p.toString().endsWith(".jar"))
                        .filter(p -> {
                            try(ZipFile zipFile = new ZipFile(p.toFile())){
                                return zipFile.stream().anyMatch(entry -> entry.getName().startsWith("com/thizthizzydizzy/treefeller/core"));
                            }catch(IOException e){
                                return false;
                            }
                        })
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Could not find main TreeFeller jar file!"));
                }

                System.out.println("Copying TreeFeller core files from "+sourceJar.getFileName());
                try(FileSystem srcFs = FileSystems.newFileSystem(sourceJar, (ClassLoader)null); FileSystem destFs = FileSystems.newFileSystem(extractedJar, (ClassLoader)null)){

                    Path root = srcFs.getPath("/com/thizthizzydizzy/treefeller/core");
                    Files.walk(root).forEach(sourcePath -> {
                        try{
                            Path relativePath = srcFs.getPath("/").relativize(sourcePath);
                            Path destPath = destFs.getPath(relativePath.toString());

                            if(Files.isDirectory(sourcePath)){
                                Files.createDirectories(destPath);
                            }else{
                                Files.copy(sourcePath, destPath, StandardCopyOption.REPLACE_EXISTING);
                            }
                        }catch(IOException e){
                            throw new RuntimeException("Failed to copy class: "+sourcePath, e);
                        }
                    });
                }
            }

            treeFellerPath = extractedJar.toAbsolutePath();
            System.out.println("Extracted Forge Platform");
        }catch(IOException e){
            throw new RuntimeException("Failed to initialize bootstrapper", e);
        }

        return bootstrapper.scanModFile(treeFellerPath);
    }
    @Override
    public String name(){
        return "treefeller-bootstrap";
    }
    public Path findPath(IModFile imf, String... strings){
        return bootstrapper.findPath(imf, strings);
    }
    @Override
    public void scanFile(IModFile imf, Consumer<Path> cnsmr){
        bootstrapper.scanFile(imf, cnsmr);
    }
    public Optional<Manifest> findManifest(Path path){
        return bootstrapper.findManifest(path);
    }
    @Override
    public void initArguments(Map<String, ?> map){
        this.initArguments = map;
    }
    @Override
    public boolean isValid(IModFile imf){
        return bootstrapper.isValid(imf);
    }
    private static interface VersionFetcher{
        public String fetchMCVersion() throws Exception;
    }

}
