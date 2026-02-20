package com.thizthizzydizzy.treefeller.forge.bootstrap.reforged;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.forgespi.locating.IModLocator;
import net.minecraftforge.versions.mcp.MCPVersion;

public class Bootstrapper extends AbstractModProvider implements IModLocator{
    @Override
    public List scanMods(){
        String version = MCPVersion.getMCVersion();
        String targetJarfile;
        switch(version){
            default:
            case "1.21.11":
            case "1.21.10":
            case "1.21.9":
            case "1.21.8":
            case "1.21.7":
            case "1.21.6":
                targetJarfile = "TreeFellerForgeReforgedPlatform.jar";
                break;
            case "1.21.5":
            case "1.21.4":
            case "1.21.3":
            case "1.21.2":
            case "1.21.1":
            case "1.21":
            case "1.20.6":
            case "1.20.5":
            case "1.20.4":
            case "1.20.3":
            case "1.20.2":
            case "1.20.1":
            case "1.20":
            case "1.19.4":
            case "1.19.3":
            case "1.19.2":
            case "1.19.1":
            case "1.19":
            case "1.18.2":
            case "1.18.1":
            case "1.18":
            case "1.17.1":
            case "1.17":
            case "1.16.5":
            case "1.16.4":
            case "1.16.3":
            case "1.16.2":
            case "1.16.1":
            case "1.16":
            case "1.15.2":
            case "1.15.1":
            case "1.15":
            case "1.14.4":
            case "1.14.3":
            case "1.14.2":
            case "1.14.1":
            case "1.14":
            case "1.13.2":
            case "1.13.1":
            case "1.13":
            case "1.12.2":
            case "1.12.1":
            case "1.12":
            case "1.11.2":
            case "1.11.1":
            case "1.11":
            case "1.10.2":
            case "1.10.1":
            case "1.10":
            case "1.9.4":
            case "1.9.3":
            case "1.9.2":
            case "1.9.1":
            case "1.9":
            case "1.8.9":
            case "1.8.8":
            case "1.8.7":
            case "1.8.6":
            case "1.8.5":
            case "1.8.4":
            case "1.8.3":
            case "1.8.2":
            case "1.8.1":
            case "1.8":
            case "1.7.10":
                targetJarfile = "TreeFellerForgePlatform1_21_5.jar";
                break;
        }
        Path treeFellerPath;
        try{
            InputStream is = getClass().getResourceAsStream("/META-INF/jars/"+targetJarfile);
            if(is==null)throw new RuntimeException("Could not find internal jar file "+targetJarfile+" for MC version "+version+"!");

            Path tempDir = Files.createDirectories(FMLPaths.GAMEDIR.get().resolve("mods/treefeller"));
            Path extractedJar = tempDir.resolve("TreeFellerForgePlatform.jar");

            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);
            treeFellerPath = extractedJar.toAbsolutePath();
            System.out.println("Extracted Forge Platform.");
        }catch(IOException e){
            throw new RuntimeException("Failed to initialize bootstrapper", e);
        }

        return Arrays.asList(createMod(treeFellerPath));
    }
    @Override
    public String name(){
        return "treefeller-bootstrapper";
    }
}
