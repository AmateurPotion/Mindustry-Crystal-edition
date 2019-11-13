package io.anuke.mindustry.entities.units;

import io.anuke.arc.math.Mathf;
import io.anuke.mindustry.Vars;
import io.anuke.mindustry.content.Items;
import io.anuke.mindustry.content.UnitTypes;
import io.anuke.mindustry.entities.type.BaseUnit;
import io.anuke.mindustry.entities.type.TileEntity;
import io.anuke.mindustry.entities.type.base.Dagger;
import io.anuke.mindustry.gen.Call;
import io.anuke.mindustry.type.Item;
import io.anuke.mindustry.type.UnitType;

import static io.anuke.mindustry.Vars.unitGroups;

public class UnitDrops{
    private static Item[] dropTable1;

    public static void dropItems(BaseUnit unit){
        //items only dropped in waves for enemy team
        if(unit.getType() == UnitTypes.dagger&&unit.getTeam() != Vars.waveTeam || !Vars.state.rules.unitDrops){
            return;
        }

        TileEntity core = unit.getClosestEnemyCore();

        if(core == null || core.dst(unit) > Vars.mineTransferRange){
            return;
        }

        if(dropTable1 == null){
            dropTable1 = new Item[]{Items.titanium, Items.silicon, Items.lead, Items.copper, Items.meteorShard};
        }

        for(int i = 0; i < 3; i++){
            for(Item item : dropTable1){
                //only drop unlocked items
                /*
                if(!Vars.headless && !Vars.data.isUnlocked(item)){
                    continue;
                }

                 */

                if(Mathf.chance(0.03)){
                    int amount = Mathf.random(20, 40);
                    amount = core.tile.block().acceptStack(item, amount, core.tile, null);
                    if(amount > 0){
                        Call.transferItemTo(item, amount, unit.x + Mathf.range(2f), unit.y + Mathf.range(2f), core.tile);
                    }
                }
            }
        }
    }
}
