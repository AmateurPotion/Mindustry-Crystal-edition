package io.anuke.mindustry.world.blocks.storage;

import io.anuke.mindustry.content.Mechs;
import io.anuke.mindustry.type.Mech;
import io.anuke.mindustry.world.Tile;


public class Coreshard extends CoreBlock {
    protected Mech mech = Mechs.starter;

    public Coreshard(String name){
        super(name);
        //TODO add arc fuction
    }

    @Override
    public boolean canBreak(Tile tile){
        return true;
    }

}
