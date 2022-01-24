package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.TurtleEntity;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.posdata.PosDataKeys;
import weightedgpa.infinibiome.internal.generators.utils.condition.Condition;
import weightedgpa.infinibiome.internal.misc.Helper;
import weightedgpa.infinibiome.internal.misc.MCHelper;

import java.util.List;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in non-snow sand beaches
public final class TurtleGen extends MobGenBase {
    public TurtleGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":turtle");

        config = initConfig()
            .getEntity(w -> new TurtleEntity(EntityType.TURTLE, w.getWorld()))
            .setGroupCount(1)
            .setBabyChance(0)
            .includingUnderwater()
            .neverInMushroomIsland()
            .anySlopeIncludingHigh()
            .setLandMass(
                l -> !l.isLand()
            )
            .anyTemperatureIncludingFreezing()
            .anyHumidityIncludingDesert()
            .setChancePerChunk(1/50d)
            .addExtraConditions(
                //half as common away from beaches
                chancePerChunk(0.5).activeOutside(
                    onlyInLandMass(
                        di,
                        l -> l.getTransitionToBeach() > 0.8
                    )
                ),
                //makes sure turtles are only in sandy beaches
                new Condition.BoolInterpolated() {
                    @Override
                    public boolean passes(BlockPos2D pos) {
                        List<BlockState> groundBlocks = posData.get(PosDataKeys.GROUND_BLOCKS, pos);

                        if (groundBlocks.isEmpty()) {
                            return false;
                        }

                        return groundBlocks.get(0)
                            .getBlock()
                            .equals(Blocks.SAND);
                    }

                    @Override
                    public String toString() {
                        return "CheckSandy{}";
                    }
                },
                //makes sure turtles don't spawn in inland beaches
                new Condition.BoolInterpolated() {
                    @Override
                    public boolean passes(BlockPos2D pos) {
                        return nearOcean(pos);
                    }

                    @Override
                    public boolean isSlow() {
                        return true;
                    }

                    @Override
                    public String toString() {
                        return "CheckNotInland{}";
                    }
                }
            );
    }

    private boolean nearOcean(BlockPos2D pos) {
        return Helper.findSuitableSpot(
            pos,
            100,
            this::inOcean,
            BlockPos2D.INFO
        ) != null;
    }

    private boolean inOcean(BlockPos2D pos) {
        return posData.get(PosDataKeys.MAPPED_HEIGHT, pos) < MCHelper.WATER_HEIGHT - 16;
    }
}
