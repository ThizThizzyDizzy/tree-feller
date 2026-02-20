package com.thizthizzydizzy.treefeller.forge.bootstrap.reforged;
import cpw.mods.modlauncher.api.IEnvironment;
import cpw.mods.modlauncher.api.ITransformationService;
import cpw.mods.modlauncher.api.ITransformer;
import cpw.mods.modlauncher.api.IncompatibleEnvironmentException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.Set;
public class Bootstrapper implements ITransformationService{
    public static Path treeFellerPath;
    @Override
    public String name(){
        return "treefeller-bootstrapper";
    }
    @Override
    public void initialize(IEnvironment environment){
        try{
            InputStream is = getClass().getResourceAsStream("/META-INF/jars/TreeFellerForgeReforgedPlatform.jar");

            Path gameDir = environment.getProperty(IEnvironment.Keys.GAMEDIR.get()).orElse(Paths.get("."));
            Path tempDir = Files.createDirectories(gameDir.resolve("mods/treefeller"));
            Path extractedJar = tempDir.resolve("TreeFellerForgeReforgedPlatform.jar");

            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);
            treeFellerPath = extractedJar.toAbsolutePath();
            System.out.println("Extracted Forge Reforged Platform.");
        }catch(IOException e){
            throw new RuntimeException("Failed to initialize bootstrapper", e);
        }
    }
    @Override
    public void onLoad(IEnvironment ie, Set<String> set) throws IncompatibleEnvironmentException{
    }
    @Override
    public List<ITransformer> transformers(){
        return Collections.emptyList();
    }

}
