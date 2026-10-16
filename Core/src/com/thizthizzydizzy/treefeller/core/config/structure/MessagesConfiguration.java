package com.thizthizzydizzy.treefeller.core.config.structure;
import com.thizthizzydizzy.treefeller.core.config.ConfigComment;
import com.thizthizzydizzy.treefeller.core.config.structure.message.MessageConfiguration;
import com.thizthizzydizzy.treefeller.core.config.structure.message.SingleMessageConfiguration;
/** All customizable player feedback lives here; diagnostics have fixed text. */
public class MessagesConfiguration{
    public SingleMessageConfiguration toggle = new SingleMessageConfiguration();
    public SingleMessageConfiguration checking = new SingleMessageConfiguration();
    public SingleMessageConfiguration prevent_breakage = new SingleMessageConfiguration();
    public SingleMessageConfiguration prevent_breakage_success = new SingleMessageConfiguration();
    public SingleMessageConfiguration durability_low = new SingleMessageConfiguration();
    public SingleMessageConfiguration partial = new SingleMessageConfiguration();
    public SingleMessageConfiguration partial_tool = new SingleMessageConfiguration();
    public SingleMessageConfiguration tree_protected = new SingleMessageConfiguration();
    public SingleMessageConfiguration success = new SingleMessageConfiguration();
    public SingleMessageConfiguration reload = new SingleMessageConfiguration();
    public SingleMessageConfiguration toggle_on = new SingleMessageConfiguration();
    public SingleMessageConfiguration toggle_off = new SingleMessageConfiguration();
    public SingleMessageConfiguration unknown_command = new SingleMessageConfiguration();
    public SingleMessageConfiguration no_permission = new SingleMessageConfiguration();
    public SingleMessageConfiguration command_usage = new SingleMessageConfiguration();

    @ConfigComment("Option failures use global/tool/tree; passing checks use success. Both chat and actionbar are optional.")
    public TriggerMessages trigger = new TriggerMessages();
    public CriteriaMessages criteria = new CriteriaMessages();
    public DetectionMessages detection = new DetectionMessages();
    public CuttingMessages cutting = new CuttingMessages();
    public BreakingMessages breaking = new BreakingMessages();
    public ResultMessages result = new ResultMessages();
    public ItemMessages item = new ItemMessages();

    public static class TriggerMessages{
        public MessageConfiguration default_enabled = new MessageConfiguration();
        public MessageConfiguration cooldown = new MessageConfiguration();
        public DualListMessages permissions = new DualListMessages();
        public RangeMessages food = new RangeMessages();
        public RangeMessages saturation = new RangeMessages();
        public RangeMessages health = new RangeMessages();
        public MessageConfiguration adventure_mode = new MessageConfiguration();
        public MessageConfiguration survival_mode = new MessageConfiguration();
        public MessageConfiguration creative_mode = new MessageConfiguration();
        public MessageConfiguration with_sneaking = new MessageConfiguration();
        public MessageConfiguration without_sneaking = new MessageConfiguration();
        public RangeMessages day_time = new RangeMessages();
        public RangeMessages moon_phase = new RangeMessages();
        public DualListMessages dimensions = new DualListMessages();
        public DualListMessages biomes = new DualListMessages();
        public DualListMessages allowed_trees = new DualListMessages();
    }

    public static class CriteriaMessages{
        public RangeMessages required_trunk = new RangeMessages();
        public RangeMessages required_leaves = new RangeMessages();
        public MessageConfiguration max_height = new MessageConfiguration();
        public MessageConfiguration require_cross_section = new MessageConfiguration();
        public BlockFilterMessages trunk_filter = new BlockFilterMessages();
        public BlockFilterMessages leaf_filter = new BlockFilterMessages();
        public BlockFilterMessages decoration_filter = new BlockFilterMessages();
        public MessageConfiguration trunk_max_horizontal_line = new MessageConfiguration();
        public RangeMessages height_ratio = new RangeMessages();
        public RangeMessages trunk_vertical_ratio = new RangeMessages();
    }

    public static class DetectionMessages{
        public MessageConfiguration root_distance = new MessageConfiguration();
        public MessageConfiguration disconnected_trunk_distance = new MessageConfiguration();
        public MessageConfiguration max_leaf_distance_from_top = new MessageConfiguration();
        public MessageConfiguration leaf_detect_range = new MessageConfiguration();
        public MessageConfiguration diagonal_leaves = new MessageConfiguration();
        public BlockDataRulesMessages block_data_rules = new BlockDataRulesMessages();
        public MessageConfiguration secondary_tree_verification = new MessageConfiguration();
        public DecorationMessages decorations = new DecorationMessages();
        public MessageConfiguration roots = new MessageConfiguration();
        public MessageConfiguration trunk = new MessageConfiguration();
        public MessageConfiguration leaves = new MessageConfiguration();
    }

