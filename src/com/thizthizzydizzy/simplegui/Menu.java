package com.thizthizzydizzy.simplegui;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.function.BiConsumer;
import net.wesjd.anvilgui.AnvilGUI;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryDragEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
public abstract class Menu{
    public final Plugin plugin;
    public final Player player;
    protected final String title;
    protected Inventory inventory;
    private MenuListener listener = new MenuListener();
    private boolean open;
    protected final int size;
    protected final ArrayList<Component> components = new ArrayList<>();
    public final Menu parent;
    public Menu(Menu parent, Plugin plugin, Player player, String title, int size){
        this.parent = parent;
        this.plugin = plugin;
        this.player = player;
        this.title = title;
        this.size = size;
    }
    public void openInventory(){
        if(open){
            updateInventory();
            return;
        }
        Bukkit.getPluginManager().registerEvents(listener, plugin);
        inventory = Bukkit.createInventory(null, size, title);
        updateInventory();
        player.openInventory(inventory);
        open = true;
        onOpen();
    }
    public final void open(Menu menu){
        doClose(menu==null);
        if(menu!=null)menu.openInventory();
    }
    private void doClose(boolean closeInv){
        if(!open)return;
        open = false;
        if(closeInv&&!inventory.getViewers().isEmpty())player.closeInventory();
        HandlerList.unregisterAll(listener);
    }
    public final void close(){
        doClose(true);
    }
    public final void openAnvilGUI(String initialText, String title, BiConsumer<Player, String> completeFunction){
        close();
        new AnvilGUI.Builder().text(initialText).plugin(plugin).title(title).itemLeft(new ItemBuilder(Material.PAPER).setDisplayName(initialText).build()).onClose((plyr) -> {
            new BukkitRunnable() {
                @Override
                public void run(){
                    open(Menu.this);
                }
            }.runTask(plugin);
        }).onClick((i, state) -> {
            completeFunction.accept(state.getPlayer(), state.getText());
            new BukkitRunnable() {
                @Override
                public void run(){
                    open(Menu.this);
                }
            }.runTask(plugin);
            return Arrays.asList(AnvilGUI.ResponseAction.close());
        }).open(player);
    }
    public <T extends Component> T add(T component){
        if(component==null)throw new IllegalArgumentException("Cannot add null to a menu!");
        components.add(component);
        return component;
    }
    protected void updateInventory(){
        if(inventory==null)return;//be patient, the inventory doesn't even exist yet!
        inventory.clear();
        for(Component c : components){
            inventory.setItem(c.index, c.draw());
        }
    }
    /**
     * Only called when closed by an external source, such as the player
     */
    public void onClose(){
        new BukkitRunnable(){
            @Override
            public void run(){
                open(parent);//WHY doesn't this work on the same thread? I dunno.
            }
        }.runTask(plugin);
    }
    public void onOpen(){}
    public void onVoidClicked(){}
    public void onClick(int slot, ClickType click){}
    public void onInventoryClick(int slot, ClickType click){}
    private void click(int slot, ClickType click){
        for(int i = 0; i<components.size(); i++){
            Component c = components.get(i);
            if(c.index==slot)c.onClick(click);
        }
        onClick(slot, click);
    }
    class MenuListener implements Listener{
        @EventHandler
        public void onInventoryClick(InventoryClickEvent event){
            if(event.getInventory().equals(inventory)){
                Player clicker = (Player) event.getWhoClicked();
                event.setCancelled(true);
                if(clicker!=player)return;
                if(event.getRawSlot()<0){
                    Menu.this.onVoidClicked();
                }else if(event.getClickedInventory().equals(inventory)){
                    Menu.this.click(event.getSlot(), event.getClick());
                }else{
                    Menu.this.onInventoryClick(event.getSlot(), event.getClick());
                }
            }
        }
        @EventHandler
        public void onInventoryDrag(InventoryDragEvent event){
            if(event.getInventory().equals(inventory)){
                event.setCancelled(true);
            }
        }
        @EventHandler
        public void onInventoryClose(InventoryCloseEvent event){
            if(open&&event.getInventory().equals(inventory)){
                close();
                onClose();
            }
        }
    }
    /**
     * Converts a slot number to coordinates, where (0,0) is in the center of the GUI.
     * Unknown behavior with even height.
     * @param id the slot ID
     * @return <code>new int[]{x, y}</code>
     */
    protected int[] convertIdToCenteredCoords(int id){
        int x = id;
        int y = 0;
        while(x>8){
            x-=9;
            y++;
        }
        x-=4;
        y-=size/18;// height/2
        return new int[]{x,y};
    }
    protected int convertCenteredCoordsToId(int x, int y){
        int row = y+size/18;// height/2
        int column = x+4;
        if(row<0||column<0||row>(size/9)-1||column>8)return -1;//offscreen
        return row*9+column;
    }
    protected ItemBuilder makeItem(Material type){
        return new ItemBuilder(type);
    }
}