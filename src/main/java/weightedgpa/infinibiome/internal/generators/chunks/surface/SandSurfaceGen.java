package weightedgpa.infinibiome.internal.generators.chunks.surface;

import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraftforge.common.Tags;
import weightedgpa.infinibiome.api.Infinibiome;
import weightedgpa.infinibiome.api.dependency.DependencyInjector;
import weightedgpa.infinibiome.api.generators.SurfaceTimings;
import weightedgpa.infinibiome.api.generators.Timing;
import net.minecraft.block.BlockState;
import weightedgpa.infinibiome.internal.generators.utils.GenHelper;

import java.util.ArrayList;
import java.util.List;

import static weightedgpa.infinibiome.internal.generators.chunks.surface.SurfaceHelper.*;
import static weightedgpa.infinibiome.internal.generators.utils.condition.ConditionHelper.*;

public final class SandSurfaceGen extends SurfaceGenBase {
    private static final List<Block> VALID_BLOCKS = new ArrayList<>();

    static {
        VALID_BLOCKS.addAll(Tags.Blocks.SAND.getAllElements());
        VALID_BLOCKS.addAll(Tags.Blocks.DIRT.getAllElements());
    };

    private final Type type;

    public SandSurfaceGen(Type type, DependencyInjector di){
        super(di, Infinibiome.MOD_ID + ":sand" + type.name());

        this.type = type;

        config = initConfig()
            .setBlock(
                type.block
            )
            .setPatchy()
            .setExtendsDown()
            .setValidBlocks(VALID_BLOCKS)
            .setNeverUnderwater()
            .neverInMushroomIsland()
            .inSpawnRegion(type.spawnRegionRate)
            .addExtraConditions(
                onlyInHumidity(
                    di,
                    GenHelper.DRYISH
                )
            );
    }

    @Override
    public Timing getTiming() {
        return SurfaceTimings.PATCHY;
    }

    @Override
    public String toString() {
        return "Sand{" +
            "type=" + type +
            '}';
    }

    public enum Type {
        SAND(
            COMMON_REGION_RATE / 2,
            Blocks.SAND.getDefaultState()
        ),
        SAND_RED(
            COMMON_REGION_RATE / 4,
            Blocks.RED_SAND.getDefaultState()
        ),
        SANDSTONE(
            COMMON_REGION_RATE / 4,
            Blocks.SANDSTONE.getDefaultState()
        ),
        SANDSTONE_RED(
            COMMON_REGION_RATE / 4,
            Blocks.RED_SANDSTONE.getDefaultState()
        );

        private final double spawnRegionRate;
        private final BlockState block;

        Type(double spawnRegionRate, BlockState block) {
            this.spawnRegionRate = spawnRegionRate;
            this.block = block;
        }
    }
}
