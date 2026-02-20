package com.thizthizzydizzy.treefeller.forge.bootstrap.reforged;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.forgespi.locating.IModLocator;

public class Bootstrapper extends AbstractModProvider implements IModLocator{
    @Override
    public List<ModFileOrException> scanMods(){
        Path treeFellerPath;
        try{
            InputStream is = getClass().getResourceAsStream("/META-INF/jars/TreeFellerForgeReforgedPlatform.jar");

            Path tempDir = Files.createDirectories(FMLPaths.GAMEDIR.get().resolve("mods/treefeller"));
            Path extractedJar = tempDir.resolve("TreeFellerForgeReforgedPlatform.jar");

            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);
            treeFellerPath = extractedJar.toAbsolutePath();
            System.out.println("Extracted Forge Reforged Platform.");
        }catch(IOException e){
            throw new RuntimeException("Failed to initialize bootstrapper", e);
        }
        
        ArrayList<IModLocator.ModFileOrException> mods = new ArrayList<>();
        IModLocator.ModFileOrException mod = createMod(treeFellerPath, true);
        mods.add(mod);
        return mods;
    }
    @Override
    public String name(){
        return "treefeller-bootstrapper";
    }
}
