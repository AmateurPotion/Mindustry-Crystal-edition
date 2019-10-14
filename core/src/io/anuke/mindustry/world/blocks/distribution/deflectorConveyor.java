package io.anuke.mindustry.world.blocks.distribution;

import io.anuke.arc.*;
import io.anuke.arc.collection.*;
import io.anuke.arc.graphics.*;
import io.anuke.arc.graphics.g2d.*;
import io.anuke.arc.math.*;
import io.anuke.arc.math.geom.*;
import io.anuke.arc.util.*;
import io.anuke.mindustry.entities.type.*;
import io.anuke.mindustry.gen.*;
import io.anuke.mindustry.graphics.*;
import io.anuke.mindustry.input.InputHandler.*;
import io.anuke.mindustry.type.*;
import io.anuke.mindustry.world.*;
import io.anuke.mindustry.world.meta.*;
import io.anuke.arc.math.Mathf;
import io.anuke.mindustry.entities.type.Bullet;
import io.anuke.mindustry.entities.effect.Lightning;
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.graphics.Pal;
import io.anuke.arc.Core;
import io.anuke.arc.graphics.g2d.Draw;
import io.anuke.arc.graphics.g2d.TextureRegion;
import io.anuke.mindustry.world.*;
import io.anuke.mindustry.world.meta.BlockGroup;

import static io.anuke.mindustry.Vars.content;
import static io.anuke.mindustry.Vars.itemSize;
import static io.anuke.mindustry.Vars.tilesize;
import java.io.*;

import static io.anuke.mindustry.Vars.*;

public class deflectorConveyor extends Block{
    protected int variants = 0;

    private static final float itemSpace = 0.4f;
    private static final float minmove = 1f / (Short.MAX_VALUE - 2);
    private static ItemPos drawpos = new ItemPos();
    private static ItemPos pos1 = new ItemPos();
    private static ItemPos pos2 = new ItemPos();
    private final Vector2 tr1 = new Vector2();
    private final Vector2 tr2 = new Vector2();
    //shield
    public static final float hitTime = 10f;

    protected float maxDamageDeflect = 10f;
    protected Rectangle rect = new Rectangle();
    protected Rectangle rect2 = new Rectangle();
    //lightning
    protected float lightningChance = 0.05f;
    protected float lightningDamage = 15f;
    protected int lightningLength = 17;

    private TextureRegion[][] regions = new TextureRegion[7][4];

    protected float speed = 0f;

    protected deflectorConveyor(String name){
        super(name);
        solid = true;
        destructible = true;
        rotate = true;
        update = true;
        layer = Layer.overlay;
        group = BlockGroup.transportation;
        hasItems = true;
        itemCapacity = 4;

        idleSound = Sounds.conveyor;
        idleSoundVolume = 0.004f;
        unloadable = false;
    }

    private static int compareItems(long a, long b){
        pos1.set(a, ItemPos.packShorts);
        pos2.set(b, ItemPos.packShorts);
        return Float.compare(pos1.y, pos2.y);
    }

    @Override
    public void setStats(){
        super.setStats();
        stats.add(BlockStat.itemsMoved, speed * 60 * (1f / itemSpace), StatUnit.itemsSecond);
    }

    @Override
    public void load(){
        super.load();

        if(variants != 0){
            variantRegions = new TextureRegion[variants];

            for(int i = 0; i < variants; i++){
                variantRegions[i] = Core.atlas.find(name + (i + 1));
            }
            region = variantRegions[0];
        }

        for(int i = 0; i < regions.length; i++){
            for(int j = 0; j < 4; j++){
                regions[i][j] = Core.atlas.find(name + "-" + i + "-" + j);
            }
        }
    }

