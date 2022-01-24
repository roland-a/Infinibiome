package weightedgpa.infinibiome.internal.minecraftImpl;

import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.dimension.OverworldDimension;
import net.minecraft.world.gen.ChunkGenerator;
import net.minecraft.world.server.ServerWorld;

public final class IBWorldType extends WorldType {
    public IBWorldType() {
        super("Infinibiome");
    }

    public static void init() {
        new IBWorldType();
    }

    @Override
    public boolean hasInfoNotice() {
        return false;
    }

    @SuppressWarnings("SuspiciousLiteralUnderscore")
    @Override
    public int getVersion() {
        return /*0_0*/4_02;
    }

    @Override
    public ChunkGenerator<?> createChunkGenerator(World world) {
        assert world instanceof ServerWorld;

        System.out.println(world + " " + world.getDimension());

        if (!(world.getDimension() instanceof OverworldDimension)){
            return WorldType.DEFAULT.createChunkGenerator(world);
        }

        return new WrappedChunkGenerator((ServerWorld)world);
    }
}
