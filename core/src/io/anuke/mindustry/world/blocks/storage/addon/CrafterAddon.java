package io.anuke.mindustry.world.blocks.storage.addon;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;

import io.anuke.arc.function.Consumer;
import io.anuke.arc.function.Supplier;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.arc.math.Mathf;
import io.anuke.arc.util.Time;
import io.anuke.mindustry.content.Fx;
import io.anuke.mindustry.entities.Effects;
import io.anuke.mindustry.entities.Effects.Effect;
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.gen.*;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.ItemStack;
import io.anuke.mindustry.type.Liquid;
import io.anuke.mindustry.type.LiquidStack;
import io.anuke.mindustry.world.Tile;
import io.anuke.mindustry.world.consumers.ConsumeLiquidBase;
import io.anuke.mindustry.world.consumers.ConsumeType;
import io.anuke.mindustry.world.meta.BlockStat;
import io.anuke.mindustry.world.meta.StatUnit;

public class CrafterAddon extends AddonBase{
    protected ItemStack outputItem;
    protected LiquidStack outputLiquid;

    protected float craftTime = 80;
    protected Effect craftEffect = Fx.none;
    protected Effect updateEffect = Fx.none;
    protected float updateEffectChance = 0.04f;

    protected Consumer<Tile> drawer = null;
    protected Supplier<TextureRegion[]> drawIcons = null;

    public CrafterAddon(String name){
        super(name);
        update = true;
        solid = true;
        hasItems = true;
        health = 60;
        idleSound = Sounds.machine;
        idleSoundVolume = 0.03f;
        destructible = true;
    }

    @Override
    public void setStats(){
        if(consumes.has(ConsumeType.liquid)){
            ConsumeLiquidBase cons = consumes.get(ConsumeType.liquid);
            cons.timePeriod = craftTime;
        }

        super.setStats();
        stats.add(BlockStat.productionTime, craftTime / 60f, StatUnit.seconds);

        if(outputItem != null){
            stats.add(BlockStat.output, outputItem);
        }

        if(outputLiquid != null){
            stats.add(BlockStat.output, outputLiquid.liquid, outputLiquid.amount, false);
        }
    }

    @Override
    public boolean shouldIdleSound(Tile tile){
        return tile.entity.cons.valid();
    }

    @Override
    public void init(){
        outputsLiquid = outputLiquid != null;
        super.init();
    }

    @Override
    public void draw(Tile tile){
        if(drawer == null){
            super.draw(tile);
        }else{
            drawer.accept(tile);
        }
    }

    @Override
    public TextureRegion[] generateIcons(){
        return drawIcons == null ? super.generateIcons() : drawIcons.get();
    }

    @Override
    public void update(Tile tile){
        CrafterAddonEntity entity1 = tile.entity();

        if(entity1.cons.valid()){

            entity1.progress += getProgressIncrease(entity1, craftTime);
            entity1.totalProgress += entity1.delta();
            entity1.warmup = Mathf.lerpDelta(entity1.warmup, 1f, 0.02f);

            if(Mathf.chance(Time.delta() * updateEffectChance)){
                Effects.effect(updateEffect, entity1.x + Mathf.range(size * 4f), entity1.y + Mathf.range(size * 4));
            }
        }else{
            entity1.warmup = Mathf.lerp(entity1.warmup, 0f, 0.02f);
        }

        if(entity1.progress >= 1f){
            entity1.cons.trigger();

            if(outputItem != null){
                useContent(tile, outputItem.item);
                for(int i = 0; i < outputItem.amount; i++){
                    offloadNear(tile, outputItem.item);
                }
            }

            if(outputLiquid != null){
                useContent(tile, outputLiquid.liquid);
                handleLiquid(tile, tile, outputLiquid.liquid, outputLiquid.amount);
            }

            Effects.effect(craftEffect, tile.drawx(), tile.drawy());
            entity1.progress = 0f;
        }

        if(outputItem != null && tile.entity.timer.get(timerDump, dumpTime)){
            tryDump(tile, outputItem.item);
        }

        if(outputLiquid != null){
            tryDumpLiquid(tile, outputLiquid.liquid);
        }
    }

    @Override
    public boolean outputsItems(){
        return outputItem != null;
    }



    @Override
    public boolean canProduce(Tile tile){
        if(outputItem != null && tile.entity.items.get(outputItem.item) >= itemCapacity){
            return false;
        }
        return outputLiquid == null || !(tile.entity.liquids.get(outputLiquid.liquid) >= liquidCapacity);
    }

    @Override
    public TileEntity newEntity(){
        return new CrafterAddonEntity();
    }

    @Override
    public int getMaximumAccepted(Tile tile, Item item){
        return itemCapacity;
    }

    public Item outputItem(){
        return outputItem == null ? null : outputItem.item;
    }

    public Liquid outputLiquid(){
        return outputLiquid == null ? null : outputLiquid.liquid;
    }

    public static class CrafterAddonEntity extends TileEntity{
        public float progress;
        public float totalProgress;
        public float warmup;

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeFloat(progress);
            stream.writeFloat(warmup);
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            progress = stream.readFloat();
            warmup = stream.readFloat();
        }
    }
}