    public static class CuttingMessages{
        public MessageConfiguration leave_stump = new MessageConfiguration();
        public MessageConfiguration leaf_break_range = new MessageConfiguration();
        public MessageConfiguration partial_trunk_limit = new MessageConfiguration();
        public MessageConfiguration trunk_damage_mult = new MessageConfiguration();
        public MessageConfiguration leaves_damage_mult = new MessageConfiguration();
        public MessageConfiguration decorations_damage_mult = new MessageConfiguration();
        public MessageConfiguration respect_unbreaking = new MessageConfiguration();
        public MessageConfiguration respect_unbreakable = new MessageConfiguration();
        public MessageConfiguration stacked_tools = new MessageConfiguration();
        public MessageConfiguration prevent_breakage = new MessageConfiguration();
        public MessageConfiguration allow_partial = new MessageConfiguration();
        public MessageConfiguration allow_partial_tool = new MessageConfiguration();
        public MessageConfiguration cutting_animation = new MessageConfiguration();
        public MessageConfiguration animation_delay = new MessageConfiguration();
    }

    public static class BreakingMessages{
        public FellBehaviorMessages trunk_behavior = new FellBehaviorMessages();
        public FellBehaviorMessages leaves_behavior = new FellBehaviorMessages();
        public FellBehaviorMessages decoration_behavior = new FellBehaviorMessages();
        public MessageConfiguration fall_direction = new MessageConfiguration();
        public MessageConfiguration fall_direction_yaw = new MessageConfiguration();
        public MessageConfiguration fall_cardinal = new MessageConfiguration();
        public MessageConfiguration directional_fall_velocity = new MessageConfiguration();
        public MessageConfiguration vertical_fall_velocity = new MessageConfiguration();
        public MessageConfiguration explosive_fall_velocity = new MessageConfiguration();
        public MessageConfiguration random_fall_velocity = new MessageConfiguration();
        public MessageConfiguration fall_delay = new MessageConfiguration();
        public MessageConfiguration block_conversions = new MessageConfiguration();
    }

    public static class ResultMessages{
        public SaplingReplantMessages sapling_replant = new SaplingReplantMessages();
        public MessageConfiguration overrideable_blocks = new MessageConfiguration();
        public MessageConfiguration trunk_fortune = new MessageConfiguration();
        public MessageConfiguration leaves_fortune = new MessageConfiguration();
        public MessageConfiguration decoration_fortune = new MessageConfiguration();
        public MessageConfiguration trunk_silk_touch = new MessageConfiguration();
        public MessageConfiguration leaves_silk_touch = new MessageConfiguration();
        public MessageConfiguration decoration_silk_touch = new MessageConfiguration();
        public MessageConfiguration rotate_logs = new MessageConfiguration();
        public MessageConfiguration drop_conversions = new MessageConfiguration();
        public MessageConfiguration drop_to_inventory = new MessageConfiguration();
        public MessageConfiguration trunk_drop_chance = new MessageConfiguration();
        public MessageConfiguration leaves_drop_chance = new MessageConfiguration();
        public MessageConfiguration decorations_drop_chance = new MessageConfiguration();
        public EffectMessages effects = new EffectMessages();
        public MessageConfiguration consumed_food_base = new MessageConfiguration();
        public MessageConfiguration consumed_food_trunk = new MessageConfiguration();
        public MessageConfiguration consumed_food_leaves = new MessageConfiguration();
        public MessageConfiguration consumed_food_decoration = new MessageConfiguration();
        public MessageConfiguration consumed_health_base = new MessageConfiguration();
        public MessageConfiguration consumed_health_trunk = new MessageConfiguration();
        public MessageConfiguration consumed_health_leaves = new MessageConfiguration();
        public MessageConfiguration consumed_health_decoration = new MessageConfiguration();
        public CascadeMessages cascade = new CascadeMessages();
    }

    public static class ItemMessages extends MessageConfiguration{
        public MessageConfiguration material = new MessageConfiguration();
        public VariableIntegerRangeMessages durability = new VariableIntegerRangeMessages();
        public DualListMessages custom_name = new DualListMessages();
        public DualListMessages lore = new DualListMessages();
        public DualListMessages attributes = new DualListMessages();
        public DualListMessages enchantments = new DualListMessages();
        public DualListMessages custom_model_data = new DualListMessages();
    }

