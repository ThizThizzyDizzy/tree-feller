package com.thizthizzydizzy.treefeller.core.config.structure.section;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultBoolean;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultFallDirection;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultFloat;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultInteger;
import com.thizthizzydizzy.treefeller.core.config.globaldefault.ConfigGlobalDefaultNewInstance;
import com.thizthizzydizzy.treefeller.core.config.structure.section.breaking.FallDirection;
import com.thizthizzydizzy.treefeller.core.config.structure.section.breaking.FellBehavior;
import com.thizthizzydizzy.treefeller.core.config.structure.special.IBlockDefinition;
import java.util.HashMap;
public class BreakingConfiguration{
    @ConfigComment("How the tree trunk should be felled")
    @ConfigGlobalDefaultNewInstance
    public FellBehavior trunk_behavior;

    @ConfigComment("How the tree leaves should be felled")
    @ConfigGlobalDefaultNewInstance
    public FellBehavior leaves_behavior;

    @ConfigComment("How the tree decorations should be felled")
    @ConfigGlobalDefaultNewInstance
    public FellBehavior decoration_behavior;
    
    @ConfigComment("The direction that trees should fall")
    @ConfigGlobalDefaultFallDirection(FallDirection.RANDOM)
    public FallDirection fall_direction;
    
    @ConfigComment("The yaw rotation for where trees should fall, in degrees. Applies to FIXED and RELATIVE fall directions.\nFor FIXED fall direction, 0 = North, 90 = east, etc.\nFor RELATIVE fall direction, 0 = forwards (away from the player), 90 = to the right, etc.")
    @ConfigGlobalDefaultFloat(0)
    public Float fall_direction_yaw;
    
    @ConfigComment("Force falling trees to fall in the closest cardinal direction (N/S/E/W)")
    @ConfigGlobalDefaultBoolean(false)
    public Boolean fall_cardinal;
    
    @ConfigComment("Horizontal velocity applied to falling tree blocks, in the tree's falling direction")
    @ConfigGlobalDefaultFloat(0.35f)
    public Float directional_fall_velocity;
    
    @ConfigComment("Upward vertical velocity applied to falling tree blocks")
    @ConfigGlobalDefaultFloat(0.05f)
    public Float vertical_fall_velocity;
    
    @ConfigComment("Horizontal velocity applied to falling tree blocks, away from the block that was cut")
    @ConfigGlobalDefaultFloat(0)
    public Float explosive_fall_velocity;
    
    @ConfigComment("Horizontal velocity applied to each falling tree block, in a random direction")
    @ConfigGlobalDefaultFloat(0)
    public Float random_fall_velocity;
    
    @ConfigComment("Extra delay, in ticks, between breaking a block and spawning falling block entities.\n(This should be left at 0 unless you have issues with blocks colliding with an in-progress cutting animation)")
    @ConfigGlobalDefaultInteger(0)
    public Integer fall_delay;
    
    @ConfigComment("Convert these blocks instead of breaking them")
    public HashMap<IBlockDefinition, IBlockDefinition> block_conversions;
}
