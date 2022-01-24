package weightedgpa.infinibiome.internal.generators.interchunks.mob;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.AnimalEntity;
import net.minecraft.entity.passive.SheepEntity;
import net.minecraft.item.DyeColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IWorld;
import org.apache.commons.lang3.Validate;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;
import weightedgpa.infinibiome.internal.misc.MCHelper;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.pos.InterChunkPos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns in cold climates with moderate tree density
public final class SheepGen extends MobGenBase {
    private static final DyeColor[] WEIGHTED_DYE_COLORS;

    static {
        List<DyeColor> weightedDyeColor = new ArrayList<>();

        weightedDyeColor.addAll(
            Collections.nCopies(820, DyeColor.WHITE)
        );

        weightedDyeColor.addAll(
            Collections.nCopies(50, DyeColor.BLACK)
        );

        weightedDyeColor.addAll(
            Collections.nCopies(50, DyeColor.GRAY)
        );

        weightedDyeColor.addAll(
            Collections.nCopies(50, DyeColor.GRAY)
        );

        weightedDyeColor.addAll(
            Collections.nCopies(29, DyeColor.BROWN)
        );

        weightedDyeColor.addAll(
            Collections.nCopies(1, DyeColor.PINK)
        );

        WEIGHTED_DYE_COLORS = weightedDyeColor.toArray(new DyeColor[0]);

        Validate.isTrue(WEIGHTED_DYE_COLORS.length == 1000);
    }

    public SheepGen(DependencyInjector di) {
        super(di, Infinibiome.MOD_ID + ":sheep");

        config = initConfig()
            .getEntity(this::getEntity)
            .setGroupCount(2, 3)
            .setBabyChance(MobHelper.COMMON_BABY_RATE)
            .alwaysAboveWater()
            .neverInMushroomIsland()
            .anyNonHighSlope()
            .onlyOnNonBeachLand()
            .anyTemperatureIncludingFreezing()
            .anyNonDesertHumidity()
            .setChancePerChunk(MobHelper.COMMON_RATE * 2)
            .addExtraConditions(
                //sheeps are 1/2 less common outside coldish temperatures or forests
                chancePerChunk(
                    0.7
                )
                .activeOutside(
                    onlyInTemperature(
                        di,
                        GenHelper.COLDISH
                    )
                ),
                chancePerChunk(
                    0.7
                )
                .activeOutside(
                    onlyInTreeDensity(
                        di,
                        GenHelper.FORESTED_TREE_INTERVAL
                    )
                ),
                onlyIfNotNear(
                    di,
                    2,
                    MobGenBase::canSpawnAtInterChunk,
                    WolfGen.class
                )
            );
    }

    private AnimalEntity getEntity(BlockPos mobPos, InterChunkPos interChunkPos, IWorld world) {
        SheepEntity result = new SheepEntity(EntityType.SHEEP, world.getWorld());

        result.setFleeceColor(
            getSheepColor(MCHelper.to2D(mobPos))
        );

        return result;
    }

    private DyeColor getSheepColor(BlockPos2D pos) {
        Random random = randomGen.getRandom(pos.getBlockX(), pos.getBlockZ());

        int randomIndex = random.nextInt(WEIGHTED_DYE_COLORS.length);

        return WEIGHTED_DYE_COLORS[randomIndex];
    }
}