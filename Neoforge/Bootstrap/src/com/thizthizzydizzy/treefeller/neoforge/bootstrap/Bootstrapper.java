package com.thizthizzydizzy.treefeller.neoforge.bootstrap;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Stream;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.fml.loading.moddiscovery.AbstractJarFileModLocator;

public class Bootstrapper extends AbstractJarFileModLocator{
    @Override
    public Stream<Path> scanCandidates(){
        Path treeFellerPath;
        try{
            InputStream is = getClass().getResourceAsStream("/META-INF/jars/TreeFellerNeoforgePlatform1_20_3.jar");

            Path tempDir = Files.createDirectories(FMLPaths.GAMEDIR.get().resolve("mods/treefeller"));
            Path extractedJar = tempDir.resolve("TreeFellerNeoforgePlatform1_20_3.jar");

            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);
            treeFellerPath = extractedJar.toAbsolutePath();
            System.out.println("Extracted Neoforge 1.20.3 Platform.");
        }catch(IOException e){
            throw new RuntimeException("Failed to initialize bootstrapper", e);
        }
        return Arrays.asList(treeFellerPath).stream();
    }
    @Override
    public String name(){
        return "treefeller-bootstrapper";
    }
    @Override
    public void initArguments(Map<String, ?> map){
    }
}
