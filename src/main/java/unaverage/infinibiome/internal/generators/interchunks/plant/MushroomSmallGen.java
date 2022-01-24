package weightedgpa.infinibiome.internal.generators.interchunks.plant;

import com.google.common.collect.ImmutableList;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;

import java.util.List;

public final class MushroomSmallGen extends PlantGenBase {
    private final Type type;

    public MushroomSmallGen(Type type, DependencyInjector di) {
        super(
            di,
            Infinibiome.MOD_ID + ":mushroomSmall" + type.name()
        );

        config = initConfig()
            .setPlant(
                type.blocks
            )
            .setAboveWater()
            .setWithCommonRate()
            .setWithCommonRadius()
            .setWithCommonDensity()
            .alsoInMushroomIsland()
            .anyTemperatureIncludingFreezing()
            .anyHumidity()
            .setNoSpawnRegion()
            .setNoExtraConditions();

        this.type = type;
    }

    public enum Type {
        BROWN(Blocks.BROWN_MUSHROOM.getDefaultState()),
        RED(Blocks.RED_MUSHROOM.getDefaultState());

        private final List<BlockState> blocks;

        Type(BlockState blocks) {
            this.blocks = ImmutableList.of(blocks);
        }
    }

    @Override
    public String toString() {
        return "MushroomSmall{" +
            "type=" + type +
            '}';
    }
}