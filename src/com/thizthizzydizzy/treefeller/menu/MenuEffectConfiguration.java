package com.thizthizzydizzy.treefeller.menu;
import com.thizthizzydizzy.simplegui.Button;
import com.thizthizzydizzy.simplegui.ItemBuilder;
import com.thizthizzydizzy.simplegui.Menu;
import com.thizthizzydizzy.treefeller.Effect;
import com.thizthizzydizzy.treefeller.Effect.EffectLocation;
import com.thizthizzydizzy.treefeller.Option;
import com.thizthizzydizzy.treefeller.Tool;
import com.thizthizzydizzy.treefeller.Tree;
import com.thizthizzydizzy.treefeller.TreeFeller;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyBoolean;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyDouble;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyEnum;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyFloat;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyInteger;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyMaterial;
import com.thizthizzydizzy.treefeller.menu.modify.MenuModifyStringList;
import java.util.ArrayList;
import java.util.Arrays;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;
public class MenuEffectConfiguration extends Menu{
    private final Effect effect;
    public MenuEffectConfiguration(Menu parent, Plugin plugin, Player player, Effect effect){
        super(parent, plugin, player, "Effect Configuration: "+effect.name, 18);
        this.effect = effect;
        refresh();
    }
    @Override
    public void onOpen(){
        refresh();
    }
    private void refresh(){
        components.clear();
        add(new Button(0, new ItemBuilder(Material.PAPER).setDisplayName("Name").addLore(effect.name), (click) -> {
            if(click==ClickType.LEFT){
                openAnvilGUI(effect.name, "Modify Effect Name", (player, str) -> {
                    effect.name = str;
                });
            }
        }));
        add(new Button(1, new ItemBuilder(effect.location.getItem()).setDisplayName("Location").addLore(effect.location.toString()), (click) -> {
            if(click==ClickType.LEFT)open(new MenuModifyEnum<EffectLocation>(this, plugin, player, effect.name, "EffectLocation", false, effect.location, EffectLocation.values(), (value) -> {
                effect.location = value;
            }){
                @Override
                public Material getItem(EffectLocation value){
                    return value.getItem();
                }
            });
        }));
        add(new Button(2, new ItemBuilder(Material.BELL).setDisplayName("Chance").addLore(effect.chance+""), (click) -> {
            if(click==ClickType.LEFT)open(new MenuModifyDouble(this, plugin, player, effect.name, 0, 1, false, effect.chance, (value) -> {
                effect.chance = value;
            }));
        }));
        switch(effect.type){
            case EXPLOSION:
                add(new Button(3, new ItemBuilder(Material.TNT).setDisplayName("Power").addLore(effect.power+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyFloat(this, plugin, player, effect.name, 0, Float.MAX_VALUE, false, effect.power, (value) -> {
                        effect.power = value;
                    }));
                }));
                add(new Button(4, new ItemBuilder(Material.FIRE_CHARGE).setDisplayName("Fire").addLore(effect.fire+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyBoolean(this, plugin, player, effect.name, false, effect.fire, (value) -> {
                        effect.fire = value;
                    }));
                }));
                break;
            case SOUND:
                add(new Button(3, new ItemBuilder(Material.NOTE_BLOCK).setDisplayName("Sound").addLore(effect.sound), (click) -> {
                    if(click==ClickType.LEFT)openAnvilGUI(effect.sound, "Modify Effect Sound: "+effect.name, (player, str) -> {
                        effect.sound = str;
                    });
                }));
                add(new Button(4, new ItemBuilder(Material.NOTE_BLOCK).setDisplayName("Volume").addLore(effect.volume+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyFloat(this, plugin, player, effect.name, 0, Float.MAX_VALUE, false, effect.volume, (value) -> {
                        effect.volume = value;
                    }));
                }));
                add(new Button(5, new ItemBuilder(Material.NOTE_BLOCK).setDisplayName("Pitch").addLore(effect.pitch+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyFloat(this, plugin, player, effect.name, 0.5f, 2f, false, effect.pitch, (value) -> {
                        effect.pitch = value;
                    }));
                }));
                break;
            case PARTICLE:
                add(new Button(3, new ItemBuilder(Material.FIREWORK_STAR).setDisplayName("Particle").addLore(effect.particle.toString()), (click) -> {
                    if(click==ClickType.LEFT){
                        openAnvilGUI(effect.particle.name(), "Modify Effect Particle: "+effect.name, (player, str) -> {
                            try{
                                Particle p = TreeFeller.getParticle(str);
                                if(p!=null){
                                    effect.particle = p;
                                    effect.extra = null;
                                }
                            }catch(IllegalArgumentException ex){}
                        });//TODO switch back to enum when that supports multi-page
//                        open(new MenuModifyEnum<>(this, plugin, player, effect.name, "Particle", false, effect.particle, Particle.values(), (value) -> {
//                            effect.particle = value;
//                        }));
                    }
                }));
                add(new Button(4, new ItemBuilder(Material.RED_CONCRETE).setDisplayName("x").addLore(effect.x+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyDouble(this, plugin, player, effect.name, -Double.MAX_VALUE, Double.MAX_VALUE, false, effect.x, (value) -> {
                        effect.x = value;
                    }));
                }));
                add(new Button(5, new ItemBuilder(Material.GREEN_CONCRETE).setDisplayName("y").addLore(effect.y+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyDouble(this, plugin, player, effect.name, -Double.MAX_VALUE, Double.MAX_VALUE, false, effect.y, (value) -> {
                        effect.y = value;
                    }));
                }));
                add(new Button(6, new ItemBuilder(Material.BLUE_CONCRETE).setDisplayName("z").addLore(effect.z+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyDouble(this, plugin, player, effect.name, -Double.MAX_VALUE, Double.MAX_VALUE, false, effect.z, (value) -> {
                        effect.z = value;
                    }));
                }));
                
                add(new Button(7, new ItemBuilder(Material.RED_CONCRETE).setDisplayName("dx").addLore(effect.dx+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyDouble(this, plugin, player, effect.name, -Double.MAX_VALUE, Double.MAX_VALUE, false, effect.dx, (value) -> {
                        effect.dx = value;
                    }));
                }));
                add(new Button(8, new ItemBuilder(Material.GREEN_CONCRETE).setDisplayName("dy").addLore(effect.dy+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyDouble(this, plugin, player, effect.name, -Double.MAX_VALUE, Double.MAX_VALUE, false, effect.dy, (value) -> {
                        effect.dy = value;
                    }));
                }));
                add(new Button(9, new ItemBuilder(Material.BLUE_CONCRETE).setDisplayName("dz").addLore(effect.dz+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyDouble(this, plugin, player, effect.name, -Double.MAX_VALUE, Double.MAX_VALUE, false, effect.dz, (value) -> {
                        effect.dz = value;
                    }));
                }));
                add(new Button(10, new ItemBuilder(Material.BLUE_CONCRETE).setDisplayName("Speed").addLore(effect.speed+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyDouble(this, plugin, player, effect.name, 0, Double.MAX_VALUE, false, effect.speed, (value) -> {
                        effect.speed = value;
                    }));
                }));
                add(new Button(11, new ItemBuilder(Material.BLUE_CONCRETE).setDisplayName("Count").addLore(effect.count+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyInteger(this, plugin, player, effect.name, 0, Integer.MAX_VALUE, false, effect.count, (value) -> {
                        effect.count = value;
                    }));
                }));
                switch(effect.particle){
                    case REDSTONE:
                        add(new Button(12, new ItemBuilder(Material.RED_DYE).setDisplayName("Red").addLore(effect.extra==null?"null":((Particle.DustOptions)effect.extra).getColor().getRed()+""), (click) -> {
                            if(click==ClickType.LEFT)open(new MenuModifyInteger(this, plugin, player, effect.name, 0, 255, false, effect.extra==null?0:((Particle.DustOptions)effect.extra).getColor().getRed(), (value) -> {
                                int g = 0;
                                int b = 0;
                                float size = 1;
                                if(effect.extra!=null){
                                    Particle.DustOptions extra = (Particle.DustOptions)effect.extra;
                                    g = extra.getColor().getGreen();
                                    b = extra.getColor().getBlue();
                                    size = extra.getSize();
                                }
                                effect.extra = new Particle.DustOptions(Color.fromRGB(value, g, b), size);
                            }));
                        }));
                        add(new Button(13, new ItemBuilder(Material.GREEN_DYE).setDisplayName("Green").addLore(effect.extra==null?"null":((Particle.DustOptions)effect.extra).getColor().getGreen()+""), (click) -> {
                            if(click==ClickType.LEFT)open(new MenuModifyInteger(this, plugin, player, effect.name, 0, 255, false, effect.extra==null?0:((Particle.DustOptions)effect.extra).getColor().getGreen(), (value) -> {
                                int r = 0;
                                int b = 0;
                                float size = 1;
                                if(effect.extra!=null){
                                    Particle.DustOptions extra = (Particle.DustOptions)effect.extra;
                                    r = extra.getColor().getRed();
                                    b = extra.getColor().getBlue();
                                    size = extra.getSize();
                                }
                                effect.extra = new Particle.DustOptions(Color.fromRGB(r, value, b), size);
                            }));
                        }));
                        add(new Button(14, new ItemBuilder(Material.BLUE_DYE).setDisplayName("Blue").addLore(effect.extra==null?"null":((Particle.DustOptions)effect.extra).getColor().getBlue()+""), (click) -> {
                            if(click==ClickType.LEFT)open(new MenuModifyInteger(this, plugin, player, effect.name, 0, 255, false, effect.extra==null?0:((Particle.DustOptions)effect.extra).getColor().getBlue(), (value) -> {
                                int r = 0;
                                int g = 0;
                                float size = 1;
                                if(effect.extra!=null){
                                    Particle.DustOptions extra = (Particle.DustOptions)effect.extra;
                                    r = extra.getColor().getRed();
                                    g = extra.getColor().getGreen();
                                    size = extra.getSize();
                                }
                                effect.extra = new Particle.DustOptions(Color.fromRGB(r, g, value), size);
                            }));
                        }));
                        add(new Button(15, new ItemBuilder(Material.PISTON).setDisplayName("Size").addLore(effect.extra==null?"null":((Particle.DustOptions)effect.extra).getSize()+""), (click) -> {
                            if(click==ClickType.LEFT)open(new MenuModifyFloat(this, plugin, player, effect.name, 0, Float.MAX_VALUE, false, effect.extra==null?0:((Particle.DustOptions)effect.extra).getSize(), (value) -> {
                                int r = 0;
                                int g = 0;
                                int b = 0;
                                if(effect.extra!=null){
                                    Particle.DustOptions extra = (Particle.DustOptions)effect.extra;
                                    r = extra.getColor().getRed();
                                    g = extra.getColor().getGreen();
                                    b = extra.getColor().getBlue();
                                }
                                effect.extra = new Particle.DustOptions(Color.fromRGB(r, g, b), value);
                            }));
                        }));
                        break;
                    case ITEM_CRACK:
                        add(new Button(12, new ItemBuilder(Material.IRON_PICKAXE).setDisplayName("Item").addLore(effect.extra==null?"null":((ItemStack)effect.extra).getType().toString()), (click) -> {
                            if(click==ClickType.LEFT)open(new MenuModifyMaterial(this, plugin, player, effect.name, false, "item", effect.extra==null?Material.IRON_PICKAXE:((ItemStack)effect.extra).getType(), (mat) -> {
                                return mat.isItem();
                            }, (value) -> {
                                effect.extra = new ItemStack(value);
                            }));
                        }));
                        break;
                    case BLOCK_CRACK:
                    case BLOCK_DUST:
                    case FALLING_DUST:
                        add(new Button(12, new ItemBuilder(Material.IRON_PICKAXE).setDisplayName("Block").addLore(effect.extra==null?"null":((BlockData)effect.extra).getMaterial().toString()), (click) -> {
                            if(click==ClickType.LEFT)open(new MenuModifyMaterial(this, plugin, player, effect.name, false, "block", effect.extra==null?Material.STONE:((BlockData)effect.extra).getMaterial(), (mat) -> {
                                return mat.isBlock();
                            }, (value) -> {
                                effect.extra = Bukkit.createBlockData(value);
                            }));
                        }));
                        break;
                }
                break;
            case MARKER:
                add(new Button(3, new ItemBuilder(Material.ARMOR_STAND).setDisplayName("Permanent").addLore(effect.permanent+""), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyBoolean(this, plugin, player, effect.name, false, effect.permanent, (value) -> {
                        effect.permanent = value;
                    }));
                }));
                add(new Button(4, new ItemBuilder(Material.PAPER).setDisplayName("Tags").addLore(Arrays.toString(effect.tags)), (click) -> {
                    if(click==ClickType.LEFT)open(new MenuModifyStringList(this, plugin, player, effect.name, false, new ArrayList<>(Arrays.asList(effect.tags)), (value) -> {
                        effect.tags = value.toArray(new String[value.size()]);
                    }));
                }));
                break;
            default:
                throw new IllegalArgumentException("Could not build GUI for EffectType: "+effect.type+"! This is a bug!");
        }
        add(new Button(size-1, makeItem(Material.BARRIER).setDisplayName("Back").addLore("Shift-right click to delete effect"), (click) -> {
            if(click==ClickType.LEFT)open(parent);
            if(click==ClickType.SHIFT_RIGHT){
                TreeFeller.effects.remove(effect);
                if(Option.EFFECTS.globalValue!=null)Option.EFFECTS.globalValue.remove(effect);
                for(Tool t : TreeFeller.tools){
                    if(Option.EFFECTS.getValue(t)!=null){
                        Option.EFFECTS.getValue(t).remove(effect);
                    }
                }
                for(Tree t : TreeFeller.trees){
                    if(Option.EFFECTS.getValue(t)!=null){
                        Option.EFFECTS.getValue(t).remove(effect);
                    }
                }
                open(parent);
            }
        }));
        updateInventory();
    }
}