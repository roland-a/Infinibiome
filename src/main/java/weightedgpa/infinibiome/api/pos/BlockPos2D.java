package weightedgpa.infinibiome.api.pos;

import net.minecraft.util.Direction;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import weightedgpa.infinibiome.internal.misc.Log2helper;

import java.util.Objects;
import java.util.function.ToIntFunction;

//should not be extended
public class BlockPos2D {
    public static IntPosInfo<BlockPos2D> INFO = new IntPosInfo<BlockPos2D>() {
        @Override
        public int getIntX(BlockPos2D pos) {
            return pos.getBlockX();
        }

        @Override
        public int getIntZ(BlockPos2D pos) {
            return pos.getBlockZ();
        }

        @Override
        public int getLog2Scale() {
            return 0;
        }

        @Override
        public BlockPos2D build(int x, int z) {
            return new BlockPos2D(x, z);
        }
    };

    int blockX;
    int blockZ;

    public BlockPos2D(int blockX, int blockZ) {
        this.blockX = blockX;
        this.blockZ = blockZ;
    }

    public int getBlockX() {
        return blockX;
    }

    public int getBlockZ() {
        return blockZ;
    }

    public BlockPos2D offset(int x, int z){
        return new BlockPos2D(
            getBlockX() + x,
            getBlockZ() + z
        );
    }

    public BlockPos2D offset(Direction direction, int amount){
        switch (direction){
            case NORTH: return this.offset(0, -amount);
            case SOUTH: return this.offset(0, amount);
            case WEST: return this.offset(-amount, 0);
            case EAST: return this.offset(amount, 0);
            default: return this;
        }
    }

    public BlockPos2D offset(Direction direction){
        return offset(direction, 1);
    }

    public BlockPos to3D(int y){
        return new BlockPos(
            this.getBlockX(),
            y,
            this.getBlockZ()
        );
    }

    public BlockPos to3D(ToIntFunction<BlockPos2D> func){
        return new BlockPos(
            this.getBlockX(),
            func.applyAsInt(this),
            this.getBlockZ()
        );
    }

    public ChunkPos toChunkPos(){
        return new ChunkPos(
            Log2helper.floorDiv(getBlockX(), 4),
            Log2helper.floorDiv(getBlockZ(), 4)
        );
    }

    public BlockPos2D toImmutable(){
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        BlockPos2D that = (BlockPos2D)o;
        return getBlockX() == that.getBlockX() &&
            getBlockZ() == that.getBlockZ();
    }

    @Override
    public int hashCode() {
        return Objects.hash(blockX, blockZ);
    }

    @Override
    public String toString() {
        return String.format("BlockPos2D(x=%s, z=%s)", getBlockX(), getBlockZ());
    }

    public static class Mutable extends BlockPos2D{
        public Mutable() {
            super(0 ,0);
        }

        public void setPos(int blockX, int blockZ) {
            this.blockX = blockX;
            this.blockZ = blockZ;
        }

        @Override
        public BlockPos2D toImmutable(){
            return new BlockPos2D(
                getBlockX(),
                getBlockZ()
            );
        }

        @Override
        public int hashCode() {
            throw new UnsupportedOperationException("should not be stored");
        }

        @Override
        public String toString() {
            return "BlockPos2D.Mutable{" +
                "blockX=" + getBlockX() +
                ", blockZ=" + getBlockZ() +
                '}';
        }
    }
}
