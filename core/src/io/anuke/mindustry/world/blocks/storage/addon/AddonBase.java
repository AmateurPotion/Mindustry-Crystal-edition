package io.anuke.mindustry.world.blocks.storage.addon;

import io.anuke.mindustry.world.blocks.storage.StorageBlock;

public class AddonBase extends StorageBlock{

    public AddonBase(String name){
        super(name);
        solid = true;
        update = false;
        destructible = true;
    }

}