    @Override
    public void draw(Tile tile){
//wall
        if(variants == 0){
            Draw.rect(region, tile.drawx(), tile.drawy());
        }else{
            Draw.rect(variantRegions[Mathf.randomSeed(tile.pos(), 0, Math.max(0, variantRegions.length - 1))], tile.drawx(), tile.drawy());
        }
//conveyor
        super.draw(tile);

        deflectorConveyorEntity entity1 = tile.entity();

        if(entity1.hit < 0.0001f) return;

        Draw.alpha(entity1.hit * 0.5f);
        Fill.rect(tile.drawx(), tile.drawy(), tilesize * size, tilesize * size);
        Draw.blend();
        Draw.reset();

        entity1.hit = Mathf.clamp(entity1.hit - Time.delta() / hitTime);


        deflectorConveyorEntity entity2 = tile.entity();
        byte rotation = tile.rotation();

        int frame = entity2.clogHeat <= 0.5f ? (int)(((Time.time() * speed * 8f * entity2.timeScale)) % 4) : 0;
        Draw.rect(regions[Mathf.clamp(entity2.blendbits, 0, regions.length - 1)][Mathf.clamp(frame, 0, regions[0].length - 1)], tile.drawx(), tile.drawy(),
        tilesize * entity2.blendsclx, tilesize * entity2.blendscly, rotation * 90);
    }

    @Override
    public boolean shouldIdleSound(Tile tile){
        deflectorConveyorEntity entity2 = tile.entity();
        return entity2.clogHeat <= 0.5f ;
    }

    @Override
    public void onProximityUpdate(Tile tile){
        super.onProximityUpdate(tile);

        deflectorConveyorEntity entity2 = tile.entity();
        entity2.blendbits = 0;
        entity2.blendsclx = entity2.blendscly = 1;

        if(blends(tile, 2) && blends(tile, 1) && blends(tile, 3)){
            entity2.blendbits = 3;
        }else if(blends(tile, 1) && blends(tile, 3)){
            entity2.blendbits = 4;
        }else if(blends(tile, 1) && blends(tile, 2)){
            entity2.blendbits = 2;
        }else if(blends(tile, 3) && blends(tile, 2)){
            entity2.blendbits = 2;
            entity2.blendscly = -1;
        }else if(blends(tile, 1)){
            entity2.blendbits = 1;
            entity2.blendscly = -1;
        }else if(blends(tile, 3)){
            entity2.blendbits = 1;
        }
    }

    @Override
    public void getPlaceDraw(PlaceDraw draw, int rotation, int prevX, int prevY, int prevRotation){
        draw.rotation = rotation;
        draw.scalex = draw.scaley = 1;

        int blendbits = 0;

        if(blends(rotation, 1, prevX, prevY, prevRotation)){
            blendbits = 1;
            draw.scaley = -1;
        }else if(blends(rotation, 3, prevX, prevY, prevRotation)){
            blendbits = 1;
        }

        draw.rotation = rotation;
        draw.region = regions[blendbits][0];
    }

    protected boolean blends(int rotation, int offset, int prevX, int prevY, int prevRotation){
        Point2 left = Geometry.d4(rotation - offset);
        return left.equals(prevX, prevY) && prevRotation == Mathf.mod(rotation + offset, 4);
    }

    protected boolean blends(Tile tile, int direction){
        Tile other = tile.getNearby(Mathf.mod(tile.rotation() - direction, 4));
        if(other != null) other = other.link();

        return other != null && other.block().outputsItems()
        && ((tile.getNearby(tile.rotation()) == other) || (!other.block().rotate || other.getNearby(other.rotation()) == tile));
    }

    @Override
    public TextureRegion[] generateIcons(){
        return new TextureRegion[]{Core.atlas.find(name + "-0-0")};
    }