    public static class RangeMessages extends MessageConfiguration{
        public MessageConfiguration min = new MessageConfiguration();
        public MessageConfiguration max = new MessageConfiguration();
    }

    public static class DualListMessages extends MessageConfiguration{
        public MessageConfiguration whitelist = new MessageConfiguration();
        public MessageConfiguration blacklist = new MessageConfiguration();
    }

    public static class BlockFilterMessages extends MessageConfiguration{
        public MessageConfiguration required = new MessageConfiguration();
        public MessageConfiguration blacklist = new MessageConfiguration();
    }

    public static class VariableIntegerRangeMessages extends MessageConfiguration{
        public RangeMessages value = new RangeMessages();
        public RangeMessages percent = new RangeMessages();
    }

    public static class BlockDataRulesMessages extends MessageConfiguration{
        public MessageConfiguration ignore_parallel_trunk_pillars = new MessageConfiguration();
        public MessageConfiguration use_leaf_distance = new MessageConfiguration();
    }

    public static class DecorationMessages extends MessageConfiguration{
        public MessageConfiguration blocks = new MessageConfiguration();
        public MessageConfiguration direction = new MessageConfiguration();
        public MessageConfiguration column = new MessageConfiguration();
    }

    public static class FellBehaviorMessages extends MessageConfiguration{
        public MessageConfiguration landing = new MessageConfiguration();
        public MessageConfiguration natural = new MessageConfiguration();
        public MessageConfiguration fall_as_entities = new MessageConfiguration();
        public MessageConfiguration fall_damage_amount = new MessageConfiguration();
        public MessageConfiguration fall_damage_maximum = new MessageConfiguration();
    }

    public static class SaplingReplantMessages extends MessageConfiguration{
        public MessageConfiguration enable = new MessageConfiguration();
        public MessageConfiguration use_tree_saplings = new MessageConfiguration();
        public MessageConfiguration use_inventory_saplings = new MessageConfiguration();
        public MessageConfiguration spawn_saplings = new MessageConfiguration();
        public MessageConfiguration sapling_timeout = new MessageConfiguration();
        public MessageConfiguration grasses = new MessageConfiguration();
        public MessageConfiguration saplings = new MessageConfiguration();
        public MessageConfiguration max_saplings = new MessageConfiguration();
    }

    public static class CascadeMessages extends MessageConfiguration{
        public MessageConfiguration enable = new MessageConfiguration();
        public MessageConfiguration parallel_cascade_limit = new MessageConfiguration();
        public MessageConfiguration cascade_check_limit = new MessageConfiguration();
        public MessageConfiguration trees = new MessageConfiguration();
    }

    public static class EffectMessages extends MessageConfiguration{
        public MessageConfiguration chance = new MessageConfiguration();
        public MessageConfiguration location = new MessageConfiguration();
        public MessageConfiguration type = new MessageConfiguration();
        public MessageConfiguration filter = new MessageConfiguration();
        public MessageConfiguration particle = new MessageConfiguration();
        public MessageConfiguration x = new MessageConfiguration();
        public MessageConfiguration y = new MessageConfiguration();
        public MessageConfiguration z = new MessageConfiguration();
        public MessageConfiguration dx = new MessageConfiguration();
        public MessageConfiguration dy = new MessageConfiguration();
        public MessageConfiguration dz = new MessageConfiguration();
        public MessageConfiguration speed = new MessageConfiguration();
        public MessageConfiguration count = new MessageConfiguration();
        public MessageConfiguration r = new MessageConfiguration();
        public MessageConfiguration g = new MessageConfiguration();
        public MessageConfiguration b = new MessageConfiguration();
        public MessageConfiguration size = new MessageConfiguration();
        public MessageConfiguration item = new MessageConfiguration();
        public MessageConfiguration block = new MessageConfiguration();
        public MessageConfiguration sound = new MessageConfiguration();
        public MessageConfiguration volume = new MessageConfiguration();
        public MessageConfiguration pitch = new MessageConfiguration();
        public MessageConfiguration power = new MessageConfiguration();
        public MessageConfiguration fire = new MessageConfiguration();
        public MessageConfiguration permanent = new MessageConfiguration();
        public MessageConfiguration tags = new MessageConfiguration();
    }
}
