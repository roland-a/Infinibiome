package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

//spawns only in sparse tree density
public final class TulipGen extends PlantGenBase {
    private final Type type;

    public TulipGen(Type type, DependencyInjector di) {
        super(
            di,
            type.seedBranch
        );

        config = initConfig()
            .setPlant(type.blocks)
            .setAboveWater()
            .setWithCommonRate()
            .setWithCommonRadius()
            .setWithCommonDensity()
            .neverInMushroomIsland()
            .anyNonFreezingTemperature()
            .anyHumidity()
            .setSpawnRegion(
                PlantHelper.COMMON_REGION_RATE/4f
            )
            .setExtraConditions(
                onlyInTreeDensity(
                    di,
                    GenHelper.NON_FORESTED_TREE_INTERVAL
                )
            );

        this.type = type;
    }

    @Override
    public String toString() {
        return "Tulip{" +
            "type=" + type +
            '}';
    }

    public enum Type {
        ORANGE(
            Infinibiome.MOD_ID + ":tulipOrange",
            Blocks.ORANGE_TULIP.getDefaultState()
        ),
        PINK(
            Infinibiome.MOD_ID + ":tulipPink",
            Blocks.PINK_TULIP.getDefaultState()
        ),
        RED(
            Infinibiome.MOD_ID + ":tulipRed",
            Blocks.RED_TULIP.getDefaultState()
        ),
        WHITE(
            Infinibiome.MOD_ID + ":tulipWhite",
            Blocks.WHITE_TULIP.getDefaultState()
        );

        private final String seedBranch;
        private final ImmutableList<BlockState> blocks;

        Type(String seedBranch, BlockState blocks) {
            this.seedBranch = seedBranch;
            this.blocks = ImmutableList.of(blocks);
        }
    }
}