    @Override
    public void drawLayer(Tile tile){
        deflectorConveyorEntity entity = tile.entity();

        byte rotation = tile.rotation();

        try{

            for(int i = 0; i < entity.convey.size; i++){
                ItemPos pos = drawpos.set(entity.convey.get(i), ItemPos.drawShorts);

                if(pos.item == null) continue;

                tr1.trns(rotation * 90, tilesize, 0);
                tr2.trns(rotation * 90, -tilesize / 2f, pos.x * tilesize / 2f);

                Draw.rect(pos.item.icon(Item.Icon.medium),
                (tile.x * tilesize + tr1.x * pos.y + tr2.x),
                (tile.y * tilesize + tr1.y * pos.y + tr2.y), itemSize, itemSize);
            }

        }catch(IndexOutOfBoundsException e){
            Log.err(e);
        }
    }

    @Override
    public void unitOn(Tile tile, Unit unit){
        deflectorConveyorEntity entity = tile.entity();

        if(entity.clogHeat > 0.5f){
            return;
        }

        entity.noSleep();

        float speed = this.speed * tilesize / 2.4f;
        float centerSpeed = 0.1f;
        float centerDstScl = 3f;
        float tx = Geometry.d4[tile.rotation()].x, ty = Geometry.d4[tile.rotation()].y;

        float centerx = 0f, centery = 0f;

        if(Math.abs(tx) > Math.abs(ty)){
            centery = Mathf.clamp((tile.worldy() - unit.y) / centerDstScl, -centerSpeed, centerSpeed);
            if(Math.abs(tile.worldy() - unit.y) < 1f) centery = 0f;
        }else{
            centerx = Mathf.clamp((tile.worldx() - unit.x) / centerDstScl, -centerSpeed, centerSpeed);
            if(Math.abs(tile.worldx() - unit.x) < 1f) centerx = 0f;
        }

        if(entity.convey.size * itemSpace < 0.9f){
            unit.applyImpulse((tx * speed + centerx) * entity.delta(), (ty * speed + centery) * entity.delta());
        }
    }

    @Override
    public void update(Tile tile){
        deflectorConveyorEntity entity = tile.entity();
        entity.minitem = 1f;
        Tile next = tile.getNearby(tile.rotation());
        if(next != null) next = next.link();

        float nextMax = next != null && next.block() instanceof deflectorConveyor ? 1f - Math.max(itemSpace - next.<deflectorConveyorEntity>entity().minitem, 0) : 1f;
        int minremove = Integer.MAX_VALUE;

        for(int i = entity.convey.size - 1; i >= 0; i--){
            long value = entity.convey.get(i);
            ItemPos pos = pos1.set(value, ItemPos.updateShorts);

            //..this should never happen, but in case it does, remove it and stop here
            if(pos.item == null){
                entity.convey.removeValue(value);
                break;
            }

            float nextpos = (i == entity.convey.size - 1 ? 100f : pos2.set(entity.convey.get(i + 1), ItemPos.updateShorts).y) - itemSpace;
            float maxmove = Math.min(nextpos - pos.y, speed * entity.delta());

            if(maxmove > minmove){
                pos.y += maxmove;
                if(Mathf.isEqual(pos.x, 0, 0.1f)){
                    pos.x = 0f;
                }
                pos.x = Mathf.lerpDelta(pos.x, 0, 0.1f);
            }

            pos.y = Mathf.clamp(pos.y, 0, nextMax);

            if(pos.y >= 0.9999f && offloadDir(tile, pos.item)){
                if(next != null && next.block() instanceof deflectorConveyor){
                    deflectorConveyorEntity othere = next.entity();

                    ItemPos ni = pos2.set(othere.convey.get(othere.lastInserted), ItemPos.updateShorts);

                    if(next.rotation() == tile.rotation()){
                        ni.x = pos.x;
                    }
                    othere.convey.set(othere.lastInserted, ni.pack());
                }
                minremove = Math.min(i, minremove);
                tile.entity.items.remove(pos.item, 1);
            }else{
                value = pos.pack();

                if(pos.y < entity.minitem)
                    entity.minitem = pos.y;
                entity.convey.set(i, value);
            }
        }

        if(entity.minitem < itemSpace){
            entity.clogHeat = Mathf.lerpDelta(entity.clogHeat, 1f, 0.02f);
        }else{
            entity.clogHeat = Mathf.lerpDelta(entity.clogHeat, 0f, 1f);
        }

        if(entity.items.total() == 0){
            entity.sleep();
        }else{
            entity.noSleep();
        }

        if(minremove != Integer.MAX_VALUE) entity.convey.truncate(minremove);
    }

