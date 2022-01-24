
package weightedgpa.infinibiome.internal.generators.posdata;


import net.minecraft.block.Blocks;
import net.minecraft.block.BlockState;
import weightedgpa.infinibiome.internal.floatfunc.generators.RandomGen;
import weightedgpa.infinibiome.api.pos.BlockPos2D;
import weightedgpa.infinibiome.api.generators.Seed;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

public enum GroundBlocks {
    DIRT(
        Blocks.DIRT.getDefaultState(),
        2, 4
    ),
    GRAVEL(
        Blocks.GRAVEL.getDefaultState(),
        2, 4
    ),
    SAND(
        Blocks.SAND.getDefaultState(),
        2, 4,
        Blocks.SANDSTONE.getDefaultState(),
        2, 4
    ),
    STONE(
        Blocks.STONE.getDefaultState(),
        2, 4
    );

    private static final RandomGen randomProducer = new RandomGen(Seed.ROOT.newSeed("surfaceBlocks"));

    private final List<List<BlockState>> blockChoices = new ArrayList<>();

    GroundBlocks(BlockState block, int min, int max){
        this(block, min, max, block, 0, 0);
    }

    GroundBlocks(BlockState block1, int min1, int max1, BlockState block2, int min2, int max2){
        for (int block1Count = min1; block1Count <= max1; block1Count++){
            for (int block2Count = min2; block2Count <= max2; block2Count++){
                List<BlockState> blocks = new ArrayList<>();

                blocks.addAll(
                    Collections.unmodifiableCollection(
                        Collections.nCopies(
                            block1Count,
                            block1
                        )
                    )
                );

                blocks.addAll(
                    Collections.unmodifiableCollection(
                        Collections.nCopies(
                            block2Count,
                            block2
                        )
                    )
                );

                blockChoices.add(blocks);
            }
        }
    }

    public final List<BlockState> getSurfaceBlocks(BlockPos2D pos){
        Random randomFromPos = randomProducer.getRandom(pos.getBlockX(), pos.getBlockZ());

        int randomIndex = randomFromPos.nextInt(blockChoices.size());

        return blockChoices.get(randomIndex);
    }

    private List<BlockState> repeatBlocks(@Nullable BlockState block, int amount){
        if (block == null){
            return Collections.emptyList();
        }

        return Collections.nCopies(
            amount,
            block
        );
    }
}