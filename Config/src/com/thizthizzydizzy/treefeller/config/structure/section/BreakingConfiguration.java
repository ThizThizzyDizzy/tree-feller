package com.thizthizzydizzy.treefeller.config.structure.section;
import com.thizthizzydizzy.treefeller.config.structure.section.breaking.FallDirection;
import com.thizthizzydizzy.treefeller.config.structure.section.breaking.FellBehavior;
import java.util.HashMap;
import com.thizthizzydizzy.treefeller.config.ConfigComment;
public class BreakingConfiguration{
    @ConfigComment("How the tree trunk should be felled")
    public FellBehavior trunk_behavior = new FellBehavior();

    @ConfigComment("How the tree leaves should be felled")
    public FellBehavior leaves_behavior = new FellBehavior();

    @ConfigComment("How the tree decorations should be felled")
    public FellBehavior decoration_behavior = new FellBehavior();
    
    @ConfigComment("The direction that trees should fall")
    public FallDirection fall_direction = FallDirection.RANDOM;
    
    @ConfigComment("The yaw rotation for where trees should fall, in degrees. Applies to FIXED and RELATIVE fall directions.\nFor FIXED fall direction, 0 = North, 90 = east, etc.\nFor RELATIVE fall direction, 0 = forwards (away from the player), 90 = to the right, etc.")
    public float fall_direction_yaw;
    
    @ConfigComment("Force falling trees to fall in the closest cardinal direction (N/S/E/W)")
    public boolean fall_cardinal = false;
    
    @ConfigComment("Horizontal velocity applied to falling tree blocks, in the tree's falling direction")
    public float directional_fall_velocity = 0.35f;
    
    @ConfigComment("Upward vertical velocity applied to falling tree blocks")
    public float vertical_fall_velocity = 0.05f;
    
    @ConfigComment("Horizontal velocity applied to falling tree blocks, away from the block that was cut")
    public float explosive_fall_velocity = 0f;
    
    @ConfigComment("Horizontal velocity applied to each falling tree block, in a random direction")
    public float random_fall_velocity = 0f;
    
    @ConfigComment("Extra delay, in ticks, between breaking a block and spawning falling block entities.\n(This should be left at 0 unless you have issues with blocks colliding with an in-progress cutting animation)")
    public int fall_delay = 0;
    
    @ConfigComment("Convert these blocks instead of breaking them")
    public HashMap<String, String> block_conversions;
}