    @Override
    public boolean isAccessible(){
        return true;
    }

    @Override
    public int removeStack(Tile tile, Item item, int amount){
        deflectorConveyorEntity entity = tile.entity();
        entity.noSleep();
        int removed = 0;

        for(int j = 0; j < amount; j++){
            for(int i = 0; i < entity.convey.size; i++){
                long val = entity.convey.get(i);
                ItemPos pos = pos1.set(val, ItemPos.drawShorts);
                if(pos.item == item){
                    entity.convey.removeValue(val);
                    entity.items.remove(item, 1);
                    removed++;
                    break;
                }
            }
        }
        return removed;
    }

    @Override
    public void getStackOffset(Item item, Tile tile, Vector2 trns){
        trns.trns(tile.rotation() * 90 + 180f, tilesize / 2f);
    }

    @Override
    public int acceptStack(Item item, int amount, Tile tile, Unit source){
        deflectorConveyorEntity entity = tile.entity();
        return Math.min((int)(entity.minitem / itemSpace), amount);
    }

    @Override
    public void handleStack(Item item, int amount, Tile tile, Unit source){
        deflectorConveyorEntity entity = tile.entity();

        for(int i = amount - 1; i >= 0; i--){
            long result = ItemPos.packItem(item, 0f, i * itemSpace);
            entity.convey.insert(0, result);
            entity.items.add(item, 1);
        }

        entity.noSleep();
    }

    @Override
    public boolean acceptItem(Item item, Tile tile, Tile source){
        int direction = source == null ? 0 : Math.abs(source.relativeTo(tile.x, tile.y) - tile.rotation());
        float minitem = tile.<deflectorConveyorEntity>entity().minitem;
        return (((direction == 0) && minitem > itemSpace) ||
        ((direction % 2 == 1) && minitem > 0.52f)) && (source == null || !(source.block().rotate && (source.rotation() + 2) % 4 == tile.rotation()));
    }

    @Override
    public void handleItem(Item item, Tile tile, Tile source){
        byte rotation = tile.rotation();

        int ch = Math.abs(source.relativeTo(tile.x, tile.y) - rotation);
        int ang = ((source.relativeTo(tile.x, tile.y) - rotation));

        float pos = ch == 0 ? 0 : ch % 2 == 1 ? 0.5f : 1f;
        float y = (ang == -1 || ang == 3) ? 1 : (ang == 1 || ang == -3) ? -1 : 0;

        deflectorConveyorEntity entity = tile.entity();
        entity.noSleep();
        long result = ItemPos.packItem(item, y * 0.9f, pos);

        tile.entity.items.add(item, 1);

        for(int i = 0; i < entity.convey.size; i++){
            if(compareItems(result, entity.convey.get(i)) < 0){
                entity.convey.insert(i, result);
                entity.lastInserted = (byte)i;
                return;
            }
        }

        //this item must be greater than anything there...
        entity.convey.add(result);
        entity.lastInserted = (byte)(entity.convey.size - 1);
    }

    @Override
    public TileEntity newEntity(){
        return new deflectorConveyorEntity();

    }

    public static class deflectorConveyorEntity extends TileEntity{

        public float hit;

        LongArray convey = new LongArray();
        byte lastInserted;
        float minitem = 1;

        int blendbits;
        int blendsclx, blendscly;

        float clogHeat = 0f;

        @Override
        public void write(DataOutput stream) throws IOException{
            super.write(stream);
            stream.writeInt(convey.size);

            for(int i = 0; i < convey.size; i++){
                stream.writeInt(ItemPos.toInt(convey.get(i)));
            }
        }

