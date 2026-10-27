package com.thizthizzydizzy.treefeller.platform.bukkit.core.connector;
import com.thizthizzydizzy.treefeller.core.control.*;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
import com.thizthizzydizzy.treefeller.platform.bukkit.core.TreeFellerBukkit;
import java.util.*;
import java.util.function.Consumer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.ItemMeta;
/** Bukkit rendering and event translation only. */
public final class BukkitControlAudience implements ControlAudience{
    private final TreeFellerBukkit plugin;
    private final CommandSender sender;
    private final Renderer renderer;
    public BukkitControlAudience(TreeFellerBukkit plugin,CommandSender sender,Renderer renderer){this.plugin=plugin;this.sender=sender;this.renderer=renderer;}
    @Override public boolean hasPermission(String permission){return sender.hasPermission(permission);}
    @Override public void sendMessage(String message){sender.sendMessage(message);}
    @Override public IPlayerConnector player(){return sender instanceof Player?plugin.getPlayerConnector((Player)sender):null;}
    @Override public void show(Screen screen){if(sender instanceof Player)renderer.show((Player)sender,screen,0);}
    @Override public void requestText(String title,String initialValue,Consumer<String> submit,Runnable cancel){
        if(!(sender instanceof Player))return;
        Player player=(Player)sender;
        player.closeInventory();
        renderer.input.put(player.getUniqueId(),new Input(submit,cancel));
        player.sendMessage(title+" | Current: "+initialValue);
        player.sendMessage("Enter the new value in chat, or type cancel.");
    }
    private static final class Input{
        final Consumer<String> submit;final Runnable cancel;
        Input(Consumer<String> submit,Runnable cancel){this.submit=submit;this.cancel=cancel;}
    }
    private static final class View implements InventoryHolder{
        final Screen screen;final int page;Inventory inventory;
        View(Screen screen,int page){this.screen=screen;this.page=page;}
        @Override public Inventory getInventory(){return inventory;}
    }
    public static final class Renderer implements Listener{
        private final TreeFellerBukkit plugin;
        private final Map<UUID,Input> input=new java.util.concurrent.ConcurrentHashMap<>();
        public Renderer(TreeFellerBukkit plugin){this.plugin=plugin;}
        private ItemStack item(String title,String description){return item(title,description,Screen.Kind.VALUE);}
        private ItemStack item(String title,String description,Screen.Kind kind){
            Material material;
            switch(kind){
                case SECTION:material=Material.CHEST;break;
                case TOGGLE:material=Material.LEVER;break;
                case ADD:material=Material.SLIME_BALL;break;
                case BACK:case MOVE:material=Material.ARROW;break;
                case SAVE:material=Material.EMERALD_BLOCK;break;
                case REMOVE:material=Material.BARRIER;break;
                default:material=Material.PAPER;
            }
            ItemStack item=new ItemStack(material);ItemMeta meta=item.getItemMeta();
            meta.setDisplayName(title);
            List<String> lore=new ArrayList<>();
            for(String line:description.split("\\n"))for(int i=0;i<line.length();i+=65)lore.add(line.substring(i,Math.min(line.length(),i+65)));
            meta.setLore(lore);item.setItemMeta(meta);return item;
        }
        void show(Player player,Screen screen,int page){
            input.remove(player.getUniqueId());
            View view=new View(screen,page);
            String title=screen.title;
            view.inventory=Bukkit.createInventory(view,54,title.substring(Math.max(0,title.length()-32)));
            for(int slot=0;slot<45;slot++){
                int index=page*45+slot;if(index>=screen.entries.size())break;
                Screen.Entry entry=screen.entries.get(index);view.inventory.setItem(slot,item(entry.title,entry.description,entry.kind));
            }
            if(page>0)view.inventory.setItem(45,item("Previous page",""));
            if((page+1)*45<screen.entries.size())view.inventory.setItem(53,item("Next page",""));
            player.openInventory(view.inventory);
        }
        @EventHandler public void click(InventoryClickEvent event){
            if(!(event.getView().getTopInventory().getHolder() instanceof View))return;
            event.setCancelled(true);
            if(!(event.getWhoClicked() instanceof Player))return;
            View view=(View)event.getView().getTopInventory().getHolder();
            int slot=event.getRawSlot();if(slot<0||slot>=54)return;
            Player player=(Player)event.getWhoClicked();
            Bukkit.getScheduler().runTask(plugin,() -> {
                if(!player.isOnline()||player.getOpenInventory().getTopInventory()!=view.inventory)return;
                if(slot==45&&view.page>0)show(player,view.screen,view.page-1);
                else if(slot==53&&(view.page+1)*45<view.screen.entries.size())show(player,view.screen,view.page+1);
                else if(slot<45){int index=view.page*45+slot;if(index<view.screen.entries.size())view.screen.entries.get(index).activate.run();}
            });
        }
        @EventHandler public void drag(InventoryDragEvent event){
            if(event.getView().getTopInventory().getHolder() instanceof View)event.setCancelled(true);
        }
        @EventHandler public void chat(AsyncPlayerChatEvent event){
            // The asynchronous event only schedules delivery; callbacks run on the server thread.
            Input pending=input.remove(event.getPlayer().getUniqueId());
            if(pending==null)return;
            event.setCancelled(true);String text=event.getMessage();
            Bukkit.getScheduler().runTask(plugin,() -> {if(!event.getPlayer().isOnline())return;if(text.equalsIgnoreCase("cancel"))pending.cancel.run();else pending.submit.accept(text);});
        }
        @EventHandler public void quit(PlayerQuitEvent event){input.remove(event.getPlayer().getUniqueId());}
    }
}
