package com.thizthizzydizzy.treefeller.core.control;
import com.thizthizzydizzy.treefeller.core.TreeFellerCore;
import com.thizthizzydizzy.treefeller.core.config.ConfigReader;
import com.thizthizzydizzy.treefeller.core.config.ConfigWriter;
import com.thizthizzydizzy.treefeller.core.config.structure.TreeFellerConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.message.SingleMessageConfiguration;
import com.thizthizzydizzy.treefeller.core.message.TreeFellerMessages;
import com.thizthizzydizzy.treefeller.core.player.PlayerSettings;
import com.thizthizzydizzy.treefeller.core.debug.TreeFellerDebugger;
import java.io.IOException;
import java.nio.file.*;
/** Public, transport-independent actions. Call on the server thread from any UI or integration. */
public final class TreeFellerControls{
    private TreeFellerControls(){}
    public static boolean permitted(ControlAudience source,String capability){
        if(source.hasPermission("treefeller."+capability))return true;
        message(source,TreeFellerCore.config.messages.no_permission);
        return false;
    }
    public static void message(ControlAudience source,SingleMessageConfiguration message){
        if(message!=null&&(message.chat!=null||message.actionbar!=null)){
            if(source.player()!=null)TreeFellerMessages.send(source.player(),message);
            else if(message.chat!=null)source.sendMessage(message.chat);
        }
    }
    public static boolean requirePlayer(ControlAudience source){
        if(source.player()!=null)return true;
        source.sendMessage("This action requires a player.");return false;
    }
    /** Null reverses the player's current state. */
    public static void toggle(ControlAudience source,Boolean enabled){
        if(!permitted(source,"toggle")||!requirePlayer(source))return;
        PlayerSettings settings=PlayerSettings.get(source.player());
        boolean next=enabled==null?!settings.isToggledOn():enabled;
        settings.setToggledOn(next);
        message(source,next?TreeFellerCore.config.messages.toggle_on:TreeFellerCore.config.messages.toggle_off);
    }
    /** Toggle the player-facing debug channel; other diagnostic channels retain their settings. */
    public static void debug(ControlAudience source,Boolean enabled){
        if(!permitted(source,"debug"))return;
        boolean next=enabled==null?!TreeFellerCore.config.debug.chat:enabled;
        TreeFellerCore.config.debug.chat=next;
        source.sendMessage("TreeFeller debug chat "+(next?"enabled":"disabled")+".");
    }
    public static void reload(ControlAudience source){
        if(!permitted(source,"reload"))return;
        try{
            TreeFellerConfiguration replacement=TreeFellerCore.connector.loadConfig(ConfigReader::read);
            if(replacement==null)throw new IllegalStateException("No configuration was loaded");
            TreeFellerDebugger.clearAll();
            TreeFellerCore.config=replacement;
            message(source,replacement.messages.reload);
        }catch(RuntimeException ex){source.sendMessage("Could not reload TreeFeller: "+ex.getMessage());}
    }
    public static void save(ControlAudience source){
        if(!permitted(source,"config"))return;
        Path path=TreeFellerCore.connector.configurationPath(),temporary=null;
        try{
            Path parent=path.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            temporary=Files.createTempFile(parent,"treefeller-", ".conf");
            ConfigWriter.write(temporary,TreeFellerCore.config);
            try{Files.move(temporary,path,StandardCopyOption.ATOMIC_MOVE,StandardCopyOption.REPLACE_EXISTING);}
            catch(AtomicMoveNotSupportedException ex){Files.move(temporary,path,StandardCopyOption.REPLACE_EXISTING);}
            source.sendMessage("TreeFeller configuration saved.");
        }catch(IOException|RuntimeException ex){source.sendMessage("Could not save TreeFeller: "+ex.getMessage());}
        finally{if(temporary!=null)try{Files.deleteIfExists(temporary);}catch(IOException ex){TreeFellerCore.connector.log(ex.toString());}}
    }
}