        @Override
        public void read(DataInput stream, byte revision) throws IOException{
            super.read(stream, revision);
            convey.clear();
            int amount = stream.readInt();
            convey.ensureCapacity(Math.min(amount, 10));

            for(int i = 0; i < amount; i++){
                convey.add(ItemPos.toLong(stream.readInt()));
            }
        }
    }

    //Container class. Do not instantiate.
    static class ItemPos{
        private static short[] writeShort = new short[4];
        private static byte[] writeByte = new byte[4];

        private static short[] packShorts = new short[4];
        private static short[] drawShorts = new short[4];
        private static short[] updateShorts = new short[4];

        Item item;
        float x, y;

        private ItemPos(){
        }

        static long packItem(Item item, float x, float y){
            short[] shorts = packShorts;
            shorts[0] = (short)item.id;
            shorts[1] = (short)(x * Short.MAX_VALUE);
            shorts[2] = (short)((y - 1f) * Short.MAX_VALUE);
            return Pack.longShorts(shorts);
        }

        static int toInt(long value){
            short[] values = Pack.shorts(value, writeShort);

            short itemid = values[0];
            float x = values[1] / (float)Short.MAX_VALUE;
            float y = ((float)values[2]) / Short.MAX_VALUE + 1f;

            byte[] bytes = writeByte;
            bytes[0] = (byte)itemid;
            bytes[1] = (byte)(x * 127);
            bytes[2] = (byte)(y * 255 - 128);

            return Pack.intBytes(bytes);
        }

        static long toLong(int value){
            byte[] values = Pack.bytes(value, writeByte);

            byte itemid = values[0];
            float x = values[1] / 127f;
            float y = ((int)values[2] + 128) / 255f;

            short[] shorts = writeShort;
            shorts[0] = (short)itemid;
            shorts[1] = (short)(x * Short.MAX_VALUE);
            shorts[2] = (short)((y - 1f) * Short.MAX_VALUE);
            return Pack.longShorts(shorts);
        }

        ItemPos set(long lvalue, short[] values){
            Pack.shorts(lvalue, values);

            if(values[0] >= content.items().size || values[0] < 0)
                item = null;
            else
                item = content.items().get(values[0]);

            x = values[1] / (float)Short.MAX_VALUE;
            y = ((float)values[2]) / Short.MAX_VALUE + 1f;
            return this;
        }

        long pack(){
            return packItem(item, x, y);
        }
    }

    // shield

    @Override
    public void handleBulletHit(TileEntity entity1, Bullet bullet){
        super.handleBulletHit(entity1, bullet);

        //doesn't reflect powerful bullets
        if(bullet.damage() > maxDamageDeflect || bullet.isDeflected()) return;

        float penX = Math.abs(entity1.x - bullet.x), penY = Math.abs(entity1.y - bullet.y);

        bullet.hitbox(rect2);

        Vector2 position = Geometry.raycastRect(bullet.x - bullet.velocity().x*Time.delta(), bullet.y - bullet.velocity().y*Time.delta(), bullet.x + bullet.velocity().x*Time.delta(), bullet.y + bullet.velocity().y*Time.delta(),
                rect.setSize(size * tilesize + rect2.width*2 + rect2.height*2).setCenter(entity1.x, entity1.y));

        if(position != null){
            bullet.set(position.x, position.y);
        }

        if(penX > penY){
            bullet.velocity().x *= -1;
        }else{
            bullet.velocity().y *= -1;
        }

        //bullet.updateVelocity();
        bullet.resetOwner(entity1, entity1.getTeam());
        bullet.scaleTime(1f);
        bullet.deflect();

        ((deflectorConveyorEntity)entity1).hit = 1f;

        if(Mathf.chance(lightningChance)){
            Lightning.create(entity1.getTeam(), Pal.surge, lightningDamage, bullet.x, bullet.y, bullet.rot() + 180f, lightningLength);
        }
    }
}