package com.thizthizzydizzy.treefeller.forge.bootstrap.reforged;

import com.thizthizzydizzy.treefeller.forge.bootstrap.reforged.Bootstrapper;
import java.util.ArrayList;
import java.util.List;
import net.minecraftforge.fml.loading.moddiscovery.AbstractModProvider;
import net.minecraftforge.forgespi.locating.IModLocator;

public class Locator extends AbstractModProvider implements IModLocator{
    @Override
    public List<ModFileOrException> scanMods(){
        ArrayList<IModLocator.ModFileOrException> mods = new ArrayList<>();
        IModLocator.ModFileOrException mod = createMod(Bootstrapper.treeFellerPath, true);
        mods.add(mod);
        return mods;
    }
    @Override
    public String name(){
        return "treefeller-locator";
    }
}
