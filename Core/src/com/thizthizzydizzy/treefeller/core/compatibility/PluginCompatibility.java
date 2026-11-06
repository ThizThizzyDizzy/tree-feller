package com.thizthizzydizzy.treefeller.core.compatibility;
import com.thizthizzydizzy.treefeller.core.connector.world.IWorldConnector;
import com.thizthizzydizzy.treefeller.core.connector.player.IPlayerConnector;
public interface PluginCompatibility{
    String id();
    boolean installed();
    default void initialize(){}
    default void reload(){}
    default void close(){}
    /** Native fixture adapters; scenario decisions and assertions stay in Core. */
    interface TestCounter extends AutoCloseable{
        int count();
        @Override void close();
    }
    default TestCounter testQuest(IPlayerConnector player,java.util.Map<String,Object> options){throw new UnsupportedOperationException(id()+" has no quest API");}
    default Runnable testRegion(IWorldConnector world,long min,long max,java.util.UUID owner){throw new UnsupportedOperationException(id()+" has no region API");}
    default String testPlaceholder(IPlayerConnector player,String parameter){throw new UnsupportedOperationException(id()+" has no placeholder API");}
    default boolean canBreak(CompatibilityContext context, long pos){return true;}
    default boolean requirements(CompatibilityContext context){return true;}
    default void fell(CompatibilityContext context){}
    default void experience(CompatibilityContext context,String skill,double amount,boolean modifiers){}
    default double testExperience(CompatibilityContext context,String skill){throw new UnsupportedOperationException(id()+" has no XP observation API");}
    default int professionLevel(CompatibilityContext context,String profession){return Integer.MAX_VALUE;}
    default double breakBlock(CompatibilityContext context, long pos){return 1;}
    default double bonusDrops(CompatibilityContext context,long pos){return 1;}
    default void regenerate(CompatibilityContext context,long pos){}
    default void removeBlock(CompatibilityContext context, long pos){}
    default Object capture(IWorldConnector world, long pos){return null;}
    default void addBlock(CompatibilityContext context, long pos, Object previous){}
    default void dropItem(IPlayerConnector player, Object item){}
}
