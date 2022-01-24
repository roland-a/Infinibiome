package weightedgpa.infinibiome.api.pos;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import weightedgpa.infinibiome.internal.misc.MathHelper;
import weightedgpa.infinibiome.internal.pos.PosHelper;

import java.util.Objects;
import java.util.Random;
import java.util.function.Consumer;

/**
 * Represents a 2x2 chunk
 */
public final class InterChunkPos  {
    public static final int CENTER_LENGTH = 16;
    public static final int OVERALL_LENGTH = CENTER_LENGTH * 2;

    private final ChunkPos lowestChunkPos;
    private final int lowestX;
    private final int lowestZ;

    public InterChunkPos(int x, int z) {
        this(new ChunkPos(x, z));
    }

    public InterChunkPos(ChunkPos lowestChunkPos) {
        this.lowestChunkPos = lowestChunkPos;

        this.lowestX = lowestChunkPos.x * CENTER_LENGTH + 8;
        this.lowestZ = lowestChunkPos.z * CENTER_LENGTH + 8;
    }

    public InterChunkPos(BlockPos2D pos) {
        this(
            pos.offset(-8, -8).toChunkPos()
        );
    }

    public InterChunkPos(BlockPos pos) {
        this(
            new ChunkPos(pos.add(-8, 0, -8))
        );
    }

    public static final IntPosInfo<InterChunkPos> INFO = new IntPosInfo<InterChunkPos>() {
        @Override
        public int getIntX(InterChunkPos pos) {
            return pos.getX();
        }

        @Override
        public int getIntZ(InterChunkPos pos) {
            return pos.getZ();
        }

        @Override
        public int getLog2Scale() {
            return 4;
        }

        @Override
        public int getOffset() {
            return 8;
        }

        @Override
        public InterChunkPos build(int x, int z) {
            return new InterChunkPos(x, z);
        }
    };

    public InterChunkPos offset(int x, int z) {
        return new InterChunkPos(
            new ChunkPos(
                lowestChunkPos.x + x,
                lowestChunkPos.z + z
            )
        );
    }

    /**
     * @return The x value of the lowest chunk inside this interChunkPos.
     */
    public int getX() {
        return lowestChunkPos.x;
    }

    /**
     * @return The z value of the lowest chunk inside this interChunkPos.
     */
    public int getZ() {
        return lowestChunkPos.z;
    }

    /**
     * @return The lowest chunkPos2D inside this interChunkPos.
     */
    public ChunkPos getLowestChunkPos() {
        return lowestChunkPos;
    }

    /**
     * @return The lowest blockPos2D in this interChunkPos's center.
     */
    public BlockPos2D getLowestCenterBlockPos(){
        return new BlockPos2D(lowestX, lowestZ);
    }

    /**
     * @param consumer The function that will take in every blockPos2D in the center.
     */
    public void forEachCenterPos(Consumer<BlockPos2D> consumer){
        BlockPos2D.Mutable lowestPos = new BlockPos2D.Mutable();
        
        for (int x = 0; x < 16; x++){
            for (int z = 0; z < 16; z++){
                lowestPos.setPos(
                    lowestX + x,
                    lowestZ + z
                );
                
                consumer.accept(lowestPos);
            }
        }
    }

    public void forEachAllPos(Consumer<BlockPos2D> consumer){
        BlockPos2D.Mutable lowestPos = new BlockPos2D.Mutable();

        for (int x = 0; x < 32; x++){
            for (int z = 0; z < 32; z++){
                lowestPos.setPos(
                    lowestX - 8 + x,
                    lowestZ - 8 + z
                );

                consumer.accept(lowestPos);
            }
        }
    }



    public BlockPos2D getRandomCenterPos(Random random) {
        return new BlockPos2D(
            MathHelper.randomInt(
                lowestX,
                lowestX + 16,
                random
            ),
            MathHelper.randomInt(
                lowestZ,
                lowestZ + 16,
                random
            )
        );
    }

    public boolean containsPos(BlockPos2D pos){
        return PosHelper.contains(
            lowestX - 8,
            lowestZ - 8,
            OVERALL_LENGTH,
            pos.getBlockX(),
            pos.getBlockZ()
        );
    }

    public boolean containsCenterPos(BlockPos2D pos){
        return PosHelper.contains(
            lowestX,
            lowestZ,
            CENTER_LENGTH,
            pos.getBlockX(),
            pos.getBlockZ()
        );
    }

    @Override
    public String toString() {
        return String.format(
            "InterChunkPos(x=%s z=%s)", getX(), getZ()
        );
    }

    @Override
    @SuppressWarnings("all")
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        InterChunkPos that = (InterChunkPos)o;
        return Objects.equals(lowestChunkPos, that.lowestChunkPos);
    }

    @Override
    public int hashCode() {
        return lowestChunkPos.hashCode();
    }

}
