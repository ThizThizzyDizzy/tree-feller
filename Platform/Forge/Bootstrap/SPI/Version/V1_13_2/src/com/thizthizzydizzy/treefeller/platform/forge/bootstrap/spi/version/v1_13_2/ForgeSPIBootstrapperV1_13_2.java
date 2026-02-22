package com.thizthizzydizzy.treefeller.platform.forge.bootstrap.spi.version.v1_13_2;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import java.util.zip.ZipFile;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.AbstractJarFileLocator;
import net.minecraftforge.fml.loading.moddiscovery.IModLocator;
import net.minecraftforge.fml.loading.moddiscovery.ModFile;
public class ForgeSPIBootstrapperV1_13_2 extends AbstractJarFileLocator implements IModLocator{
    @Override
    public List<ModFile> scanMods(){
        Path treeFellerPath;
        try{
            InputStream is = getClass().getResourceAsStream("/META-INF/jars/TreeFellerPlatformForgeVersionV1_21_5.jar");
            if(is==null)
                throw new RuntimeException("Could not find platform jarfile in /META-INF/jars!");

            Path modsDir = FMLPaths.GAMEDIR.get().resolve("mods");
            Path tempDir = Files.createDirectories(modsDir.resolve("treefeller"));
            Path extractedJar = tempDir.resolve("TreeFellerPlatformForge.jar");

            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);

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

            treeFellerPath = extractedJar.toAbsolutePath();
            System.out.println("Extracted Forge Platform");
        }catch(IOException e){
            throw new RuntimeException("Failed to initialize bootstrapper", e);
        }
        ModFile modFile = new ModFile(treeFellerPath, this);
        modJars.put(modFile, createFileSystem(modFile));
        return Arrays.asList(modFile);
    }
    @Override
    public String name(){
        return "treefeller-bootstrap";
    }
    @Override
    public void initArguments(Map<String, ?> map){
    }
}
