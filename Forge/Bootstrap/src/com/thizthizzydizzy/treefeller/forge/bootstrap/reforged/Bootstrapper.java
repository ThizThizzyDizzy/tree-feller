package com.thizthizzydizzy.treefeller.forge.bootstrap.reforged;

import cpw.mods.jarhandling.SecureJar;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Stream;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.forgespi.locating.IModFile;
import net.minecraftforge.forgespi.locating.IModLocator;

public class Bootstrapper extends AbstractModProvider implements IModLocator{
    private static boolean legacyLocator = false;
    @Override
    public List scanMods(){
//        try{
//            Class<?> mcpVersionClass = Class.forName("net.minecraftforge.versions.mcp.MCPVersion");
//            Method getMCVersion = mcpVersionClass.getDeclaredMethod("getMCVersion");
//            version = (String)getMCVersion.invoke(null);
//        }catch(Exception e){
            String version = FMLLoader.versionInfo().mcVersion();
//            
//            
//            System.out.println("Failed to get minecraft version from MCPVersion ("+e.getClass().getName()+" "+e.getMessage()+") Attempting to fall back to ForgeVersion.");
//            try{
//                Class<?> forgeVersionClass = Class.forName("net.minecraftforge.common.ForgeVersion");
//                Field mcVersionField = forgeVersionClass.getField("mcVersion");
//                version = (String)mcVersionField.get(null);
//            }catch(Exception ex){
//                System.out.println("Failed to get minecraft version from ForgeVersion ("+ex.getClass().getName()+" "+ex.getMessage()+") Attempting to extract from SharedConstants.");
//
//                try{
//                    // Don't want to deal with obfuscation maps here, look for ANY field that looks like a version number.
//                    Class<?> sharedConstantsClass = Class.forName("net.minecraft.SharedConstants");
//                    for(Field field : sharedConstantsClass.getDeclaredFields()){
//                        if(field.getType()==String.class&&field.getModifiers()==(Modifier.PUBLIC|Modifier.STATIC|Modifier.FINAL)){
//                            // This looks like a constant! Hey, maybe it's the version! Time to pull it and find out!
//                            String possibleVersion = (String)field.get(null);
//                            if(possibleVersion.matches("1\\.\\d\\d?\\.?\\d*")){
//                                // That looks like a minecraft version to me!
//                                version = possibleVersion;
//                                break;
//                            }
//                        }
//                    }
//                }catch(Exception exc){
//                    throw new RuntimeException("Failed to get minecraft version from SharedConstants!", exc);
//                }
//
//                if(version==null)
//                    throw new RuntimeException("Could not find minecraft version!");
//            }
//        }
        System.out.println("Identified Minecraft version as "+version);
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
                targetJarfile = "TreeFellerForgePlatform1_21_5.jar";
                break;
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
                legacyLocator = true;
                break;
        }
        Path treeFellerPath;
        try{
            InputStream is = getClass().getResourceAsStream("/META-INF/jars/"+targetJarfile);
            if(is==null)
                throw new RuntimeException("Could not find internal jar file "+targetJarfile+" for MC version "+version+"!");

            Path tempDir = Files.createDirectories(FMLPaths.GAMEDIR.get().resolve("mods/treefeller"));
            Path extractedJar = tempDir.resolve("TreeFellerForgePlatform.jar");

            Files.copy(is, extractedJar, StandardCopyOption.REPLACE_EXISTING);
            treeFellerPath = extractedJar.toAbsolutePath();
            System.out.println("Extracted Forge Platform: "+targetJarfile);
        }catch(IOException e){
            throw new RuntimeException("Failed to initialize bootstrapper", e);
        }

        ModFileOrException mod;
        try{
            if(legacyLocator){
                Method method = getClass().getSuperclass().getDeclaredMethod("createMod", Path[].class);
                method.setAccessible(true);
                mod = (ModFileOrException)method.invoke(this, (Object)new Path[]{treeFellerPath});
            }else{
                Method method = getClass().getSuperclass().getDeclaredMethod("createMod", Path.class);
                method.setAccessible(true);
                mod = (ModFileOrException)method.invoke(this, treeFellerPath);
            }
        }catch(Exception e){
            throw new RuntimeException("Failed to call createMod", e);
        }
        System.out.println("Created mod, sending off to Forge.");
        return Arrays.asList(mod);
    }
    @Override
    public String name(){
        return "treefeller-bootstrapper";
    }
    public void initArguments(Map map){
    }
    public void scanFile(IModFile file, Consumer<Path> pathConsumer){
        if(!legacyLocator){
            try{
                MethodHandles.lookup()
                    .findSpecial(AbstractModProvider.class, "scanFile", MethodType.methodType(void.class, IModFile.class, Consumer.class), Bootstrapper.class)
                    .invoke(this, file, pathConsumer);
            }catch(Throwable t){
                throw new RuntimeException("Failed to call scanFile", t);
            }
        }else{
            final Function<Path, SecureJar.Status> status = p->file.getSecureJar().verifyPath(p);
            try (Stream<Path> files = Files.find(file.getSecureJar().getRootPath(), Integer.MAX_VALUE, (p, a) -> p.getNameCount() > 0 && p.getFileName().toString().endsWith(".class"))) {
                file.setSecurityStatus(files.peek(pathConsumer).map(status).reduce((s1, s2)-> SecureJar.Status.values()[Math.min(s1.ordinal(), s2.ordinal())]).orElse(SecureJar.Status.INVALID));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
